package edu.umn.power327;
//import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
	// code goes here omg!
        FileEnumerator enumerator = new FileEnumerator();
        // ArrayList should be better than Vector, since ArrayList size grows slower
        // and we don't need this to be thread safe
        ArrayList<Path> fileList = enumerator.enumerateFiles();
        int range = fileList.size(); // will use as a modulus for rng
        int halfSize = range / 2;

        Random random = new Random();
        Path curfile;

        while(range > halfSize) {
            // get a random file and swap it with last in fileList (first out-of-range file)
            Collections.swap(fileList, random.nextInt(range), (range - 1));
            range--;
            curfile = fileList.get(range); //
            // do compression suite on the first out-of-range file
            // compression things go here

            // store the info in the database, or store locally if bad connection
        }
//        System.out.println("list size: " + listSize);
    }
}
