# File-Transfer-Protocol

**Summary**

This is an implemementation of the Very Simple File Transfer Protocol written in Java. The program was written using 
Intellij IDE and has been tested on both a Windows machine and on Osprey. The instructions for compiling the program are
using a UNIX terminal.

**Compilation**

Compile both the Server.java and the Client.java files using javac. 

**Input files**

The Server requires there to be a database of usernames and passwords in a local .txt file under the name "users.txt"

The format of users.txt should be:

username0 password0
username1 password1
.
.
.
usernamen passwordn

The Server also requires there to be a local directory under the name "root". This directory called root should have an 
arbitrary number of .txt files. No other file extensions were tested. 

No input files are needed for the Client program. 

**Using the VSTFP**

Open two terminal windows and navigate to the location of the Client and Server programs in each window. Use java Server 
to run the Server first, then use java Client in the second terimanl to run the Client. 

Once connected, the commands and functionality of the commands are verbatim the specifications listed in the 
description of the project on Canvas. 
