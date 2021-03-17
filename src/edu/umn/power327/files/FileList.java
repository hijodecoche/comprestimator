package edu.umn.power327.files;

import edu.umn.power327.database.DBController;

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

public class FileList {

    private BufferedReader reader;
    private final DBController dbController = DBController.getInstance();
    private int startIndex;
    private final ArrayList<String> prohibited;

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
            dbController.updateStartIndex(0);
            ArrayList<String> fileStringList = new ArrayList<>();

            /* DEBUGGING */
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
            Date resultdate = new Date(System.currentTimeMillis());
            System.out.println("Enumeration began " + sdf.format(resultdate));
            /* END DEBUGGING */

            enumFiles(fileStringList);
            Collections.shuffle(fileStringList);
            BufferedWriter bw = new BufferedWriter(new FileWriter("enumeration.dat", false));
            for (String s : fileStringList) {
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

        // fill prohibited with skip_list
        prohibited = new ArrayList<>();
        try {
            FileReader fr = new FileReader("skip_list.txt");
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                prohibited.add(line);
            }
        } catch (IOException e) {
            prohibited.add("/dev");
            prohibited.add("/sys");
            prohibited.add("/proc");
            prohibited.add("/snap");
            prohibited.add("/run");
        }
    }

    public void enumFiles(ArrayList<String> fileList) throws Exception {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (isWindows) {
            enumerateWindows(fileList);
        }
        else
            enumerateUnix(fileList);
    }

    private void enumerateWindows(ArrayList<String> fileList) throws InterruptedException {
        Process process;
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "dir /b /s /a:-D");
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            try {
                pb.directory(p.toFile());
                process = pb.start();
                StreamGobbler gobbler = new StreamGobbler(process.getInputStream(), fileList::add);
                Executors.newSingleThreadExecutor().submit(gobbler);
                int exit = process.waitFor();
            } catch (IOException ignored) {}
        }
    }

    private void enumerateUnix(ArrayList<String> fileList) throws InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "sh", "-c", "find / -path /proc -prune -o -path /sys -prune -o -path /dev -prune -o -path /snap -prune -o -path /run -prune -o -type f -print");
        pb.redirectError(ProcessBuilder.Redirect.to(new File("/dev/null")));
        Process process = pb.start();

        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), fileList::add);
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
                // check if file is prohibited
                for (String s : prohibited) {
                    if (path.startsWith(s)) {
                        throw new IOException("File is prohibited.");
                    }
                }
                file = new File(path);
            } catch (IOException ignored) {
                // file does not exist or cannot be opened
            } catch (NullPointerException e) {
                // we reached the end of the enumeration file
                return null;
            }
        }

        try {
            dbController.updateStartIndex(startIndex);
        } catch (SQLException ignored) {}

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
        private final InputStream inputStream;
        private final Consumer<String> consumer;

        public StreamGobbler(InputStream inStream, Consumer<String> consumer) {
            this.inputStream = inStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }
}
