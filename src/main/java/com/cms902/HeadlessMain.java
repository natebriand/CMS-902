package com.cms902;

import com.cms902.manager.TrackManager;
import com.cms902.model.Scenario;
import com.cms902.simulation.SimulationEngine;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Headless entry point for a containerized run without a display.
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
        manager.addListener(tracks -> {
            // Convert remaining-count into 1-based tick number
            long tick = TICKS_TO_RUN - latch.getCount() + 1;
            System.out.println("=== Tick " + tick + " | " + tracks.size() + " tracks ===");

            tracks.forEach(System.out::println);

            latch.countDown();
        });

        engine.start();
        latch.await(30, TimeUnit.SECONDS);
        engine.stop();

        System.out.println("CMS 902 headless run complete.");
    }
}