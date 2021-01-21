### Thank you for volunteering in this experiment! You are a good person!

### TL;DR
Run this <b>on a terminal</b> using `java -Xmx6g -jar comprestimator.jar` because that's the only way you'll see if it is working and/or if it has finished running.  When it's done running, zip the file "test.db" and drop it in the Google Drive folder assigned to you.

## About Comprestimator:
  This program is part of an experiment to learn how real-world files compress.  When run on a machine, it feeds every file on the system through five different file compressors to see how well the file compresses and how long it takes the file to compress.  To protect your privacy, it will never store the name or location of any file on the system.  Instead, it uses a cryptographic hash function to identify duplicate files.  If you would like to know more about how hash functions work, visit the Wikipedia article on cryptographic hash functions: https://en.wikipedia.org/wiki/Cryptographic_hash_function.  Besides storing the file's hash, this program will record the file's original size, its compressed size, and the amount of time needed to compress the file.  All of this information will be stored locally on a sqlite database.
  
## What you need:
- The latest Java Runtime Environment (JRE).  This should run on anything that supports JRE 8 or higher.

## Before you run it:
  You will not be able to use your computer while this is running.  To keep your computer awake and prevent it from powering down, comprestimator will occasionally move the cursor on your computer, so you won't be able to use the mouse/touchpad while this is running.  I highly recommend running this overnight.  It takes hours.
  If you need to stop the program early, press Ctrl + C.  This should halt the program.
  
## How to run it:
**WARNING: Comprestimator will take many hours to finish.  If you need to use your computer before the program has finished running, hit `Ctrl + C`.** You can restart the program later, and it will pick up where it left off.

  ### Windows:
  1. Open CMD (command prompt) or Powershell.  Navigate to the folder where you placed comprestimator.jar.  
  2. Run: `java -Xmx6g -jar comprestimator.jar`. The first message you should see is "Beginning file enumeration...".  This takes several minutes depending on the amount of data on your computer.
  3. Next, you should see "Beginning compression loop...".  <b>This step will take many hours.  If you need to halt the program to use your computer, hit `Ctrl + C`.</b> You can safely restart the program later (this will not corrupt or overwrite any previous results).
  4. When the program is finished running, you will see a new file called "test.db".   Please save this!! This contains all 
     the results from the experiment!  
  5. I recommend compressing test.db before sending it.  To do this, right click on the file, and select "send to compressed 
     (zipped) folder."
  6. Drop it in the Google Drive folder assigned to you.  Email me at power327@d.umn.edu if you need help.  Thank you!
  
  
  ### Linux:
  1. Open a terminal.  Navigate to the directory that contains comprestimator.jar.
  2. Run: `java -Xmx6g -jar comprestimator.jar`. The first message you should see is "Beginning file enumeration...".  This takes several minutes depending on the amount of data on your computer.
  3. Next, you should see "Beginning compression loop...".  <b>This step will take many hours.  If you need to halt the program to use your computer, hit `Ctrl + C`.</b> You can safely restart the program later (this will not corrupt or overwrite any previous results).
  4. When the program is finished running, you will see a new file called "test.db".  Zip this file however you like (gzip
     works well).
  5. Drop the zipped file in the Google Drive folder assigned to you.  Email me at power327@d.umn.edu if you need help.  Thank you!
  
## Viewing results:
If you want to see the data comprestimator has stored, you will need to [download SQLite](https://www.sqlite.org/download.html).  Once you have downloaded and unzipped the file (or built the code from source), open a terminal, nagivate to the folder that contains comprestimator.jar and test.db, and enter `sqlite3 test.db`.  To see a few results gathered, enter `select * from lz4_results limit 10;`. Without `limit 10` you will likely get a list of about 100,000 results.  What you are seeing is the file's hash value (about 64 letters and numbers of gobbldeegook), the file extension (e.g. pdf, doc, jpg, etc.), and some numbers representing the size of the file before and after compressing and how long compression took.  **The names of your files and their contents are never stored.**

If this is overwhelming, email power327@d.umn.edu and ask for assistance.  We are also happy to send you the entire database dumped as a text file.
