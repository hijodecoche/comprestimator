package edu.umn.power327.files;

import edu.umn.power327.database.DBController;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;

public class FileList {

    private ArrayList<String> fileList = null;
    private BufferedReader reader;
    private final DBController dbController = DBController.getInstance();
    private RandomAccessFile raf;
    private long startIndex = 0;

    public FileList() throws Exception {
        fileList = new ArrayList<>();
        raf = new RandomAccessFile("placeholder.dat", "rw");
        try {
            FileReader fr = new FileReader("enumeration.dat");
            reader = new BufferedReader(fr);
            startIndex = raf.readLong();
            System.out.println("Resuming previous enumeration...");
            if (startIndex < 0 || !findPlace()){
                throw new Exception("Could not seek to specified line");
            }
            System.out.println("Successfully found where we left off!");

        } catch (Exception e) {
            enumFiles();
            Collections.shuffle(fileList);
            BufferedWriter bw = new BufferedWriter(new FileWriter("enumeration.dat", false));
            raf.writeLong(0);
            for (String s : fileList) {
                bw.write(s);
            }
            bw.flush();
        }

        if (dbController == null)
            throw new Exception("Could not get database instance!");
    }

    public void enumFiles() throws Exception {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (isWindows) {
            enumerateWindows();
        }
        else
            enumerateUnix();
    }

    private void enumerateWindows() throws IOException, InterruptedException {
        Process process;
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/s", "dir /b /s /a:-D");
        System.out.println(System.currentTimeMillis());
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            pb.directory(p.toFile());
            process = pb.start();
            LineParser lineParser = new LineParser(process.getInputStream(), fileList);
            Executors.newSingleThreadExecutor().submit(lineParser);
            int exit = process.waitFor();
            System.out.println(p.toFile());
        }
        System.out.println(System.currentTimeMillis());

    }

    private void enumerateUnix() throws InterruptedException, IOException {
//        System.out.println("Detected unix");
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", "find / > enumeration.dat");
        System.out.println(System.currentTimeMillis());
        Process process = pb.start();
        LineParser lineParser = new LineParser(process.getInputStream(), fileList);
        Executors.newSingleThreadExecutor().submit(lineParser);
        int exit = process.waitFor();
        System.out.println(System.currentTimeMillis());
    }

    public File getNext() throws IOException {
        startIndex++;
        // write startIndex to raf
        raf.seek(0);
        raf.writeLong(startIndex);
        // return the next string
        return new File(reader.readLine());
    }

    private boolean findPlace() {
        // get startIndex-many lines until we reach proper spot
        long counter = 0;
        while (counter < startIndex) {
            try {
                reader.readLine();
                counter++;
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    private static class LineParser implements Runnable {
        private final InputStream inputStream;
        private final ArrayList<String> fileList;

        public LineParser(InputStream inputStream, ArrayList<String> f) {
            this.inputStream = inputStream;
            fileList = f;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(fileList::add);
        }
    }
}
