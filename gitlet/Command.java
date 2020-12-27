package gitlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import static gitlet.Gitfile.INDEX;

public class Command {

    /**
     * init
     * set up folders and set the zero commit
     */
    static void init() {
        Gitfile.setDirectory();
        Objects initCommit = new Objects();
        Gitfile.writeObject(initCommit);
    }

    /**
     * add [filename]
     * add the file to the staging area
     * @param fileToAdd filename of the new file to be added
     */
    static void add(String fileToAdd) {
        //staging area is the Tree class under Objects and corresponding index file
        //first read the file content and make a blob
        File newFile = new File(fileToAdd);
        Objects blob = new Objects(Utils.readContentsAsString(newFile), fileToAdd);
        //write the blob to disk
        Gitfile.writeObject(blob);

    }

    /**
     * commit all files in the staging area
     */
    static void commit(String msg) {
        //read from the stage, deserialize hashmap
        //TODO stageDELEt
        Objects stageEntries = Utils.readObject(INDEX, Objects.class);
        //need to access previous commit to compare versions
        File head = Gitfile.getObjectsAsFile(Gitfile.getHead());
        if (stageEntries== null ||stageEntries.indexFile.isEmpty() ) {
            //TODO check staged removal file too
            Utils.exitWithError("No changes added to the commit");
        }
        Objects currHeads = Utils.readObject(head, Objects.class);
        //compare and override old version with staged/removed
        currHeads.updateDictDiff(stageEntries);
        //TODO currHeads.updateDictDiff(stageDelete);
        currHeads.makeCommit(msg);
        //write commit Object to file
        Gitfile.writeObject(currHeads);
        //wipe the INDEX file
        try {
            new FileOutputStream(INDEX).close();
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

    }
}
