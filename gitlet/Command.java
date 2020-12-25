package gitlet;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public class Command {

    /**
     * init
     * set up folders and set the zero commit
     */
    static void init() {
        Gitfile.setDirectory();
        Objects initCommit = new Objects();
        Gitfile.writeCommit(initCommit);
    }

    /**
     * add [filename]
     * add the file to the staging area
     * @param file file to be added
     */
    static void add(String file) {

    }

    /**
     * commit
     * commit all files in the staging area
     */
    static void commit() {

    }
}
