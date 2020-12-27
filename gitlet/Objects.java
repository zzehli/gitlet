package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Objects implements Serializable {
    private String type;

    //for blob
    private String content;
    private String CwdName;

    //for commits
    private String msg;
    //String stamp = Utils.timeStamp()
    private String time;
    private LinkedList<String> parentHash;

    //for index, from filename name to individual index object
    public HashMap<String, Gitindex> indexFile;


    /**
     * Constructor for initial commit
     */
    public Objects() {
        //00:00:00 UTC, Thursday, 1 January 1970
        time = Instant.EPOCH.toString();
        msg = "initial commit";
        type = "commit";
    }

    /**
     *
     * Constructor for blob
     * @param content content string
     * @param filename name in cwd
     */
    public Objects(String content, String filename) {
        this.content = content;
        type = "blob";
        CwdName = filename;
    }

    /**
     * Constructor just to initialize Hashmap
     * @param args only recognizes "index"
     */
    public Objects(String args) {
        if (args.equals("index")) {
            indexFile = new HashMap<>();
        }
    }

    public String getType() {
        return type;
    }

    public String getCwdName() {
        return CwdName;
    }

    /**
     * suppose the Object is a commit Object, set timestamp and parent Hash
     * @param msg set the message of the commit
     */
    public void makeCommit(String msg) {
        time = Utils.timeStamp();
        parentHash = new LinkedList<>();
        parentHash.add(Gitfile.getHead());
        this.msg = msg;
        type = "commit";
    }

    /**
     * provide an access to put method for HashMap of entries/object
     * @param hash hash code of the object
     * @param fileName cwd filename
     */
    public void putEntryToDict(String hash, String fileName) {
        try {
            Gitindex entry = new Gitindex(hash, fileName);
            indexFile.put(fileName, entry);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * update dictionary of files according to parameter passed, parameter Objects
     * take precedent: currHead.compare(Staged)
     * @param staged staged/newer file to compare with
     */
    public void updateDictDiff(Objects staged) {
        if (indexFile == null)
            return;
        for (String filename : staged.indexFile.keySet()) {
            indexFile.put(filename, staged.indexFile.get(filename));
        }
    }

    /**
     * helper function to print out the Object list stored in the object
     */
    public void printDict() {
        String content = "";
        for (Gitindex entry: indexFile.values())
        {
            content += entry.getSha1Hash() + "\t"
                    + entry.getFilename() + "\n";

        }
        System.out.println(content);
    }

}
