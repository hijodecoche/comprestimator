package edu.umn.power327.comprestimator.files;

import edu.umn.power327.comprestimator.database.DBController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FileList {

    private BufferedReader reader;
    private final DBController dbController = DBController.getInstance();
    private int startIndex;
    private final int listLength;
    private static FileList fileList;

    static {
        try {
            fileList = new FileList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FileList getInstance() {
        return fileList;
    }

    private FileList() throws Exception {

        System.out.println("Beginning filesystem enumeration...");

        try {
            FileReader fr = new FileReader("enumeration.dat");
            reader = new BufferedReader(fr);
            System.out.println("Loading previous enumeration...");
            startIndex = dbController.getStartIndex();
            if (startIndex < 0 || !findCheckpoint()) {
                throw new Exception("Could not seek to specified line");
            }
            System.out.println("Successfully found where we left off!");

        } catch (Exception e) {
            dbController.updateStartIndex(0); // if database is old but enum file is new, change starting index
            ArrayList<String> filePathList = new ArrayList<>();

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
            Date resultdate = new Date(System.currentTimeMillis());
            System.out.println("Enumeration began " + sdf.format(resultdate));

            enumFiles(filePathList);

            // SHUFFLE FILES
            Collections.shuffle(filePathList);
            BufferedWriter bw = new BufferedWriter(new FileWriter("enumeration.dat", false));
            for (String s : filePathList) {
                bw.write(s);
                bw.newLine();
            }
            bw.close();

            // notify user
            resultdate = new Date(System.currentTimeMillis());
            System.out.println("Enumeration completed " + sdf.format(resultdate));

            FileReader fr = new FileReader("enumeration.dat");
            reader = new BufferedReader(fr);
        }

        // CALCULATE LIST LENGTH
        try (Stream<String> stream = Files.lines(new File("enumeration.dat").toPath(), StandardCharsets.UTF_8)) {
            listLength = (int) stream.count();
        }

        // INFORM USER
        System.out.println("...enumeration complete.");
        System.out.flush();
    }

    public void enumFiles(ArrayList<String> fileList) throws Exception {
        // fill prohibited with skip_list
        ArrayList<String> skiplist = new ArrayList<>();
        try {
            FileReader fr = new FileReader("skip_list.txt");
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                skiplist.add(line);
            }
        } catch (IOException ioe) {
            skiplist.add("/dev");
            skiplist.add("/sys");
            skiplist.add("/proc");
            skiplist.add("/snap");
            skiplist.add("/run");
        }
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (isWindows) {
            enumerateWindows(fileList, skiplist);
        }
        else
            enumerateUnix(fileList, skiplist);
    }

    private void enumerateWindows(ArrayList<String> fileList, ArrayList<String> skiplist) throws InterruptedException {
        Process process;
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "dir /b /s /a:-D");
        pb.redirectErrorStream(true); // redirects to stdout...only thing that has worked thus far
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            try {
                if (p.toString().startsWith("\\\\") || p.toString().startsWith("//")) // exclude network drives
                    continue;
                pb.directory(p.toFile());
                process = pb.start();
                StreamGobbler gobbler = new StreamGobbler(process.getInputStream(), skiplist, fileList::add);
                Executors.newSingleThreadExecutor().submit(gobbler);
                int exit = process.waitFor();
            } catch (IOException ignored) {}
        }
    }

    private void enumerateUnix(ArrayList<String> fileList, ArrayList<String> skiplist) throws InterruptedException, IOException {
        StringBuilder commandString = new StringBuilder("find / -fstype nfs -prune -o ");
        for (String s : skiplist) {
            commandString.append("-path ").append(s).append(" -prune -o ");
        }
        commandString.append("-type f -print");
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", commandString.toString());
        pb.redirectError(ProcessBuilder.Redirect.to(new File("/dev/null")));
        Process process = pb.start();

        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), skiplist, fileList::add);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exit = process.waitFor();
    }

    /**
     * Gets next file name from stream, checks if file is in the prohibited list, and advances the startIndex
     * stored in the DB.
     * @return A new file, or null if reached end of stream
     */
    public File getNext() {
        File file = null;
        while (file == null) {
            if (startIndex < Integer.MAX_VALUE) startIndex++;

            try {
                String path = reader.readLine();
                file = new File(path);
            } catch (IOException ignored) {
                // file does not exist or cannot be opened
            } catch (NullPointerException e) {
                // we reached the end of the enumeration file
                return null;
            }
        }

        if (startIndex % 2 == 0) {
            try {
                dbController.updateStartIndex(startIndex);
            } catch (SQLException ignored) { }
        }

        return file;
    }

    /**
     * Uses info in the DB header to find index where we left off in the enumeration file.
     * @return True if successfully found the checkpoint.
     */
    private boolean findCheckpoint() {
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

    public double getPercentFilesProcessed() {
        return (double) startIndex / listLength;
    }

    private static class StreamGobbler implements Runnable {
        /* I know little about functional programming, so I stole a lot of this from
         * stackoverflow.com/questions/22845574/how-to-dynamically-do-filtering-in-java-8
         */
        private final InputStream inputStream;
        private final Consumer<String> consumer;
        private final Predicate<String> compositePredicate;

        public StreamGobbler(InputStream inStream, ArrayList<String> skip, Consumer<String> consumer) {
            this.inputStream = inStream;
            this.consumer = consumer;

            // create list of predicates that check if this pathname starts with any of the forbidden paths
            ArrayList<Predicate<String>> allFilters = new ArrayList<>();
            for (String s : skip) {
                allFilters.add(e -> !e.startsWith(s));
            }
            // create one large predicate, which is a chain of ands between predicates in list
            // if all predicates are true, then this path is NOT a descendent of a forbidden directory
            // so the composite predicate will be true, meaning it will pass the filter test
            compositePredicate = allFilters.stream().reduce(w -> true, Predicate::and);
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .filter(compositePredicate).forEach(consumer);
        }
    }
}
