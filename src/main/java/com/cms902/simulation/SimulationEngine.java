package com.cms902.simulation;

import com.cms902.model.Track;
import com.cms902.model.Scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Generates and updates a synthetic tactical picture for the combat management system.
 *
 * SimulationEngine is the data source layer of the application. It produces a randomized
 * set of tracks based on a {@link Scenario} and advances their positions using dead reckoning
 * at a fixed tick rate. Listeners are notified after each tick with the current full set of
 * tracks. In the current architecture, the only listener is the TrackManager.
 *
 * This class is designed to be swappable with a real data source (e.g., an Arduino
 * feeding sensor input) without affecting anything downstream. As long as the
 * replacement produces {@link Track} objects and notifies via the same listener
 * interface, the rest of the system requires no changes.
 *
 * Threading: the simulation tick runs on a dedicated daemon thread provided by a
 * single-threaded ScheduledExecutorService. The daemon flag ensures the JVM exits
 * cleanly when the main UI window is closed.
 */
public class SimulationEngine {

    /**
     * Callback interface for components that wish to be notified of track updates.
     *
     * Implementations receive the full track list on every simulation tick.
     * Implementations should not retain or mutate the list reference directly,
     * if they need to hold the data, they should make a defensive copy.
     */
    public interface TrackUpdateListener {
        void onTracksUpdated(List<Track> tracks);
    }

    private final Scenario scenario;
    private final List<Track> tracks;
    private final List<TrackUpdateListener> listeners;
    private ScheduledExecutorService scheduler;
    private final Random random = new Random();

    /**
     * Constructs a new simulation engine and immediately generates the initial
     * track set based on the given scenario.
     *
     * The simulation does not begin ticking until {@link #start()} is called.
     *
     * @param scenario the operational context that drives track generation
     */
    public SimulationEngine(Scenario scenario) {
        this.scenario = scenario;
        this.tracks = new ArrayList<>();
        this.listeners = new ArrayList<>();
        generateTracks();
    }

    /**
     * Generates the initial set of tracks for the current scenario.
     *
     * The number of tracks, their speed range, and their classification
     * probabilities are all driven by the scenario. Position is randomized
     * within a fixed North Atlantic operating area (48–52°N, 30–40°W),
     * chosen to reflect a realistic Royal Canadian Navy operating area
     * east of Newfoundland.
     */
    private void generateTracks() {
        for (int i=0; i<random.nextInt(scenario.minTracks, scenario.maxTracks + 1); i++) {
            String designation = String.format("T-%03d", i + 1);
            Track.TrackType[] types = Track.TrackType.values();
            Track.TrackType randomType = types[random.nextInt(types.length)];
            double latitude = 48.0 + random.nextDouble() * 4.0;   // 48.0 – 52.0°N
            double longitude = -40.0 + random.nextDouble() * 10.0; // -40.0 – -30.0°W

            Track track = new Track(designation, randomType, latitude, longitude);

            track.setSpeed(random.nextDouble(scenario.minSpeed, scenario.maxSpeed));
            track.setHeading(random.nextDouble() * 360);

            // Two-stage classification, see Scenario class Javadoc for the full rationale.
            // Stage 1: roll against friendlyProbability.
            // Stage 2 (if not friendly): pick a hostile threshold from the scenario's
            // hostile probability range, then roll against that threshold.
            Track.Classification classification;
            double friendlyRoll = random.nextDouble();
            if (friendlyRoll < scenario.friendlyProbability) {
                classification = Track.Classification.FRIENDLY;
            } else {
                double hostileThreshold = scenario.minHostileProbability +
                        random.nextDouble() * (scenario.maxHostileProbability - scenario.minHostileProbability);
                double hostileRoll = random.nextDouble();
                if (hostileRoll < hostileThreshold) {
                    classification = Track.Classification.HOSTILE;
                } else {
                    classification = Track.Classification.UNKNOWN;
                }
            }

            track.setClassification(classification);

            tracks.add(track);
        }
    }

    /**
     * Advances every track's position by one tick using dead reckoning.
     *
     * Dead reckoning estimates a new position from a known position, a heading,
     * and an elapsed time, assuming constant speed and course. The math here
     * converts knots and seconds into degrees of latitude and longitude:
     *
     * <ul>
     *   <li>Speed is in knots (nautical miles per hour).</li>
     *   <li>The tick interval is 2 seconds, so distance traveled per tick
     *       is {@code speed * (2/3600)} nautical miles.</li>
     *   <li>One degree of latitude ≈ 60 nautical miles, so dividing by 60
     *       converts distance to degrees.</li>
     *   <li>Heading is split into latitude and longitude components using
     *       cosine and sine respectively.</li>
     *   <li>Longitude is corrected by dividing by {@code cos(latitude)} to
     *       account for meridian convergence where lines of longitude grow closer
     *       together as you move away from the equator.</li>
     * </ul>
     *
     * This is a simplified flat-earth approximation. It is accurate enough
     * for short ticks at mid-latitudes but would accumulate error over long
     * simulated voyages or near the poles.
     */
    private void updateTracks() {
        for (Track track : tracks) {
            double speed = track.getSpeed();
            double heading = track.getHeading();

            double distance = speed * (2.0 / 3600);
            double delta = distance / 60;
            double deltaLat = delta * Math.cos(Math.toRadians(heading));
            double deltaLon = (delta * Math.sin(Math.toRadians(heading))) / Math.cos(Math.toRadians(track.getLatitude()));

            track.setLatitude(deltaLat + track.getLatitude());
            track.setLongitude(deltaLon + track.getLongitude());
        }
    }

    /**
     * Registers a listener to be notified on every simulation tick.
     *
     * @param listener the listener to add
     */
    public void addListener(TrackUpdateListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a previously registered listener. No effect if the listener
     * was not registered.
     *
     * @param listener the listener to remove
     */
    public void removeListener(TrackUpdateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners with the current track list.
     *
     * Note: the same list reference is passed to every listener. Listeners
     * are expected to treat it as read-only and to make a defensive copy
     * if they need to retain or modify the data.
     */
    private void notifyListeners() {
        for (TrackUpdateListener listener : listeners) {
            listener.onTracksUpdated(tracks);
        }
    }

    /**
     * Starts the simulation tick loop.
     *
     * Schedules {@link #updateTracks()} and {@link #notifyListeners()} to run
     * every 2 seconds on a dedicated background thread. The thread is marked
     * as a daemon so it does not prevent JVM shutdown when the main window
     * closes.
     *
     * Calling start() more than once will replace the existing scheduler
     * without shutting it down. Callers should call {@link #stop()} first
     * if restarting.
     */
    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> {updateTracks(); notifyListeners();}, 0, 2, TimeUnit.SECONDS);
    }

    /**
     * Stops the simulation tick loop.
     *
     * Initiates an orderly shutdown of the scheduler. In-flight ticks will
     * complete but no further ticks will be scheduled. Safe to call if the
     * simulation has not been started or is already stopped.
     */
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}