# gitlet
This is an independant implementation of a mini version of Git. The project skeleton is provided by Berkeley CS61B course
## Define terms:
**working directory**: directory tracked by git/with the dot git folder
## Design

The basic design of gitlet involves two parts, set up git objects and work with branches.

| Class: gitApi | Comments |
| ----- |-----|
| gitApi() | constructor, read the current commit from the file and work from there |
| init() ||
| add(String filename) |create blobs but don't save to file|
| rm (String filename) | unstage files |
| commit(String message) | compare blobs and save new ones to file |
| log () ||
| ... ||
| private String head ||
| private String master ||
| private HashMap<String, String> stage  | from names to hashcode|
| private HashMap<String, String> current_tree | as above |

| Class: object | Comments |
| ----- |-----|
| object(String content, String type) |constructor that create a blob or commit object, gather content for blob and gather blobs for commit|
| String typeOf(String hash)| return wether the given hash refers to blob or commits|
| findParentCommit(String hash) | take the hash of the current commit and find its parent |
| boolean compareBlob() |look up the file in current_tree and compare the hashcode to determine if the version in the tracked file is the same as the current blob|
| makeCommit() | call write commit |
|...||
| private String hash |
| private String serialContent ||
| private String type ||
| private transient HashMap<String, String> blobs |for quick acess, reduce reading time|

| Class: File | Comments |
| ---- | ------|
| writeHead(String loc) | record the current head in the the HEAD file |
| boolean writeBlob(object blob) | create blob file and write the serialized content, note that commits in gitlet combine tree objects as transient field|
| boolean writeCommit(object object) | create commit file in the .git/object folder, then take its blobs, current time and parrent (current head) commit and serialize it before hash; name it as SHA-1 hash  |
| initSetUp() | set up .git folder, .git/object folder, .git/HEAD file, git/index file, git/branches folder |
| object getBlob(String hash) |this is necessary since commits share blobs(?)|
| object getCommit(String hash) ||
| boolean stageFile() | write staged file for later commits; it can be overriden by new add if the same file changed | 

| File getCommit(String hash) | look fore the commit folder with the hash code as file name |
| String checkDotGit() | check if the current directory is initialized by gitlet |

| Class: Utils (ignore provided functions) | Comments |
| ---- | ----- |
| String getTime() | for timestamp objects at the time of adding |
| String getFileName() | for easy access to commits | 


| Class: 
## Development Journal
### Day 1: Dec 20th
Set up the project as an stand-alone repo and started the design process. The decision to extract the project from the CS61B course repo means I need to fix git issues and directory references. I added the submodule to the project folder. Dealing with submodules allow me to practice with the remote repos, which I will probably implement if the project goes well. Another problem was fixing the python test suit that contains the default library directory for the course. In the evening, I spent time reading the project prompt and read materials about git implementations. I am surprised by the number of expositions on this subject matter. There are quite a few git implementations and helpful expositories as well as conference talks. In particular, I figured out an important question: **what actually are the snapshots of the files when you add/commit?** For text files, simply save the whole file as a string, which is in turn saved to `.git/object` folder when commited. This a lot more simpler/unsophisticated than I have imagined. However, this only applies to txt file. This is really not much different from copy and paste. The disk space required for other format would be considerably larger if other file formats are involved. 

