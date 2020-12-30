package gitlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static gitlet.Gitfile.*;

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
        Objects stageEntries = Utils.readObject(INDEX, Objects.class);
        Objects stageRm = Utils.readObject(INDEX_RM, Objects.class);
        if (stageEntries.indexFile.isEmpty() && stageRm.indexFile.isEmpty()) {
            Utils.exitWithError("No changes added to the commit");
        }
        //stageEntries.printDict();
        //need to access previous commit to compare versions
        File head = Gitfile.getObjectsAsFile(Gitfile.getHead());
        Objects currHeads = Utils.readObject(head, Objects.class);
        //compare and override old version with staged/removed
        currHeads.updateDictDiff(stageEntries, stageRm);
        currHeads.makeCommit(msg);
        //write commit Object to file
        Gitfile.writeObject(currHeads);
        //wipe the INDEX file
        stageEntries.indexFile.clear();
        stageRm.indexFile.clear();
        Utils.writeObject(INDEX_RM,stageEntries);
        //wipe RM file
        Utils.writeObject(INDEX, stageEntries);
    }

    static void rm(String file) {
        File newFile = new File(file);
        //TODO check if the file exists: what if the file doesn't exist in all locations
        Objects blob = new Objects(Utils.readContentsAsString(newFile), file);
        //perform removal
        if (!Gitfile.updateIndexRemoval(blob)) {
            Utils.exitWithError("No reason to remove the file.");
        }
    }

    static void log() {
        File head = Gitfile.getObjectsAsFile(Gitfile.getHead());
        //TODO Exception in thread "main" java.lang.IllegalArgumentException: gitlet.Objects; local class incompatible: stream classdesc serialVersionUID = -2131821998149161747, local class serialVersionUID = -8555438768840914720
        Objects curr = Utils.readObject(head, Objects.class);
        String content = "";
        String currName = Gitfile.getHead();
        //TODO handle merge cases
        while (!curr.getParentHash().equals("")) {
            content += "=== \n" + "commit " + currName + "\n"
                    + "Date: " + curr.getTime() + "\n"
                    + curr.getMsg() + "\n\n";
            currName = curr.getParentHash();
            curr = Utils.readObject(Gitfile.getObjectsAsFile(curr.getParentHash()), Objects.class);
        }
        content += "=== \n" + "commit " + currName + "\n"
                + "Date: " + curr.getTime() + "\n"
                + curr.getMsg() + "\n\n";
        System.out.println(content);
    }

    static void status() {
        String[] staged = Gitfile.collectCwdNamesfromIndex(INDEX);
        String[] unstaged = Gitfile.collectCwdNamesfromIndex(INDEX_RM);
        List<String> branches = Utils.plainFilenamesIn(
                Utils.join(".gitlet", "refs", "heads"));
        branches.sort(null);
        String content = "=== Braches ===\n";

        String currHead = Utils.readContentsAsString(HEAD);
        for (String i : branches) {
            if (currHead.equals(i)) {
                i = "*" + i;
            }
            content += i + "\n";
        }
        content += "\n === Staged Files ===\n";
        for (String i : staged) {
            content += i + "\n";
        }
        content += "\n === Removed Files ===\n";
        for (String i : unstaged) {
            content += i + "\n";
        }
        //TODO handle merge case
        System.out.println(content);
    }


    static void globalLog() {
        //call log() on each of the heads
        //use a queue to store merge branch
    }

    static void branch(String branch) {
        updateBranchHead(branch, getHead());
    }










}
