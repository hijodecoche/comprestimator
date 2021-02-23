package edu.umn.power327;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FileTypeFetcher {

    ProcessBuilder pb;
    ArrayList<String> commandString = new ArrayList<>(3);

    public FileTypeFetcher() {
        commandString.add(0, "sh");
        commandString.add(1, "-c");
        commandString.add(2, "file -b ");
        pb = new ProcessBuilder(commandString);
    }

    public String fetchType(String filename) throws IOException {
        commandString.remove(2);
        commandString.add("file -b " + filename);
        String result;
        Process process = pb.start();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        result = br.readLine().replace('\'', '"');

        return result;
    }
}
