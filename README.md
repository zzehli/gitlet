# gitlet
This is an independant implementation of a mini version of Git. The project skeleton is provided by Berkeley CS61B course

## Design
| Class: gitApi | Comments |
| ----- |-----|
| init() ||
| add(String filename) |create blobs but don't save to file|
| rm (String filename) | unstage files |
| commit(String message) | compare blobs and save new ones to file|
| log () ||
| ... ||
| private head||
| private master ||
| private HashMap(filename, ) stage  | this can be a set|


| Class: Object | Comments |
| ----- |-----|
| makeBlob(String filename) ||
| makeCommit() ||
| typeOf(String hash)| return wether the given hash refers to blob or commits|
| String parentBlob() | go to the parent commit and find the corresponding blob and return its hash |
| findParentCommit(String hash) | take the hash of the current commit and find its parent |
| compareBlob(String parentHash) ||

| Class: File | Comments |
| ---- | ------|
| File getBlob(String hash) |this is necessary since commits share blobs|
| boolean stageFile() | note that file added is not stored permanently, it can be override by new add if the same file changed | 
|  

| Class: Utils (ignore provided functions) | Comments |
| ---- | ----- |
| String getTime() | for timestamp objects at the time of adding |







| Class: 
## Development Journal
### Day 1: Dec 20st
Set up the project as an stand-alone repo and started the design process. The decision to extract the project from the CS61B course repo means I need to fix git issues and directory references. I added the submodule to the project folder. Dealing with submodules allow me to practice with the remote repos, which I will probably implement if the project goes well. Another problem was fixing the python test suit that contains the default library directory for the course. In the evening, I spent time reading the project prompt and read materials about git implementations. I am surprised by the number of expositions on this subject matter. There are quite a few git implementations and helpful expositories as well as conference talks. In particular, I figured out an important question: **what actually are the snapshots of the files when you add/commit?** For text files, simply save the whole file as a string, which is in turn saved to `.git/object` folder when commited. This a lot more simpler/unsophisticated than I have imagined. However, this only applies to txt file. This is really not much different from copy and paste. The disk space required for other format would be considerably larger if other file format are involved. 

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
### Day 2: Dec 21nd
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

### Day 3: Dec 22rd
