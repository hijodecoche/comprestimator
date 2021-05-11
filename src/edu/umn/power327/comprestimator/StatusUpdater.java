package edu.umn.power327.comprestimator;

import edu.umn.power327.comprestimator.database.DBController;
import edu.umn.power327.comprestimator.files.FileList;

import java.util.TimerTask;

public class StatusUpdater extends TimerTask {

    public static final int TIME_INTERVAL = 1800; // 1800 sec == 0.5 hr

    final DBController dbController = DBController.getInstance();
    final FileList fileList = FileList.getInstance();
    private final int prevElapsed = dbController.getElapsedTime();
    private int threshold = prevElapsed / TIME_INTERVAL; // threshold for when we print status (in sec)
    private final long startTime = System.currentTimeMillis();

    @Override
    public void run() {
        int elapsedTime = (int) (System.currentTimeMillis() - startTime) / 1000 + prevElapsed; // seconds, not millis
        dbController.updateTime(elapsedTime);
        if (elapsedTime / TIME_INTERVAL >= threshold) {
            printStatusToUser();
            threshold++;
        }
    }

    /**
     * This is written for TIME_INTERVAL == 3600 sec.  Something like a Duration object would eliminate this
     * dependency, in case someone in the future wants to adjust the interval.
     */
    private void printStatusToUser() {
        System.out.println("\t------------------------------");
        System.out.println("Comprestimator has run for " + threshold / 2.0 + " hours and has processed "
                + dbController.getFilesProcessed() + " files, about "
                + String.format("%.2f", fileList.getPercentFilesProcessed()) + "% of its list.");
        System.out.println("You can cancel using CTRL + C and restart later if you wish.");
        System.out.println("Resuming compression...");
    }
}
