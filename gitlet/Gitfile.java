package gitlet;
import jdk.jshell.execution.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

public class Gitfile{
    static final File OBJECTS = new File(".gitlet/objects");
    static final File INDEX = Utils.join(".gitlet", "INDEX");
    static final File HEAD = Utils.join(".gitlet", "HEAD");

    /**
     * set up dog gitlet folder and subfolders
     */
    static void setDirectory() {
        OBJECTS.mkdirs();
        //this is for branching
        Utils.join(".gitlet", "refs", "heads").mkdirs();
    }

    /**
     * write the commit object to .gitlet/objects/
     * @param GitObject
     */
    static void writeObject(Objects GitObject) {
        String hash = Utils.hash(GitObject);
        Utils.join(OBJECTS,
                getHashHead(hash)).mkdir();
        java.io.File file = getObjectsAsFile(hash);
        if (GitObject.getType().equals("commit")) {
            //point head pointer to current head
            writeHead(hash);
            updateBranchHead(hash);
            Utils.writeObject(file, GitObject);
        }
        //handle blob
        else {
            //TODO move this function to command
            if (writeStagedToIndex(hash, GitObject.getCwdName())) {
                //if need to stage, store staged file to objects folder
                Utils.writeObject(file, GitObject);
            }
        }
    }

    /**
     * write the current head pointer location in .gitlet/HEAD file
     * @param hash
     */
    static void writeHead(String hash) {
        Utils.writeContents(HEAD, hash);
    }

    /**
     * get current HEAD commit as a string
     * @return hash of the current commit/HEAD
     */
    static String getHead() {
        return Utils.readContentsAsString(Gitfile.HEAD);
    }

    /**
     * a helper function for writeObject
     * write branch head in .gitlet/refs/heads
     * @param hash
     */
    static void updateBranchHead(String hash) {
        File branchHead = Utils.join(".gitlet", "refs", "heads", "master");
        Utils.writeContents(branchHead, hash);
    }

    /**
     * write git INDEX file to record staged file
     * @param hash sha1 of the staged file
     * @param filename filename of the stage file in the CWD
     * @return true if write to index
     */
    static boolean writeStagedToIndex(String hash, String filename) {
        Objects fileList;
        if (INDEX.exists()) {
            fileList = Utils.readObject(INDEX, Objects.class);
            if (fileList.indexFile.containsKey(filename)) {
                //if dictionary contains the same file, compare hashcode
                //same as last commit/staged already
                if (hash == fileList.indexFile.get(filename).getSha1Hash()) {
                    //this version and previous version in staging area are the same
                    //don't stage
                    return false;
                }
                //TODO check for commited cases, must look for commit folder;
                //case: commited, modified, added again (replace commit entry in INDEX), need to remove from INDEX

            }
            //write update to file
            Gitindex update = new Gitindex(hash,filename);
            fileList.indexFile.put(filename, update);

            //fileList.printDict();
        } else {
            //if INDEX doesn't exist, put the entry as the first index
            fileList = new Objects("index");
            fileList.putEntryToDict(hash, filename);
        }

        Utils.writeObject(INDEX, fileList);
    return true;
    }

    /**
     * Helper function to get a Blob/Commit object with hash
     * @param hash hashcode of the Objects
     * @return corresponding file, might not exist
     */
    static File getObjectsAsFile(String hash) {
        java.io.File file = Utils.join(OBJECTS, getHashHead(hash), getHashBody(hash));
        return file;
    }

    /**
     * Getter for hash code, used for naming things
     * @return
     */
    static String getHashHead(String hash) {
        return hash.substring(0, 2);
    }

    static String getHashBody(String hash) {
        return hash.substring(2);
    }


}
