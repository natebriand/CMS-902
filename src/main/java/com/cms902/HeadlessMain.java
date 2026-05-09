package com.cms902;

import com.cms902.manager.TrackManager;
import com.cms902.model.Scenario;
import com.cms902.simulation.SimulationEngine;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * For running CMS 902 headless without the JavaFX UI.
 *
 * Used for containerized environments where no display is available.
 * Builds the simulation and track manager, runs for a number of ticks,
 * prints each update to the console, then exits.
 */
public class HeadlessMain {

    private static final int TICKS_TO_RUN = 5;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("CMS 902 starting in headless mode...");

        SimulationEngine engine = new SimulationEngine(Scenario.PEACETIME);
        TrackManager manager = new TrackManager();
        engine.addListener(manager);

        // CountDownLatch is a counter shared between threads. Starts at TICKS_TO_RUN,
        // counts down each time we get an update, and lets the main thread wait for it.
        CountDownLatch latch = new CountDownLatch(TICKS_TO_RUN);

        // Adding the lambda as a listener making it run every time the TrackManager publishes
        // an update (every 2 seconds).
        manager.addListener(tracks -> {
            // Convert remaining-count into 1-based tick number
            long remaining = latch.getCount();
            System.out.println("=== Tick " + (TICKS_TO_RUN - remaining + 1) + " | " + tracks.size() + " tracks ===");

            tracks.forEach(System.out::println);

            // Subtract one from the latch.
            latch.countDown();
        });

        // Start the simulation's background thread.
        engine.start();

        // Block the main thread until the latch hits zero or 30 seconds pass.
        latch.await(30, TimeUnit.SECONDS);

        engine.stop();
        System.out.println("CMS 902 headless run complete.");
    }
}