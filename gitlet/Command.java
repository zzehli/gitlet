package gitlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gitlet.Gitfile.*;

public class Command {

    /**
     * init
     * set up folders and set the zero commit
     */
    static void init() {
        Gitfile.setDirectory();
        Objects initCommit = new Objects();
        Gitfile.writeGitObject(initCommit);
        //Gitfile.updateLog("master", initCommit);
    }

    /**
     * add [filename]
     * add the file to the staging area
     * @param fileToAdd filename of the new file to be added
     */
    static void add(String fileToAdd) {
        //staging area is the Tree class under Objects and corresponding index file
        //first read the file content and make a blob
        File newFile = Utils.join(fileToAdd);
        Objects blob = new Objects(Utils.readContentsAsString(newFile), fileToAdd);
        //write the blob to disk
        Gitfile.writeGitObject(blob);
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
        Objects currHeads = Gitfile.getCurrentCommit();
        //compare and override old version with staged/removed
        currHeads.updateDictDiff(stageEntries, stageRm);
        currHeads.makeCommit(msg);
        //write commit Object to file
        Gitfile.writeGitObject(currHeads);
        //Gitfile.updateLog("master", "000");
        //wipe the INDEX and RM file
        stageEntries.indexFile.clear();
        stageRm.indexFile.clear();
        Utils.writeObject(INDEX_RM,stageEntries);
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
        Objects curr = Gitfile.getCurrentCommit();
        //TODO Exception in thread "main" java.lang.IllegalArgumentException: gitlet.Objects; local class incompatible: stream classdesc serialVersionUID = -2131821998149161747, local class serialVersionUID = -8555438768840914720
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
        //get a list of branches
        List<String> branches = Utils.plainFilenamesIn(LOCAL_HEAD);
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
        File branchLog = Utils.join(LOG_REF, branch);
        //Gitfile.updateLog(branch, "000");
        updateBranchHead(branch, getHead());
    }


    /**
     * Checkout out a past version of a file in the given commit ID
     * @param commitHash hash of the desired commit
     * @param file filename to be retrieved
     */
    static void checkoutPastFile(String commitHash, String file) {
        //check untracked files first
        String currBranch = Utils.readContentsAsString(HEAD);
        //get a list of past commit
        List<String> pastCommits = Gitfile.pastCommits(currBranch);
        File rev = null;
        for (String i : pastCommits) {
            if (i.equals(commitHash)) {
                rev = Gitfile.getObjectsAsFile(commitHash);
            }
        }
        if (rev == null) {
           Utils.exitWithError("No commit with that id exists");
        }
        Objects commit = Utils.readObject(rev, Objects.class);
        if (commit.indexFile.containsKey(file) == false) {
            Utils.exitWithError("Files does not exist in that commit");
        }
        Gitindex ver = commit.indexFile.get(file);
        //create a new file with the same name
        File cwdVer = Utils.join(file);
        Gitfile.updateRepoFile(cwdVer, ver.getSha1Hash());
    }

    /**
     * checkout a file from the most recent commit
     * @param fileName cwd name of the file
     */
    static void checkoutHeadFile(String fileName) {
        //check untracked file
        List<String> cwd = Utils.plainFilenamesIn(CWD);
        Objects commit = Gitfile.getCurrentCommit();
        if (!commit.indexFile.containsKey(fileName)) {
            Utils.exitWithError("File does not exist in that commit.");
        }
        String hash = commit.indexFile.get(fileName).getSha1Hash();
        File cwdVer = Utils.join(fileName);
        Gitfile.updateRepoFile(cwdVer, hash);
    }

    static void checkoutBranch(String branchName) {
        //check untracked file
        if (Gitfile.currentBranch().equals(branchName)) {
            Utils.exitWithError("No need to checkout the current branch.");
        }
        List<String> branches = Utils.plainFilenamesIn(LOCAL_HEAD);
        if (!branches.contains(branchName)) {
            Utils.exitWithError("No such branch exists.");
        }
        //update HEAD to new branch
        Gitfile.writeHead(branchName);
        //get Branch head commit
        File commitFile = Gitfile.getObjectsAsFile(Gitfile.getBranchHead(branchName));
        Objects commit = Utils.readObject(commitFile, Objects.class);
        //put each of the file in the cwd
        for (Map.Entry<String, Gitindex>i : commit.indexFile.entrySet()) {
            File entry = Utils.join(i.getKey());
            Gitfile.updateRepoFile(entry, i.getValue().getSha1Hash());
        }
        //clear staging area
        Gitfile.clearStage();
    }







}
