package gitlet;
import jdk.jshell.execution.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Gitfile{
    static final File OBJECTS = new File(".gitlet/objects");
    static final File INDEX = Utils.join(".gitlet", "INDEX");
    static final File INDEX_RM = Utils.join(".gitlet", "INDEX_RM");
    static final File LOCAL_HEAD = Utils.join(".gitlet", "refs", "heads");
    static final File HEAD = Utils.join(".gitlet", "HEAD");
    static final File BRANCH = Utils.join(".gitlet", "refs", "heads");
    static final File LOG_REF = Utils.join(".gitlet", "logs", "refs", "heads");
    static final File CWD = Utils.join(".");
    static final String ST_RM = "REMOTE";

    /**
     * set up dog gitlet folder and subfolders
     */
    static void setDirectory() {
        OBJECTS.mkdirs();
        //this is for branching
        LOCAL_HEAD.mkdirs();
        LOG_REF.mkdirs();
        Objects index = new Objects("index");
        Utils.writeObject(INDEX_RM, index);
        Utils.writeContents(HEAD, "master");
    }

    /**
     * write the commit object to .gitlet/objects/
     * @param GitObject
     */
    static void writeGitObject(Objects GitObject) {
        String hash = Utils.hash(GitObject);
        Utils.join(OBJECTS,
                getHashHead(hash)).mkdir();
        java.io.File file = getObjectsAsFile(hash);
        if (GitObject.getType().equals("commit")) {
            //point head pointer to current head
            String currBranch = Utils.readContentsAsString(HEAD);
            System.out.println(currBranch);
            writeHead(currBranch);
            updateBranchHead(currBranch, hash);
            Utils.writeObject(file, GitObject);
        }
        //handle blob
        else {
            if (writeStagedToIndex(hash, GitObject.getCwdName())) {
                //if need to stage, store staged file to objects folder
                Utils.writeObject(file, GitObject);
            }
        }
    }

    /**
     * update the branch name of the head pointer in .gitlet/HEAD file
     * @param branch name of the branch
     */
    static void writeHead(String branch) {
        Utils.writeContents(HEAD, branch);
    }

    /**
     * get current HEAD commit hash as a string
     * @return hash of the current commit/HEAD
     */
    static String getHead() {
        String curr = Utils.readContentsAsString(HEAD);
        File branchHead = Utils.join(LOCAL_HEAD, curr);
        return Utils.readContentsAsString(branchHead);
    }

    /**
     * write git INDEX file to record staged file
     * @param hash sha1 of the staged file
     * @param filename filename of the stage file in the CWD
     * @return true if write to index
     */
    static boolean writeStagedToIndex(String hash, String filename) {
        Objects fileList;
        if (INDEX.exists()) {
            fileList = Utils.readObject(INDEX, Objects.class);
            if (fileList.indexFile.containsKey(filename)) {
                //if dictionary contains the same file, compare hashcode
                //same as last commit/staged already
                if (hash == fileList.indexFile.get(filename).getSha1Hash()) {
                    //this version and previous version in staging area are the same
                    //don't stage
                    return false;
                }
                //TODO check for commited cases, must look for commit folder;
                //case: commited, modified, added again (replace commit entry in INDEX), need to remove from INDEX

            }
            //write update to file
            Gitindex update = new Gitindex(hash,filename);
            fileList.indexFile.put(filename, update);

            //fileList.printDict();
        } else {
            //if INDEX doesn't exist, put the entry as the first index
            fileList = new Objects("index");
            fileList.putEntryToDict(hash, filename);
        }

        Utils.writeObject(INDEX, fileList);
    return true;
    }

    /**
     * helper function
     * perform removal action, either update INDEX file or create entry in
     * INDEX_RM file and perform deletion
     * @param blobRmv file to be removed from gitlet system
     * @return false if no action performed
     */
    static boolean updateIndexRemoval(Objects blobRmv) {
        String hashCode = Utils.hash(blobRmv);
        //read from staging area
        Objects stageEntries = Utils.readObject(INDEX, Objects.class);
        //read from current commit
        File head = Gitfile.getObjectsAsFile(Gitfile.getHead());
        Objects currHeads = Utils.readObject(head, Objects.class);
        if (!stageEntries.indexFile.isEmpty()
                && stageEntries.indexFile.containsKey(blobRmv.getCwdName())) {
            //Utils.restrictedDelete(file);
            stageEntries.indexFile.remove(blobRmv.getCwdName());
            Utils.writeObject(INDEX, stageEntries);
            return true;
        } else if (!currHeads.indexFile.isEmpty()
                && currHeads.indexFile.containsKey(blobRmv.getCwdName())) {
            //if current commit contain the file and stage doesn't, write it to INDEX_RM
            //delete from repo
            Utils.restrictedDelete(new File(blobRmv.getCwdName()));
            //create INDEX object for writing
            Objects removalStaged;
            //write RM file
            //read from stage remove
            removalStaged = Utils.readObject(INDEX_RM, Objects.class);
            if (removalStaged == null) {
                removalStaged = new Objects("index");
            }
            Gitindex entry = new Gitindex(hashCode, blobRmv.getCwdName());
            removalStaged.indexFile.put(blobRmv.getCwdName(), entry);
            Utils.writeObject(INDEX_RM, removalStaged);
            return true;
        }
        return false;
    }

    /**
     * Helper function to get a Blob/Commit object with hash
     * @param hash hashcode of the Objects
     * @return corresponding file, might not exist
     */
    static File getObjectsAsFile(String hash) {
        java.io.File file = Utils.join(OBJECTS, getHashHead(hash), getHashBody(hash));
        return file;
    }

    /**
     * Getter for hash code, used for naming things
     * @return
     */
    static String getHashHead(String hash) {
        return hash.substring(0, 2);
    }

    static String getHashBody(String hash) {
        return hash.substring(2);
    }

    /**
     * Given a file of Objects class, read its Hash Map and return a sorted list
     * of keys(cwd names) in the file index
     * @param index commit/index file contain hash map
     * @return sorted list of filename strings
     */
    static String[] collectCwdNamesfromIndex(File index) {
        Objects list = Utils.readObject(index, Objects.class);
        String[] output = new String[list.indexFile.size()];
        int i = 0;
        for (String e : list.indexFile.keySet()) {
            output[i++] = e;
        }
        Arrays.sort(output);
        return output;
    }

    /**
     * Get current commit as Objects
     * @return
     */
    static Objects getCurrentCommit() {
        File commit = getObjectsAsFile(Gitfile.getHead());
        return Utils.readObject(commit, Objects.class);
    }
    /**
     * get the current hash of the given branch
     * @param branchName
     * @return
     */
    static String getBranchHead(String branchName) {
        File head = Utils.join(LOCAL_HEAD, currentBranch());
        return Utils.readContentsAsString(head);
    }

    /**
     * return the name (not hash) of the current branch
     * @return
     */
    static String currentBranch() {
        return Utils.readContentsAsString(HEAD);
    }

    /**
     * write local branch head in .gitlet/refs/heads
     * @param hash hash of newest commit
     * @param branch branch to update
     */
    static void updateBranchHead(String branch, String hash) {
        File branchHead = Utils.join(".gitlet", "refs", "heads", branch);
        Utils.writeContents(branchHead, hash);
    }

    /**
     * Get the ancestor commit hash as a list of the given branch
     * @param currBranch branch to be retrieved
     * @return list of commit hash
     */
    static List<String> pastCommits(String currBranch) {
        //get head commit of this branch
        String hash = Utils.readContentsAsString(Utils.join(BRANCH, currBranch));
        Objects currCommit = Utils.readObject(Gitfile.getObjectsAsFile(hash), Objects.class);
        if (!currCommit.getType().equals("commit")) {
            Utils.exitWithError("Not a commit type");
        }
        List<String> ret = new LinkedList<>();
        String commitHash = hash;
        while (!currCommit.getParentHash().equals("")) {
            ret.add(commitHash);
            commitHash = currCommit.getParentHash();
            currCommit = Utils.readObject(Gitfile.getObjectsAsFile(currCommit.getParentHash()), Objects.class);
        }
        return ret;
    }

    /**
     * replace/create cwd file with the repo version with the given blobHash
     * @param cwdNew cwd file to be written
     * @param blobHash hash of the blob in the repo
     */
    static void updateRepoFile(File cwdNew, String blobHash) {
        File repo = Gitfile.getObjectsAsFile(blobHash);
        String content = Utils.readObject(repo, Objects.class).getContent();
        Utils.writeContents(cwdNew, content);
    }

    /**
     * clear staging area: INDEX and INDEX_RM
     */
    static void clearStage() {
        Objects stageEntries = Utils.readObject(INDEX, Objects.class);
        Objects stageRM = Utils.readObject(INDEX_RM, Objects.class);
        stageEntries.indexFile.clear();
        stageRM.indexFile.clear();
        Utils.writeObject(INDEX, stageEntries);
        Utils.writeObject(INDEX_RM, stageRM);
    }
}
