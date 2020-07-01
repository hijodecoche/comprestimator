package edu.umn.power327;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;

public class FileEnumerator {

    public ArrayList<Path> enumerateFiles() {
        ArrayList<Path> list = new ArrayList<>();
        FileEnumVisitor visitor = new FileEnumVisitor(list);
        FileSystem fs = FileSystems.getDefault();
        for (Path p : fs.getRootDirectories()) {
            try {
                Files.walkFileTree(p, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, visitor);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
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
            this.list.add(file);
            return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {
            return FileVisitResult.SKIP_SUBTREE;
        }
    }
}
