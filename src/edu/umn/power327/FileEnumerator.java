package edu.umn.power327;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class FileEnumerator {

    public ArrayList<Path> enumerateFiles() {
        ArrayList<Path> list = new ArrayList<>();
        FileEnumVisitor visitor = new FileEnumVisitor(list);
        FileSystem fs = FileSystems.getDefault();
        for (Path p : fs.getRootDirectories()) {
            try {
                Files.walkFileTree(p, visitor);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        System.out.println("exiting enumerator");
        return list;
    }

    /**
     * These are the functions that will be called on each file as we walk the file tree.
     */
    public static class FileEnumVisitor extends SimpleFileVisitor<Path> {

        public ArrayList<Path> list;

        public FileEnumVisitor(ArrayList<Path> list) {
            this.list = list;
        }
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (file.startsWith("/proc")) {
                return FileVisitResult.CONTINUE;
            } else {
                this.list.add(file);
            }
            return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {
            return FileVisitResult.SKIP_SUBTREE;
        }
    }
}
