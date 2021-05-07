package edu.umn.power327.comprestimator;

import edu.umn.power327.comprestimator.database.DBController;

import java.util.TimerTask;

public class TimeUpdater extends TimerTask {

    public static final int TIME_INTERVAL = 3600; // 3600 sec == 1 hr

    final DBController dbController = DBController.getInstance();
    private int elapsedTime = dbController.getElapsedTime();
    private int threshold = elapsedTime / TIME_INTERVAL; // threshold for when we print status (in sec)
    private final long startTime = System.currentTimeMillis();

    @Override
    public void run() {
        elapsedTime += (int)(System.currentTimeMillis() - startTime) / 60; // seconds, not millis
        dbController.updateTime(elapsedTime);
        if (elapsedTime / TIME_INTERVAL > threshold) {
            threshold++;
            printStatusToUser();
        }
    }

    /**
     * This is written for TIME_INTERVAL == 3600 sec.  Something like a Duration object would eliminate this
     * dependency, in case someone in the future wants to adjust the interval.
     */
    private void printStatusToUser() {
        System.out.println("\t------------------------------");
        System.out.println("Comprestimator has run for about " + threshold + " hours and has processed "
                + dbController.getFilesProcessed() + " files.");
        System.out.println("You can cancel using CTRL + C and restart later if you wish.");
        System.out.println("Resuming compression...");
    }
}
