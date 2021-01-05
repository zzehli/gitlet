package gitlet;
import jdk.jshell.execution.Util;

import java.io.File;
import java.util.*;

public class Gitfile {
    static final File OBJECTS = Utils.join(".gitlet","objects");
    static final File INDEX = Utils.join(".gitlet", "INDEX");
    static final File INDEX_RM = Utils.join(".gitlet", "INDEX_RM");
    static final File LOCAL_HEAD = Utils.join(".gitlet", "refs", "heads");
    static final File HEAD = Utils.join(".gitlet", "HEAD");
    static final File BRANCH = Utils.join(".gitlet", "refs", "heads");
    static final File CWD = Utils.join(".");
    static final String ST_RM = "REMOTE";

    /**
     * set up dog gitlet folder and subfolders
     */
    static void setDirectory() {
        OBJECTS.mkdirs();
        //this is for branching
        LOCAL_HEAD.mkdirs();
        Objects index = new Objects("index");
        Utils.writeObject(INDEX_RM, index);
        Objects fileList = new Objects("index");
        Utils.writeObject(INDEX, fileList);
        Utils.writeContents(HEAD, "master");
    }

    /**
     * write the commit object to .gitlet/objects/
     *
     * @param GitObject
     */
    static String writeGitObject(Objects GitObject) {
        String hash = Utils.hash(GitObject);
        Utils.join(OBJECTS,
                getHashHead(hash)).mkdir();
        java.io.File file = getObjectsAsFile(hash);
        if (GitObject.getType().equals("commit")) {
            //point head pointer to current head
            String currBranch = Utils.readContentsAsString(HEAD);
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
        return hash;
    }

    /**
     * update the branch name of the head pointer in .gitlet/HEAD file
     *
     * @param branch name of the branch
     */
    static void writeHead(String branch) {
        Utils.writeContents(HEAD, branch);
    }

    /**
     * get current HEAD commit hash as a string
     *
     * @return hash of the current commit/HEAD
     */
    static String getHead() {
        String curr = Utils.readContentsAsString(HEAD);
        File branchHead = Utils.join(LOCAL_HEAD, curr);
        return Utils.readContentsAsString(branchHead);
    }

    /**
     * write git INDEX file to record staged file
     *
     * @param hash     sha1 of the staged file
     * @param filename filename of the stage file in the CWD
     * @return true if write to index
     */
    static boolean writeStagedToIndex(String hash, String filename) {
        Objects fileList;
        fileList = Utils.readObject(INDEX, Objects.class);
        if (fileList.indexFile.containsKey(filename)) {
            //if dictionary contains the same file, compare hashcode
            //same as last commit/staged already
            if (hash == fileList.indexFile.get(filename).getSha1Hash()) {
                //this version and previous version in staging area are the same
                //don't stage
                return false;
            }
        }
        //write update to file
        Gitindex update = new Gitindex(hash, filename);
        fileList.indexFile.put(filename, update);

        //fileList.printDict();
        Utils.writeObject(INDEX, fileList);
        return true;
    }

    /**
     * helper function
     * perform removal action, either update INDEX file or create entry in
     * INDEX_RM file and perform deletion
     *
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
     *
     * @param hash hashcode of the Objects
     * @return corresponding file, might not exist
     */
    static File getObjectsAsFile(String hash) {
        java.io.File file = Utils.join(OBJECTS, getHashHead(hash), getHashBody(hash));
        return file;
    }

    /**
     * Given hash of an git object, return the object from repo
     *
     * @param hash
     * @return Objects class of the git object
     */
    static Objects getObjectsfromHash(String hash) {
        File obj = getObjectsAsFile(hash);
        return Utils.readObject(obj, Objects.class);
    }

    /**
     * Getter for hash code, used for naming things
     *
     * @return
     */
    static String getHashHead(String hash) {
        return hash.substring(0, 2);
    }

    static String getHashBody(String hash) {
        return hash.substring(2);
    }

    /**
     * Get current commit as Objects
     *
     * @return
     */
    static Objects getCurrentCommit() {
        File commit = getObjectsAsFile(Gitfile.getHead());
        return Utils.readObject(commit, Objects.class);
    }

    /**
     * get the current hash of the given branch
     *
     * @param branchName
     * @return
     */
    static String getBranchHead(String branchName) {
        File head = Utils.join(LOCAL_HEAD, branchName);
        return Utils.readContentsAsString(head);
    }

    /**
     * return the name (not hash) of the current branch
     *
     * @return
     */
    static String currentBranch() {
        return Utils.readContentsAsString(HEAD);
    }

    /**
     * write local branch head in .gitlet/refs/heads
     *
     * @param hash   hash of newest commit
     * @param branch branch to update
     */
    static void updateBranchHead(String branch, String hash) {
        File branchHead = Utils.join(".gitlet", "refs", "heads", branch);
        Utils.writeContents(branchHead, hash);
    }

    /**
     * Get the ancestor commits hash as a list (including merged in branches)
     *
     * @param currBranch branch to be retrieved
     * @return list of commit hash
     */
    static List<String> pastCommits(String currBranch) {
        //get head commit of this branch
        String hash = Utils.readContentsAsString(Utils.join(BRANCH, currBranch));
        //TODO might be a problem to return List
        List<String> ret = new ArrayList<>();
        ret.add(hash);
        LinkedList<String> queue = new LinkedList<>();
        queue.add(hash);
        while (!queue.isEmpty()) {
            String commitHash = queue.pop();
            Objects currCommit = Gitfile.getObjectsfromHash(commitHash);
            if (!currCommit.getSecondParent().equals("")) {
                ret.add(currCommit.getSecondParent());
                queue.add(currCommit.getSecondParent());
            }
            if (!currCommit.getParentHash().equals("")) {
                ret.add(currCommit.getParentHash());
                queue.add(currCommit.getParentHash());
            }
        }
        return ret;
    }

    /**
     * helper function for merge
     * Given two branches, get the split point of the two. Implemented by making
     * ancestors of two branches as a sorted map
     * @param currBranch
     * @param mergingParen
     * @return hash of the splitpoint
     */
    static String splitPoint(String currBranch, List<String> mergingParen) {
        TreeMap<Integer, String> anc = new TreeMap<>();
        //traverse through current branch, record common ancestor if present in parenII
        String hash = Utils.readContentsAsString(Utils.join(BRANCH, currBranch));
        LinkedList<String> queue = new LinkedList<>();
        int i = 0;
        queue.add(hash);
        while (!queue.isEmpty()) {
            String commitHash = queue.pop();
            Objects currCommit = Gitfile.getObjectsfromHash(commitHash);
            if (!currCommit.getSecondParent().equals("")) {
                queue.add(currCommit.getSecondParent());
                if (mergingParen.contains(currCommit.getSecondParent())) {
                    anc.put(i, currCommit.getSecondParent());
                }
            }
            if (!currCommit.getParentHash().equals("")) {
                queue.add(currCommit.getParentHash());
                if (mergingParen.contains(currCommit.getParentHash())) {
                    anc.put(i, currCommit.getParentHash());
                }
            }
            ++i;
        }
    return anc.firstEntry().getValue();
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
    static void clearStage(Objects stageEntries, Objects stageRM) {
        stageEntries.indexFile.clear();
        stageRM.indexFile.clear();
        Utils.writeObject(INDEX, stageEntries);
        Utils.writeObject(INDEX_RM, stageRM);
    }

    /**
     * compare cwd and commit and output a list of untracked files
     * @param commit
     * @return
     */
    static LinkedList<String> untrackedFiles(Objects commit,
                                              Objects staged,
                                              Objects deleted) {
        LinkedList<String> list = new LinkedList<>();
        for (String file : Utils.plainFilenamesIn(CWD)) {
            if (!commit.indexFile.containsKey(file)) {
                list.add(file);
            }
        }
        //must use itr instead of for range loop to remove
        for (Iterator<String> it= list.iterator(); it.hasNext(); ) {
            String file = it.next();
            if (staged.indexFile.containsKey(file)
                    || deleted.indexFile.containsKey(file)) {
                it.remove();
            }
        }
        return list;
    }

    /**
     * survey the current working directory to output a list of untracked but
     * modified or deleted but not staged files for log command
     * @param commit current commit to be compared
     * @param staged staged files
     * @param unstage staged for removal files
     * @return list of modified files
     */
    static LinkedList<String> modifiedFiles(Objects commit,
                                            Objects staged,
                                            Objects unstage) {
        LinkedList<String> modified = new LinkedList<>();
        for (String i : staged.indexFile.keySet()) {
            System.out.println(i);
            String sha1 = staged.indexFile.get(i).getSha1Hash();
            String content = Utils.readContentsAsString(Utils.join(i));
            if (!Utils.hash(new Objects(content, i)).equals(sha1)) {
                modified.add(i + " (modified)");
            }
        }
        for (String i : commit.indexFile.keySet()) {
            if (staged.indexFile.containsKey(i))
                continue;
            String sha1 = commit.indexFile.get(i).getSha1Hash();
            File cwdVer = Utils.join(i);
            if (!cwdVer.exists()) {
                modified.add(i + " (deleted)");
                continue;
            }
            String content = Utils.readContentsAsString(cwdVer);
            if (!Utils.hash(new Objects(content, i)).equals(sha1))
                modified.add(i + " (modified)");
        }
        for (String i : unstage.indexFile.keySet()) {
            if (modified.contains(i + " (deleted)"))
                modified.remove(i + " (deleted)");
        }
    return modified;
    }
}
