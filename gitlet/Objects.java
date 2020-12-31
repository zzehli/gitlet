package gitlet;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    private String time;

    private LinkedList<String> parentHash;

    //for index, from filename name to individual index object
    public HashMap<String, Gitindex> indexFile;


    /**
     * Constructor for initial commit
     */
    public Objects() {
//        Date: Thu Nov 9 17:01:33 2017 -0800
        time = Instant.EPOCH.atZone(ZoneId.systemDefault()).format(
                DateTimeFormatter.ofPattern("EEE MMM d kk:mm:ss uuuu xxxx"));
        msg = "initial commit";
        type = "commit";
        indexFile = new HashMap<>();
        parentHash = new LinkedList<>();
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

    public String getContent() { return content;}

    /**
     * Get the hash of the first parent,
     * later merged in parents are later down the line
     * @return hash of the parent commit
     */
    public String getParentHash() {
        if (parentHash.isEmpty()) {
            return "";
        }
        return parentHash.getFirst(); }

    /**
     * Return formated parent hash in the cases of merging commit
     * TODO need a way to signify merg commit
     * @return
     */
    public String getMergeHash() {
        String content = "";
        for (String i: parentHash) {
            content += i.substring(0,7) + " ";
        }
        return content;
    }

    public String getTime() { return time; }

    public String getMsg() { return msg; }

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
     * @param stageRm staged for removal
     */
    public void updateDictDiff(Objects staged, Objects stageRm) {
        if (!staged.indexFile.isEmpty()) {
            for (String filename : staged.indexFile.keySet()) {
                indexFile.put(filename, staged.indexFile.get(filename));
            }
        }
        if (!stageRm.indexFile.isEmpty()) {
            for (String filename : stageRm.indexFile.keySet()) {
                indexFile.remove(filename);
            }
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
