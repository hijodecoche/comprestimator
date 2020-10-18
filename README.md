### Thank you for volunteering in this experiment! You are a good person!

### TL;DR
Run this on a terminal using `java -jar comprestimator.jar` because that's the only way you'll see if it is working and/or if it has finished running.  When it's done running, zip the file "test.db" and email it to power327@d.umn.edu.

## About Comprestimator:
  This program is part of an experiment to learn how real-world files compress.  When run on a machine, it feeds every file on the system through five different file compressors to see how well the file compresses and how long it takes the file to compress.  To protect your privacy, it will never store the name or location of any file on the system.  Instead, it uses a cryptographic hash function to identify duplicate files.  If you would like to know more about how hash functions work, visit the Wikipedia article on cryptographic hash functions: https://en.wikipedia.org/wiki/Cryptographic_hash_function.  Besides storing the file's hash, this program will record the file's original size, its compressed size, and the amount of time needed to compress the file.  All of this information will be stored locally on a sqlite database.
  
## What you need:
- The latest Java Runtime Environment (JRE).  This should run on anything that supports JRE 8 or higher.

## Before you run it:
  You will not be able to use your computer while this is running.  To keep your computer awake and prevent it from powering down, comprestimator will occasionally move the cursor on your computer, so you won't be able to use the mouse/touchpad while this is running.  I highly recommend running this overnight.  It takes hours.
  If you need to stop the program early, press Ctrl + C.  This should halt the program.
  
## How to run it:
  ### Windows:
  1) Open CMD (command prompt) or Powershell.  Navigate to the folder where you placed comprestimator.jar.  
  2) Run: `java -jar comprestimator.jar`.  You should see a welcome message.
  3) When the program is finished running, you will see a new file called "test.db".   Please save this!! This contains all 
     the results from the experiment!  
  4) I recommend compressing test.db before sending it.  To do this, right click on the file, and select "send to compressed 
     (zipped) folder."
  5) Email it to power327@d.umn.edu.  Thank you!
  
  
  ### Linux:
  1) Open a terminal.  Navigate to the directory that contains comprestimator.jar.
  2) Run `java -jar comprestimator.jar`.  You should see a welcome message.
  3) When the program is finished running, you will see a new file called "test.db".  Zip this file however you like (gzip
     works well).
  4) Email the zipped file to power327@d.umn.edu.  Thank you!
  
