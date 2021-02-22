package edu.umn.power327;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;

public class FileEnumerator {

    public ArrayList<File> enumerateFiles() throws IOException {
        ArrayList<File> list;
        FileEnumVisitor visitor;
        FileSystem fs = FileSystems.getDefault();
        FileOutputStream fos;
        ObjectOutputStream oos;
        try {
            FileInputStream fis = new FileInputStream("enumeration.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);

            System.out.println("Loading previous enumeration...");
            list = (ArrayList<File>) ois.readObject();
            System.out.println("File list successfully loaded!");

        } catch (Exception e) {
            list = new ArrayList<>();
            visitor =  new FileEnumVisitor(list, getProhibitedList());

            for (Path p : fs.getRootDirectories()) {
                try {
                    Files.walkFileTree(p, visitor);
                } catch (IOException err) {
                    err.printStackTrace();
                }
            }
            fos = new FileOutputStream("enumeration.dat");
            oos = new ObjectOutputStream(fos);
            oos.writeObject(list); // in case we stop and restart, no need for second enumeration
        }

        System.out.println("exiting enumerator");
        return list;
    }

    private HashMap<String, String> getProhibitedList() {
        HashMap<String, String> prohibited = new HashMap<>();
        try {
            FileReader fr = new FileReader("skip_list.txt");
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                prohibited.put(line, line);
            }
        } catch (IOException e) {
            prohibited.put("/dev", "/dev");
            prohibited.put("/sys", "/sys");
            prohibited.put("/proc", "/proc");
            prohibited.put("/snap", "/snap");
            prohibited.put("/run", "/run");
        }

        return prohibited;
    }

    /**
     * These are the functions that will be called on each file as we walk the file tree.
     * Files.walkFileTree() requires we use Path, so we must convert each path to a File.
     */
    public static class FileEnumVisitor implements FileVisitor<Path> {

        public ArrayList<File> list;
        public HashMap<String, String> prohibited;

        public FileEnumVisitor(ArrayList<File> list, HashMap<String, String> prohibited) {
            this.list = list;
            this.prohibited = prohibited;
        }

        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
            this.list.add(path.toFile());
            return FileVisitResult.CONTINUE;
        }

        public FileVisitResult visitFileFailed(Path path, IOException e) {
            return FileVisitResult.SKIP_SUBTREE;
        }

        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            if (prohibited.containsKey(dir.toString())) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            else
                return FileVisitResult.CONTINUE;
        }
    }
}
