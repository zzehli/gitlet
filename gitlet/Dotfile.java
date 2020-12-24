package gitlet;
import jdk.jshell.execution.Util;

import java.io.File;
import java.io.Serializable;

//TODO figure out whether loc is in hash form
public class Dotfile {
    /**
     * write the current head pointer location in .gitlet/HEAD file
     * @param loc
     */
    public void writeHead(String loc) {
        java.io.File head = Utils.join(".gitlet", "HEAD");
        Utils.writeContents(head, loc);
    }

    /**
     * write the blob object to .gitlet/objects/
     * @param blob
     */
    public void writeBlob(Objects blob) {
        java.io.File file = Utils.join(".gitlet", "objects", blob.getHashHead(),blob.getHashBody() );
        Utils.writeObject(file, blob);
    }

    /**
     * write the commit object to .gitlet/objects/
     * @param commit
     */
    public void writeCommit(Objects commit) {
        java.io.File file = Utils.join(".gitlet", "objects", commit.getHashHead(), commit.getHashBody() );
        //TODO here assume every field in commit is ready to be written
        Utils.writeObject(file, commit);
    }

    /**
     * read from a blob file and return a Objects of blob type
     * @param hash hashcode of the object
     * @return Objects of blob type
     */
    public Objects getBlob(String hash) {
        java.io.File file = Utils.join(".gitlet", "objects", hash.substring(0, 2), hash.substring(2));
        return Utils.readObject(file, Objects.class);
    }



}
