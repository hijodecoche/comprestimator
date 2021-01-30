package edu.umn.power327;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class FileEnumerator {

    public ArrayList<File> enumerateFiles() throws IOException {
        ArrayList<File> list;
        FileEnumVisitor visitor;
        FileSystem fs = FileSystems.getDefault();
        FileOutputStream fos;
        ObjectOutputStream oos;
        try {
            FileInputStream fis = new FileInputStream("enumeration.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            list = (ArrayList<File>) ois.readObject();
        } catch (FileNotFoundException | ClassNotFoundException e) {
            fos = new FileOutputStream("enumeration.txt");
            oos = new ObjectOutputStream(fos);
            list = new ArrayList<>();
            visitor =  new FileEnumVisitor(list);

            for (Path p : fs.getRootDirectories()) {
                if (!p.startsWith("/proc") && !p.startsWith("/dev") && !p.startsWith("/sys") && !p.startsWith("/snap")) {
                    try {
                        Files.walkFileTree(p, visitor);
                    } catch (IOException err) {
                        err.printStackTrace();
                    }
                }
            }
            oos.writeObject(list); // in case we stop and restart, no need for second enumeration
        }

        System.out.println("exiting enumerator");
        return list;
    }

    /**
     * These are the functions that will be called on each file as we walk the file tree.
     * Files.walkFileTree() requires we use Path, so we must convert each path to a File.
     */
    public static class FileEnumVisitor extends SimpleFileVisitor<Path> {

        public ArrayList<File> list;

        public FileEnumVisitor(ArrayList<File> list) {
            this.list = list;
        }
        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
            this.list.add(path.toFile());
            return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult visitFileFailed(Path path, IOException e) {
            return FileVisitResult.SKIP_SUBTREE;
        }
    }
}
