## Thank you for volunteering in this experiment! You are a good person!

## About Comprestimator
  This program is part of an experiment to learn how real-world files compress.  When run on a machine, it feeds every file on the system through five different file compressors to see how well the file compresses and how long it takes the file to compress.
  
## Privacy Protection
Though we do not track or store file names or file locations on your system, we do collect some information that could be used to guess what type of information <i>may</i> exist on your system.  For this reason, we will not distribute this data to anyone not part of the Comprestimator study. 

Instead of storing file names, comprestimator will store a file's hash value, which is like a unique file fingerprint.  This fingerprint cannot tell us the name, location, or content of the original file.  (If you would like to know more about how hash functions work, visit the Wikipedia article on cryptographic hash functions: https://en.wikipedia.org/wiki/Cryptographic_hash_function).  Besides storing the file's hash, this program will record the file's original size, its compressed size, and the amount of time needed to compress the file.  All of this information will be stored locally on an SQLite database file.

### Skip List
We do not record the names of files processed, nor do we store any of the data that the files contain.  However, if you have some files that you don't want comprestimator to touch at all, you can enter the file location(s) in `skip_list.txt`.  See the instructions in "How to run it" to use the skip list.

## What you need
- Download the zip folder with all the necessary tools [here]('https://github.com/hijodecoche/comprestimator/raw/master/comprestimator.zip').
- The latest Java Runtime Environment (JRE).  This should run on anything that supports JRE 8 or higher.
- If you wish to build from source, see Building from Source below

## Before you run it
  You will not be able to use your computer while this is running.  We highly recommend running this overnight.  It takes many, many hours.
  If you need to stop the program early, press Ctrl + C.  This should halt the program.
  
## How to participate
If you would like to participate in our study, please read and sign the electronic consent form [available here](https://forms.gle/kzrZGrUJSHQ8QpXU8).  Once you sign up, we will send you a link to a private Google Drive folder where you can upload your compression database when you are done running the tool.

## How to run it
**WARNING: Comprestimator will take many hours to finish.  If you need to use your computer before the program has finished running, hit `Ctrl + C`.** You can restart the program later, and it will pick up where it left off.

  ### Windows
  
  (note: if you have Windows Subsystem for Linux (WSL), I recommend using WSL so that we can gather `file` command metadata.  If you do not know what WSL is, disregard this request.)
    
  1. Open your Downloads folder (or wherever you placed the comprestimator zip file).  Right-click on `comprestimator.zip` and select `Extract all...`.  
  1. If there are any folders that you do not want comprestimator to touch, enter the full pathname of each folder on a separate line in the file `skip_list.txt`.  The pathname begins with the hard drive's letter, e.g `C:\Users\me\private_folder`. Careful!  The skip list is case sensitive!
     If you cannot find `skip_list.txt`, double click `run.bat` and then hit `Ctrl + C` after a few seconds to quit the program. This will generate `skip_list.txt` for you. 
  2. Double-click `run.bat`. A shell window will open.  The first message you should see is "Beginning file enumeration...".  
     This takes several minutes depending on the amount of data on your computer.
  3. Next, you should see "Beginning compression loop...".  <b>This step will take many hours.  If you need to halt the 
     program to use your computer, hit `Ctrl + C`.</b> You can safely restart the program later (this will not corrupt or overwrite any previous results).
  4. When the program is finished running, you will see a new file called "test.db".   Please save this!! This contains all 
     the results from the experiment!  
  5. I recommend compressing test.db before sending it.  To do this, right click on the file, and select "send to compressed 
     (zipped) folder."
  6. Drop it in the Google Drive folder assigned to you.  Email me at power327@d.umn.edu if you need help.  Thank you!
  
  
  ### Linux/Mac
  1. Open a terminal.  Navigate to the directory that contains comprestimator.jar.
  1. If there are any folders that you do not want comprestimator to touch, enter the full pathname of each folder on a separate line in the file `skip_list.txt`.  Careful!  The skip list is case sensitive!
     If you cannot find `skip_list.txt`, enter `./run.sh` and then hit `Ctrl + C` after a couple seconds to quit the program.  This will generate `skip_list.txt` for you.
  2. Enter `./run.sh`. The first message you should see is "Beginning file enumeration...".  This takes several minutes depending on the amount of data on your computer.
  3. Next, you should see "Beginning compression loop...".  <b>This step will take many hours.  If you need to halt the 
     program to use your computer, hit `Ctrl + C`.</b> You can safely restart the program later (this will not corrupt or overwrite any previous results).
  4. When the program is finished running, you will see a new file called "test.db".  Zip this file however you like (gzip
     works well).
  5. Drop the zipped file in the Google Drive folder assigned to you.  Email me at power327@d.umn.edu if you need help.  Thank you!
  
## Building from source
You're more than welcome to build from source if you wish!  We've tried to make it as easy as possible.  All you will need is `ant` and JDK 8 or higher.  Once you have cloned the git repo, you'll need to create a file called `build.properties` in the same directory as `build.xml`.  In `build.properties`, write the line
```jdk.home=pathToYourJDK``` substituting the path to the JDK on your machine (it should look like `jdk.home=/usr/lib/jvm/java-8-openjdk` or something similar.  Don't add a '/' at the end of the line!

Once you've created `build.properties`, in a command line, run `ant`.  If everything goes well, you should see `BUILD SUCCESSFUL`.  All dependencies should be included in the git repo, so you shouldn't need to download anything else.  The JAR will be in `out/artifacts/comprestimator_jar`.  Wherever you put the JAR, make sure you move either `run.sh` or `run.bat` to the same directory.
  
## Viewing results
If you want to see the data comprestimator has stored, you will need to [download SQLite3](https://www.sqlite.org/download.html).  Once you have downloaded and unzipped the file (or built the code from source), open a terminal, nagivate to the folder that contains test.db, and enter `sqlite3 test.db`.  To see a few results gathered, enter `select * from lz4_results limit 10;`. Without `limit 10` you will likely get a list of about 100,000 results.  What you are seeing is the file's hash value (about 64 letters and numbers of gobbldeegook), the file extension (e.g. pdf, doc, jpg, etc.), and some numbers representing the size of the file before and after compressing and how long compression took.  **The names of your files and their contents are never stored.**

If this is overwhelming, email power327@d.umn.edu and ask for assistance.  We are also happy to send you the entire database dumped as a text file.
