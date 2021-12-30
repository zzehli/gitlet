# gitlet

This is an independant implementation of a mini version of Git. The project is based on an assignment in the Berkeley [CS61B](https://cs61bl.org/su20/projects/gitlet/) course.
I implemented the `Main`, `Gitfile`, `Command`, `Objects` and `Gitindex`. A few functions in the `Utils` class and the python testing suit is provided by the course.
## Run

In the outer gitlet folder, compile the program with: 
```
javac gitlet/*.java
```

Once you compiled, you can use run various git command with the following call
```
java gitlet.Main [command]
```

Be sure to `init` your directory before other commands.

supported commands are:
```
init
add [file name] 
commit [message]
rm [file name]
log
status
branch [branch name]
checkout -- [filename]
checkout [commit id] -- [file name]
checkout [branch name]
reset [commit id]
merge [branch name]
rebase [branch name]
```

The functionalities are simplified versions of the real Git. For example, the `add` command can only add one file at a time.
The detailed behaviors of the commands are described in the [assignment](https://cs61bl.org/su20/projects/gitlet/) page.

## Design

The basic design of gitlet involves two parts, set up git objects and work with branches.
![alt text](https://github.com/zzehli/gitlet/blob/main/test.png)


## Development Journal
### Day 1: Dec 20th
Set up the project as an stand-alone repo and started the design process. The decision to extract the project from the CS61B course repo means I need to fix git issues and directory references. I added the submodule to the project folder. Dealing with submodules allow me to practice with the remote repos, which I will probably implement if the project goes well. Another problem was fixing the python test suit that contains the default library directory for the course. In the evening, I spent time reading the project prompt and read materials about git implementations. I am surprised by the number of expositions on this subject matter. There are quite a few git implementations and helpful expositories as well as conference talks. In particular, I figured out an important question: **what actually are the snapshots of the files when you add/commit?** For text files, simply save the whole file as a string, which is in turn saved to `.git/object` folder when commited. This a lot more simpler/unsophisticated than I have imagined. However, this only applies to txt file. This is really not much different from copy and paste. The disk space required for other format would be considerably larger if other file formats are involved. 

The [naive version control workflow](http://git.github.io/git-reference/) provided by the github team illustrate what Git really is
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
I am working on branching and merging now. The development process has been smooth so far. I took two days off during weekend. 

### Dec 31th
Reflections:
* Made a bad decision **not include hash in the git object(Objects) class**. I thought I am not supposed to change the object once 
I made it, so there is no way to include hash in the private field. This is wrong, the serialized object does not necessarily need
to correspond to the one given to the sha-1 function. Therefore, one only needs to hash the content, get the hash, put in the private 
field and serialize the object and put into the file with hash as its file name.
* Because of this, I constantly perform format changes from file to object and get object from hash. I wrote quite a few helper 
function to the point that I sometime forgot what functionality I have implemented.
* The real git index system is beyond my capacity. While the .git/index file might seem small if you `cat` the file. But 
the file contains much information about your repo, if you performs [a binary/hex dump](https://mincong.io/2018/04/28/git-index/).
* Besides the index file, I omitted the log files as well. These are readable files that contains the branch commit/HEAD pointer
information to help keeping track of the commit/branching histories.
* Merging(for two branches) is more complicated than I thought. An important step is **finding the latest common ancestor/split point**. Even for two
branches, the fact that there might be merging commits in the past means one need to compare few potential candidates as ancestor and select the nearest one.
However, this information (distance from the current commit) cannot be stored beforehand. Each time one wants to find the split point, in order to compare
the candidates, one need to traverse them and calculate the steps. Real Git includes "MERGE_HEAD" tag to merge commits, 
but I did not do so. I am not sure how this can simplify the process since branching names might have changed and there is 
no way to keep track of whether the past merge took place in the current branch besides traversing again.

### Jan 2nd 
An important aspect of the project is **caching**. In my project design, most information is stored in the `Objects` class, which include 
blob and commit objects. The program starts from `main` and gets directed to corresponding functionalities in `Command` class. Each git functionality
starts by reading git objects from the `.gitlet/objects` folder, which stores git objects, by its hash. Many helper methods can be shared 
among different functionalities, such as update fatch git objects, compare different commits, staging area. These helper functions are stored in 
`Gitfile` class, which consists of only static functions. To cache information, git objects should be stored in the `Command` class and pass necessary information
to the helper functions, in stead of read files in helper functions. 

Since I do not include hash in the git object, it is not easy to fetch object hash from object file itself. So I was not 
clear how best to pass information between helper functions. While it is most convenient to pass hash and read object in the helpfer funtion, 
but I realize in some `Command` methods, I am reading same files in the parent function and helpfer function.