The [naive version control workflow](http://git.github.io/git-reference/) provided by the github team illustrate what Github really is
```
cp -R project project.bak
(do it many times, now you need more precise names)
cp -R project project.2010-06-01.bak 
wget http://example.com/project.2010-06-01.zip
unzip project.2010-06-01.zip
cp -R project.2010-06-01 project-my-copy
cd project-my-copy
(change something)
diff project-my-copy project.2010-06-01 > change.patch
(email change.patch)
```

To quote the authors of the Git Pro book: 
> Git stores content in a manner similar to a UNIX filesystem, but a bit simplified. All the content is stored as tree and blob objects, with trees corresponding to UNIX directory entries and blobs corresponding more or less to inodes or file contents. 

### Day 2: Dec 21st
Leart about JAVA file I/O and serialization. At this point, I am more or less ready to start writing the code. I have a fairly good understanding of the structure of git. **The constraints set up by the this project** makes the git implementation very workable. A few important points of implementations are:
* There are three data structures in git: blobs, trees and commits; **gitlet eliminates trees**. Trees are essential subdirectories that are represented by a pointer like structure. In the current directory, instead of a thing called "folder" in the git representation, you have a tree. This allows subdirectories to be defined in a recursive manner. In the following example, the `main` branch contains two subtrees, or two subdirectories, gitlet and testing. Reading the content of the gitlet subtree is equivalent to looking inside the directory. Git essentially coated files and subdirectories with different names, blob and trees. Also, notice the `commit` file that is a submodule.
```
homer@DESKTOP-QC9AG5L:/mnt/c/Users/homer/Documents/project/gitlet (main)$ git cat-file -p main^{tree}
100644 blob ceb14ed82a2daf4dccbb81460c37f3fb975101ac    .gitignore
100644 blob 3a64fa492252241fba74fb34aa03814302aaa288    .gitmodules
100644 blob 85b2b592c018c3bd908679397551813c60762bc3    README.md
040000 tree c47e762feb7f858b3f551fdea5e34635d017e315    gitlet
160000 commit 0969ea0a0d74f14effb77f179da3ad8edfc403c0  library-su20
040000 tree f6514aad3d93a66c3aa2c81f5818418c2e70b900    testing

homer@DESKTOP-QC9AG5L:/mnt/c/Users/homer/Documents/project/gitlet (main)$ git cat-file -p c47e762feb7f858b3f551fdea5e34635d017e315
100644 blob e3c11332b09a80357f92c1cc9edc909ae4c030f9    DumpObj.java
100644 blob 77c6f07b8150b67f73bed61a200fec1d50a52dc7    Dumpable.java
100644 blob 43637d26254c0de09918c5b4093783b8b8930b36    GitletException.java
100644 blob ef0017d98381d2211cba94b543b56fd373809347    Main.java
100644 blob c086bc00ee3a61906cbd21baa3d745639f79ad28    UnitTest.java
100644 blob 4ab9ba66f6b364b973b3cf39d0a1c55ae0d7a67d    Utils.java
```
* File I/O functions are provided by the skeleton file.
* Only support text files, therefore taking snapshots basically means taking the snapshot is very similar to saving the file as a string. There is not much to be puzzled over here about compression etc.

### Day 3: Dec 22nd
An important concept in the project is persistence, since after every run of the git/gitlet command, the program is closed, the results of the command need to be stored to hard drive. Most of the files generated are for the program to use in the future, but some will be available to users such as the files in the working directories commited. Unlike other programs that you assume will run continuously between one command and the next, git only execute one program at a time. Therefore, it is costly to reestablish the working environment when the program starts again, therefore the persistence set up needs to be efficient. And efficiency in persistence comes from how many times the program needs to read from different files. **Therefore how to store internal objects to files(file organization and content of files) and restore them is the central design question.**

Two problems stands out: **how to store the temporal history (`git log` for a list of commits) and the horizontal overview (`git ls-files`, fow which `git status` is a simplified version of)?** These two are maps (in a literal, not programming sense) for both the program and the client to navigate their actions. The horizontal view is in the `.git/index` file through `git ls-files`. My goal tomorrow is to figure out how git access commit history and finish the development doc in the morning and start coding in the afternoon. 

An more straight forward way to implement file system is to create separate folders for seperate commands such as `objects/commits` for commit history and `objects/stage` for staged blobs, etc. However, this implementation will make file navigation difficult to keep track of by the programmer, since files are essentially regarded as command specific. But as the number of commands increase, finding files might become a problem.

Two of the most important reference I use so far are the Pro Git book [chapter 10](https://git-scm.com/book/en/v2/Git-Internals-Git-References) section 2 and 3 and [mini Git implementation walkthrough](https://maryrosecook.com/blog/post/git-from-the-inside-out) by Mary Rose Cook.

### Day 4: Dec 23rd

**The gist of git is in its `objects` folder.** Surprisingly, most of the files in dot git folder are text files and human reable. Among them, objects is the central depository of archived files. It stores the three kinds of git objects with hash code. Each file is a single object: blob, tree or commit. Blobs are the only kind that store changes in working directory file. Each tree contains file info of a directory in the form of a list of hash to blobs. Each commit contains its parent info and a tree info of the tracked directory. Different git command will operates on this file system. It not only stores archieved files, but also saves program internals to be used for later. Other folders and files are there to facilitate and allow easy aacess to certain important objects, such as HEAD, index, branches folder and refs. In particular, `.git/refs` folder and `HEAD` work together to record the position of the tip of the branches(in refs) and where the current head is `HEAD`. This information is strored as hash code and refers to the corresponding file in the object folder.

Below are some important command to navigate the `objects` folder:
`cat-file -p` + hashcode to view object with corresponding name
```
homer@DESKTOP-QC9AG5L:/mnt/c/Users/homer/Documents/project/gitlet (main)$ git log --oneline
da29b8e (HEAD -> main, origin/main, origin/HEAD) update journal
4e020d2 some update README
76e0e72 update journal
9d39baa update dev journal
47bdf43 finished setup
86b3fc1 abort, testing suit requires strict file path
8d7f274 finish submodule set up
c7ae2fd try to fix gitmodules
9d01e69 add project skeleton
cdb46e8 Initial commit

homer@DESKTOP-QC9AG5L:/mnt/c/Users/homer/Documents/project/gitlet (main)$ git cat-file -p da29b8
tree f6559bff9bc1e00929f45b213fc448c52aa796f0
parent 4e020d20fb5376d8c62dadc71c4256d60eedd7b9
author *** <***> 1608705309 -0600
committer *** <***> 1608705309 -0600

update journal
```

Examine the change in file system with the `git commit` command, same as Mary Rose Cook's example:
```
homer@DESKTOP-QC9AG5L:/mnt/c/Users/homer/Documents/project/sample $ tree .git/objects
.git/objects
├── 27
│   └── 4c0052dd5408f8ae2bc8440029ff67d79bc5c3
├── 2e
│   └── 65efe2a145dda7ee51d1741299f848e5bf752e
├── 56
│   └── a6051ca2b02b04ef92d5150c9ef600403cb1de
├── info
└── pack
```
after: commit:
```
homer@DESKTOP-QC9AG5L:/mnt/c/Users/homer/Documents/project/sample (master)$ tree .git/objects/
.git/objects/
├── 16
│   └── 373b04a5e7375ba2a1a217313cf72c15aefa12
├── 27
│   └── 4c0052dd5408f8ae2bc8440029ff67d79bc5c3
├── 2e
│   └── 65efe2a145dda7ee51d1741299f848e5bf752e
├── 31
│   └── 9d7cff2c6dd7724e1d4ebcc66ac75ff4f3d595
├── 56
│   └── a6051ca2b02b04ef92d5150c9ef600403cb1de
├── 9d
│   └── 91c79faacd66e0744ba5d82e561b32c4f88f2f
├── info
└── pack

8 directories, 6 files
homer@DESKTOP-QC9AG5L:/mnt/c/Users/homer/Documents/project/sample (master)$ ls
data
1234homer@DESKTOP-QC9AG5L:/mnt/c/Users/homer/Documents/project/sample (master)$ git cat-file -p 1637
100644 blob 2e65efe2a145dda7ee51d1741299f848e5bf752e    leeter.txt
100644 blob 56a6051ca2b02b04ef92d5150c9ef600403cb1de    number.txt
homer@DESKTOP-QC9AG5L:/mnt/c/Users/homer/Documents/project/sample (master)$ git cat-file -p 319d
040000 tree 16373b04a5e7375ba2a1a217313cf72c15aefa12    data
homer@DESKTOP-QC9AG5L:/mnt/c/Users/homer/Documents/project/sample (master)$ git cat-file -p 9d91c
tree 319d7cff2c6dd7724e1d4ebcc66ac75ff4f3d595
author *** <***> 1608748395 -0600
committer *** <***> 1608748395 -0600

a1
```
Three files are created, a commit object (9d91c), a tree for the sample folder (319d) and another tree for its subdirectory `data` folder (1637).

### Dec 30th
I am working on branching and merging now. The development process has been smooth so far. I took two days off during the weekend. 