package edu.umn.power327;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
	// code goes here omg!
        FileEnumerator enumerator = new FileEnumerator();
        // ArrayList should be better than Vector, since ArrayList size grows slower
        // and we don't need this to be thread safe
        ArrayList<Path> fileList = enumerator.enumerateFiles();
        int listSize = fileList.size(); // will use as a modulus for rng
        System.out.println("list size: " + listSize);
    }
}
