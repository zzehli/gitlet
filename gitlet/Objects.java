package gitlet;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    /**
     * Copy constructor for rebase; copy the blob index from given git commit and
     * make new date
     * @param other
     */
    public Objects(Objects other) {
        time = Utils.timeStamp();
        //make a deep copy of the given file
        parentHash = new LinkedList<>();
        for (String i : other.parentHash)
            parentHash.add(i);
        indexFile = new HashMap<>();
        for (Map.Entry<String, Gitindex> entry : other.indexFile.entrySet()) {
            indexFile.put(entry.getKey(), new Gitindex(entry.getValue()));
        }
        this.msg = other.msg;
        type = "commit";
    }

    public String getType() {
        return type;
    }

    public String getCwdName() {
        return CwdName;
    }

    public String getContent() { return content;}

    public void setContent(String file) { content = file; }

    /**
     * replace the first parent
     * @param i
     */
    public void setParentHash(String i) {
        parentHash.set(0, i);
    }

    /**
     * Get the hash of the first parent,
     * later merged in parents are later down the line
     * @return hash of the parent commit
     */
    public String getParentHash() {
        if (parentHash.isEmpty()) {
            return "";
        }
        return parentHash.getFirst();
    }

    public String getSecondParent() {
        String content = "";
        if (parentHash.size() == 2) {
            content = parentHash.get(1);
        }
        return content;
    }

    /**
     * return true if the commit is a merge commit
     * @return
     */
    public boolean isMergeCommit() {
        if (parentHash.size() == 2)
            return true;
        else
            return false;
    }
    /**
     * Return formated parent hash in the cases of merging commit
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
     * make merge commit, need an extra parent head: givenHead
     * @param msg commit message
     * @param givenHead merged in branch head commit hash
     */
    public void makeMergeCommit(String msg, String givenHead) {
        time = Utils.timeStamp();
        parentHash = new LinkedList<>();
        parentHash.add(Gitfile.getHead());
        parentHash.add(givenHead);
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

    /**
     * Update replay branch by comparing to split point and head of given branch
     * replace unmodified blob with modified blobs in the given branch
     * @param splitCommit
     * @param givenCommit
     * @return
     */
    public LinkedList<String> rebaseUpdate(Objects splitCommit, Objects givenCommit) {
        LinkedList<String> update = new LinkedList<>();
        Iterator itr = splitCommit.indexFile.keySet().iterator();
        while(itr.hasNext()) {
            //case1 present in all three, modified in given branch
            String i = (String) itr.next();
            if (!indexFile.containsKey(i))
                continue;
            if (splitCommit.indexFile.containsKey(i) &&
                    givenCommit.indexFile.containsKey(i) &&
                    this.indexFile.get(i).verCompare(splitCommit.indexFile.get(i)) &&
                    !this.indexFile.get(i).verCompare(givenCommit.indexFile.get(i))
                    ) {
                indexFile.put(i, givenCommit.indexFile.get(i));
                Gitfile.updateRepoFile(Utils.join(i), givenCommit.indexFile.get(i).getSha1Hash());
                update.add(i);
            //case2 deleted in given, not modified in current branch
            } else if (splitCommit.indexFile.containsKey(i) &&
                    !givenCommit.indexFile.containsKey(i) &&
                    indexFile.get(i).verCompare(splitCommit.indexFile.get(i))) {
                indexFile.remove(i);
                Utils.restrictedDelete(i);
                update.add(i);
            }
            //case3 modification in current branch takes place later than given
            else if (splitCommit.indexFile.containsKey(i) &&
                    givenCommit.indexFile.containsKey(i) &&
                    !this.indexFile.get(i).verCompare(splitCommit.indexFile.get(i)) &&
                    !this.indexFile.get(i).verCompare(givenCommit.indexFile.get(i)))
            {
                Gitfile.updateRepoFile(Utils.join(i), this.indexFile.get(i).getSha1Hash());
                update.add(i);
            }
        }

        for (String i : givenCommit.indexFile.keySet()){
            //case4 only present in given
            if (!splitCommit.indexFile.containsKey(i) &&
                !indexFile.containsKey(i)) {
                indexFile.put(i, splitCommit.indexFile.get(i));
                Gitfile.updateRepoFile(Utils.join(i), givenCommit.indexFile.get(i).getSha1Hash());
                update.add(i);
            }
        }
        return update;
    }
}
