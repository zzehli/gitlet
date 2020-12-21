o gitlet
This is an independant implementation of a mini version of Git. The project skeleton is provided by Berkeley CS61B course

## Development Journal
Day 1: Dec 21st, set up the project as an stand-alone repo and started the design process. The decision to extract the project from the CS61B course repo means I need to fix git issues and directory references. I added the submodule to the project folder. Dealing with submodules allow to practice with the remote repos, which I will probably implement if the project goes well. Another problem was fixing the python test suit that contains the default library directory for the course. In the evening, I spent time reading the project prompt and read materials about git implementations. I am surprised by the number of expositions on this subject matter. There are quite a few git implementations and helpful expositories as well as conference talks. In particular, I figured out an important question: how to save snapshots of the files added/commited? For text files, simply save the whole file as a string. This a lot more simpler/unsophisticated than I have imagined. However, this only applies to txt file. This is really not much different from copy and paste. The disk space required for other format would be considerably larger if other file format are involved. 

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

