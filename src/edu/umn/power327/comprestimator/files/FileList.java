package edu.umn.power327.comprestimator.files;

import edu.umn.power327.comprestimator.database.DBController;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FileList {

    private BufferedReader reader;
    private final DBController dbController = DBController.getInstance();
    private int startIndex;

    public FileList() throws Exception {

        try {
            FileReader fr = new FileReader("enumeration.dat");
            reader = new BufferedReader(fr);
            System.out.println("Loading previous enumeration...");
            startIndex = dbController.getStartIndex();
            if (startIndex < 0 || !findPlace()) {
                throw new Exception("Could not seek to specified line");
            }
            System.out.println("Successfully found where we left off!");

        } catch (Exception e) {
            dbController.updateStartIndex(0); // if database is old but enum file is new, change starting index
            ArrayList<String> filePathList = new ArrayList<>();

            /* DEBUGGING */
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
            Date resultdate = new Date(System.currentTimeMillis());
            System.out.println("Enumeration began " + sdf.format(resultdate));
            /* END DEBUGGING */

            enumFiles(filePathList);

            // SHUFFLE FILES
            Collections.shuffle(filePathList);
            BufferedWriter bw = new BufferedWriter(new FileWriter("enumeration.dat", false));
            for (String s : filePathList) {
                bw.write(s);
                bw.newLine(); // maybe replace with + "\n" above
            }
            bw.close();

            /* DEBUGGING */
            resultdate = new Date(System.currentTimeMillis());
            System.out.println("Enumeration completed " + sdf.format(resultdate));
            /* END DEBUGGING */

            FileReader fr = new FileReader("enumeration.dat");
            reader = new BufferedReader(fr);
        }
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
                pb.directory(p.toFile());
                process = pb.start();
                StreamGobbler gobbler = new StreamGobbler(process.getInputStream(), skiplist, fileList::add);
                Executors.newSingleThreadExecutor().submit(gobbler);
                int exit = process.waitFor();
            } catch (IOException ignored) {}
        }
    }

    private void enumerateUnix(ArrayList<String> fileList, ArrayList<String> skiplist) throws InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "sh", "-c", "find / -mount -path /proc -prune -o -path /sys -prune -o -path /dev -prune -o -path /snap -prune -o -path /run -prune -o -type f -print");
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
