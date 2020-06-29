package edu.umn.power327;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;

public class FileEnumerator {

    public ArrayList<Path> enumerateFiles() {
        ArrayList<Path> list = new ArrayList<>();
        FileEnumVisitor visitor = new FileEnumVisitor();
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

    public static class FileEnumVisitor extends SimpleFileVisitor {
        @Override
        public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) throws IOException {
            return super.visitFile(file, attrs);
        }
    }
}
