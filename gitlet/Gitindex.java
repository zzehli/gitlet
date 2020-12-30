package gitlet;

import java.io.Serializable;

public class Gitindex implements Serializable {
    private final String filename;
    private String sha1Hash;


    /**
     * constructor with all three elements set up
     * @param hash
     * @param filename
     */
    public Gitindex(String hash, String filename) {
        this.filename = filename;
        //tracked, staged, removed (from stage), modified (not staged)
        sha1Hash = hash;
    }

    public String getSha1Hash() {
        return sha1Hash;
    }

    public String getFilename() { return filename; }


    @Override
    public int hashCode() {
        return filename.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Gitindex){
            return (((Gitindex) obj).filename.hashCode() == this.filename.hashCode());
        }
        return false;
    }




}
