package com.rousseau_alexandre.libmusicPrettifier;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyMp3File extends Mp3File {

    /**
     * Extension of the file
     */
    private static final String RETAG_EXTENSION = ".retag";
    /**
     * Represent the current ID3 tag for the file
     */
    private ID3v1 currentID3;
    /**
     * Represent the new ID3 tag with informations fetched from Discog API
     */
    private ID3v24Tag newID3 = new ID3v24Tag();

    /**
     * Open a new MP3 file
     *
     * @param path
     * @throws IOException
     * @throws UnsupportedTagException
     * @throws InvalidDataException
     */
    public MyMp3File(String path) throws IOException, UnsupportedTagException, InvalidDataException {
        super(path);

        if (hasId3v2Tag()) {
            currentID3 = getId3v2Tag();
        } else if (hasId3v1Tag()) {
            currentID3 = getId3v1Tag();
        }
    }

    /**
     * @return the current ID3 tag linked to this file
     */
    public ID3v1 getCurrentID3() {
        return currentID3;
    }

    /**
     * @return get the ID3 tag who'll be saved in the file
     */
    public ID3v24Tag getNewID3() {
        return newID3;
    }

    /**
     * For testing purpose
     *
     * @param newID3
     */
    public void setNewID3(ID3v24Tag newID3) {
        this.newID3 = newID3;
    }

    /**
     * Update ID3 tag with `newID3` variable
     *
     * @throws IOException
     * @throws NotSupportedException
     */
    public void update() throws IOException, NotSupportedException {
        // this.setId3v2Tag(newID3);
        this.setId3v1Tag(newID3);
        this.currentID3 = newID3;
        this.save(getRetagFilename());

        File origin = new File(this.getFilename());
        File retag = new File(getRetagFilename());

        origin.delete();
        retag.renameTo(origin);
    }

    private String getRetagFilename() {
        return this.getFilename() + RETAG_EXTENSION;
    }

    /**
     * Should extract name of title from the filename: remove extension, remove
     * leading number
     *
     * @return
     */
    public String getSearchStringFromFile() {
        File localFile = new File(getFilename());
        String name = localFile.getName();
        // remove extension
        name = name.replace(".mp3", "");
        // remove leading number
        name = name.replaceAll("[0-9]+", "");
        // remove leading number
        name = name.replaceAll(" +- +", " ");
        // remove leading space
        name = name.trim();

        return name;
    }

    /**
     * Send a request to Discog API. It will gess informations to search from
     * the current informations contained in ID3 tag or from the filename
     *
     * @return `true` if success
     */
    public boolean getInformations() {
        Discog api = new Discog();
        try {
            DiscogRelease result = api.search(getSearchStringFromFile());
            if (result != null) {
                newID3 = result.toID3();
                return true;
            }
        } catch (IOException ex) {
            Logger.getLogger(MyMp3File.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
