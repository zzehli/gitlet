package gitlet;
import jdk.jshell.execution.Util;

import java.io.File;
import java.io.Serializable;

//TODO figure out whether loc is in hash form
public class Gitfile{

    /**
     * set up dog gitlet folder and subfolders
     */
    static void setDirectory() {
        Utils.join(".gitlet", "objects").mkdirs();
        //TODO figure out differences between branches and refs folder
        Utils.join(".gitlet", "branches").mkdir();
        Utils.join(".gitlet", "refs").mkdir();
    }
    /**
     * write the current head pointer location in .gitlet/HEAD file
     * @param loc
     */
    static void writeHead(String loc) {
        java.io.File head = Utils.join(".gitlet", "HEAD");
        Utils.writeContents(head, loc);
    }

    /**
     * write the blob object to .gitlet/objects/
     * @param o
     */
    static void writeObjects(Objects o) {

        //Utils.writeObject(file, o);
    }

    /**
     * write the commit object to .gitlet/objects/
     * @param commit
     */
    static void writeCommit(Objects commit) {
        String hash = Utils.hash(commit);
        //TODO write getHead and getBody for hash code
        Utils.join(".gitlet", "objects",
                getHashHead(hash)).mkdir();
        java.io.File file = Utils.join(".gitlet", "objects",
                getHashHead(hash), getHashBody(hash) + ".txt");
        //TODO here the content is serialized twice
        Utils.writeObject(file, commit);
    }

    /**
     * read from a Object file (blob/commit) and return a Objects of corresponding type
     * @param hash hashcode of the object
     * @return Objects of corresponding type
     */
    static Objects getObject(String hash) {
        java.io.File file = Utils.join(".gitlet", "objects", hash.substring(0, 2), hash.substring(2));
        return Utils.readObject(file, Objects.class);
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
