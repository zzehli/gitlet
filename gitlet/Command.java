package gitlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

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
    }

    /**
     * add [filename]
     * add the file to the staging area
     *
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
        //wipe the INDEX and RM file
        stageEntries.indexFile.clear();
        stageRm.indexFile.clear();
        Utils.writeObject(INDEX_RM, stageEntries);
        Utils.writeObject(INDEX, stageEntries);
    }

    static void rm(String file) {
        File newFile = Utils.join(file);
        //TODO check if the file exists: what if the file doesn't exist in all locations
        Objects blob = new Objects(Utils.readContentsAsString(newFile), file);
        //perform removal
        if (!Gitfile.updateIndexRemoval(blob)) {
            Utils.exitWithError("No reason to remove the file.");
        }
    }

    static void log() {
        Objects curr = Gitfile.getCurrentCommit();
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
        Objects staged = Utils.readObject(INDEX, Objects.class);

        Objects unstaged = Utils.readObject(INDEX_RM, Objects.class);
        //get a list of branches
        List<String> branches = Utils.plainFilenamesIn(LOCAL_HEAD);
        List<String> untracked = Gitfile.untrackedFiles(Gitfile.getCurrentCommit(), staged, unstaged);
        ArrayList<String> stage = new ArrayList<>(
                staged.indexFile.keySet());
        ArrayList<String> unstage = new ArrayList<>(
                unstaged.indexFile.keySet());
        Collections.sort(stage);
        Collections.sort(unstage);
        branches.sort(null);
        String content = "=== Branches ===\n";

        String currHead = Utils.readContentsAsString(HEAD);
        for (String i : branches) {
            if (currHead.equals(i)) {
                i = "*" + i;
            }
            content += i + "\n";
        }
        content += "\n=== Staged Files ===\n";
        for (String i : stage) {
            content += i + "\n";
        }
        content += "\n=== Removed Files ===\n";
        for (String i : unstage) {
            content += i + "\n";
        }
        //TODO modified not staged
        content += "\n=== Modifications Not Staged For Commit ===\n";
        content += "\n=== Untracked Files ===\n";
        for (String i : untracked) {
            content += i + "\n";
        }
        System.out.println(content);
//        System.out.println(Utils.plainFilenamesIn(CWD));
    }


    static void globalLog() {
        //call log() on each of the heads
        //use a queue to store merge branch
    }

    static void branch(String branch) {
        updateBranchHead(branch, getHead());
    }


    /**
     * Checkout out a past version of a file in the given commit ID
     *
     * @param commitHash hash of the desired commit
     * @param file       filename to be retrieved
     */
    static void checkoutPastFile(String commitHash, String file) {
        //check untracked files first
        String currBranch = Utils.readContentsAsString(HEAD);
        //get a list of past commit
        List<String> pastCommits = Gitfile.pastCommits(currBranch);
        File rev = null;

        if (pastCommits.contains(commitHash)) {
            rev = Gitfile.getObjectsAsFile(commitHash);
        }
        if (rev == null) {
            Utils.exitWithError("No commit with that id exists.");
        }
        Objects commit = Utils.readObject(rev, Objects.class);
        if (commit.indexFile.containsKey(file) == false) {
            Utils.exitWithError("File does not exist in that commit");
        }
        Gitindex ver = commit.indexFile.get(file);
        //create a new file with the same name
        File cwdVer = Utils.join(file);
        Gitfile.updateRepoFile(cwdVer, ver.getSha1Hash());
    }

    /**
     * checkout a file from the most recent commit
     *
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
        for (Map.Entry<String, Gitindex> i : commit.indexFile.entrySet()) {
            File entry = Utils.join(i.getKey());
            Gitfile.updateRepoFile(entry, i.getValue().getSha1Hash());
        }
        Objects currCommit = Gitfile.getCurrentCommit();
        //delete untracked file in checkout branch
        Objects stage = Utils.readObject(INDEX, Objects.class);
        Objects unstage = Utils.readObject(INDEX_RM, Objects.class);
        LinkedList<String> list = Gitfile.untrackedFiles(currCommit, stage, unstage);
        for (String i : list) {
            Utils.restrictedDelete(i);
        }
        //clear staging area
        Gitfile.clearStage();
    }

    static void mergeCheck(String branchName) {

    }

    static void merge(String branchName) {
        String givenHash = Gitfile.getBranchHead(branchName);
        String currHash = Gitfile.getHead();

        List<String> currParents = Gitfile.pastCommits(Gitfile.currentBranch());
        List<String> givenParents = Gitfile.pastCommits(branchName);
//        String content = "";
//        String other = "";
//        for (String i : currParents)
//        {
//            content += i + " ";
//        }
//        System.out.println(content);
//        for (String i : givenParents)
//        {
//            other += i + " ";
//        }
//        System.out.println(other);
        //case1 merge branch is ancestor of current
        if (givenParents.contains(currHash)) {
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        //case2 merge branch is the children of current
        else if (currParents.contains(givenHash)) {
            checkoutBranch(branchName);
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        //case3 two branches have split lineage
        String splitPoint = Gitfile.splitPoint(Gitfile.currentBranch(), givenParents);

//        System.out.println(splitPoint);
        Objects splitCommit = Gitfile.getObjectsfromHash(splitPoint);
        Objects givenCommit = Gitfile.getObjectsfromHash(givenHash);
        Objects currCommit = Gitfile.getCurrentCommit();
        LinkedList<String> mergConf = new LinkedList<>();

        Objects newCommit = new Objects("index");
        newCommit.indexFile.clear();

        for (String file : currCommit.indexFile.keySet()) {
            String currVer = currCommit.indexFile.get(file).getSha1Hash();
            if (givenCommit.indexFile.containsKey(file)) {
                //file contains in both branch and in splitpoint
                String givenVer = givenCommit.indexFile.get(file).getSha1Hash();
                if (splitCommit.indexFile.containsKey(file)) {
                    //compare versions
                    String splitVer = splitCommit.indexFile.get(file).getSha1Hash();
                    //only modified in current
                    if (splitVer.equals(givenVer) && !splitVer.equals(currVer))
                        newCommit.indexFile.put(file, splitCommit.indexFile.get(file));
                        //only modified in given
                    else if (splitVer.equals(currVer) && !currVer.equals(givenVer)) {
                        writeStagedToIndex(givenCommit.indexFile.get(file).getSha1Hash(), file);
                        File cwdVer = Utils.join(file);
                        Gitfile.updateRepoFile(cwdVer,
                                givenCommit.indexFile.get(file).getSha1Hash());
                    } else if (!splitVer.equals(currVer) && !currVer.equals(givenVer))
                        mergConf.add(file);
                        //not modified in both; modified in the same way
                    else
                        newCommit.indexFile.put(file, currCommit.indexFile.get(file));
                }
                //file in both branch but absent in splitpoint
                else {
                    //compare versions
                    if (!givenVer.equals(currVer))
                        mergConf.add(file);
                    else
                        newCommit.indexFile.put(file, currCommit.indexFile.get(file));
                }
            }
            //file in current branch not in given branch
            else {
                //if in split branch
                if (splitCommit.indexFile.containsKey(file)) {
                    String splitVer = splitCommit.indexFile.get(file).getSha1Hash();
                    //modified
                    if (!splitVer.equals(currVer))
                        mergConf.add(file);
                        //remove unmodified
                    else
                        Utils.restrictedDelete(file);
                }
                //no in split, only in current
                else
                    newCommit.indexFile.put(file, currCommit.indexFile.get(file));
            }
        }
        //not in current branch
        for (String file : givenCommit.indexFile.keySet()) {
            if (!currCommit.indexFile.containsKey(file)) {
                String givenVer = givenCommit.indexFile.get(file).getSha1Hash();
                //not in current, present in split and given
                if (splitCommit.indexFile.containsKey(file)) {
                    String splitVer = splitCommit.indexFile.get(file).getSha1Hash();
                    if (!splitVer.equals(givenVer))
                        mergConf.add(file);
                    //else same version: remain absent
                }
                //present only in given
                else {
                    writeStagedToIndex(givenCommit.indexFile.get(file).getSha1Hash(),
                            file);
                    File cwdVer = Utils.join(file);
                    Gitfile.updateRepoFile(cwdVer,
                            givenCommit.indexFile.get(file).getSha1Hash());
                }
            }
        }
        //System.out.println("CWD"+ Utils.plainFilenamesIn(CWD));
        //if (no conflict)
//        if (mergConf.isEmpty()) {
//            //TODO merge commands
//        } else
            //else print merge conflicts, record given_hash to MERGE_HEAD
        printMergeConf(mergConf, currCommit, givenCommit);
        mergeCommit(newCommit, Gitfile.currentBranch() , branchName, givenHash);
    }

    static void mergeCommit(Objects commit, String currBranch, String givenBranch,
                            String givenHash)
    {
        //read from the stage, deserialize hashmap
        Objects stageEntries = Utils.readObject(INDEX, Objects.class);
        for (Map.Entry<String, Gitindex> i : stageEntries.indexFile.entrySet())
        {
            commit.indexFile.put(i.getKey(), i.getValue());
        }
        //commit.printDict();
        //write commit Object to file
        commit.makeMergeCommit("Merged " + givenBranch + " into "
                + currBranch + ".", givenHash);
        //wipe the INDEX and RM file
        stageEntries.indexFile.clear();
        //TODO optimization writeStage open and close every time it is called, could take the object and write at then end in merge()
        Utils.writeObject(INDEX, stageEntries);
        Gitfile.writeGitObject(commit);
    }

    //merge conflict print out
    static void printMergeConf(LinkedList<String> fileList, Objects curr, Objects given) {
        String output = "Encountered a merge conflict.\n";

        //checkout blobs in both versions, handle deleted file
        for (String i : fileList) {
            String head = "";
            String merge = "";
            String content = "";

            Objects currblob = null;
            if (curr.indexFile.containsKey(i)) {
                currblob = Gitfile.getObjectsfromHash(
                        curr.indexFile.get(i).getSha1Hash());
                head = currblob.getContent();
            } else {
                currblob = new Objects(content, i);
            }
            if (given.indexFile.containsKey(i)) {
                Objects blob = Gitfile.getObjectsfromHash(
                        given.indexFile.get(i).getSha1Hash());
                merge = blob.getContent();
            }
            content += "<<<<<<< HEAD\n" + head + "=======\n" + merge + ">>>>>>>\n";
            currblob.setContent(content);
            //write to cwd
            Utils.writeContents(Utils.join(i), content);
            //write to objects
            writeGitObject(currblob);
            //stage
            writeStagedToIndex(curr.indexFile.get(i).getSha1Hash(), currblob.getCwdName());
//            System.out.println(Utils.readContentsAsString(Utils.join(i)));
        }


    System.out.println(output );
    }
}















