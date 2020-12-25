package gitlet;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public class Objects implements Serializable {
    private String hash;
    private String type;

    //for commits
    private String msg;
    private String time;
    private List<Objects> tree;
    private String parentHash;
    //private String secondParentHash

    //for blob
    private String content;

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
     * Constructor for commit
     * @param info necessary field of information to be hashed
     */
    public Objects(String ...info) {
    }

    /**
     * Constructor for blob
     * @param content
     */




}
