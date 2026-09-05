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
 * Generates a track set from a scenario and advances it with dead reckoning on a 2s tick.
 * Listeners get the full picture each tick every tick.
 */
public class SimulationEngine {

    /**
     * Callback interface for components to be notified of track updates. This will
     * be implemented by TrackManager.
     */
    public interface TrackUpdateListener {
        void onTracksUpdated(List<Track> tracks);
    }

    private final Scenario scenario;
    private final List<Track> tracks;
    private final List<TrackUpdateListener> listeners;
    private ScheduledExecutorService scheduler;
    private final Random random = new Random();


    public SimulationEngine(Scenario scenario) {
        this.scenario = scenario;
        this.tracks = new ArrayList<>();
        this.listeners = new ArrayList<>();
        generateTracks();
    }

    /**
     * Operating area is 48–52°N, 33–37°W (sample RCN patrol area east of Newfoundland).
     */
    private void generateTracks() {
        for (int i=0; i<random.nextInt(scenario.minTracks, scenario.maxTracks + 1); i++) {
            String designation = String.format("T-%03d", i + 1);
            Track.TrackType[] types = Track.TrackType.values();
            Track.TrackType randomType = types[random.nextInt(types.length)];
            double latitude = 48.0 + random.nextDouble() * 4.0;   // 48.0 – 52.0°N
            double longitude = -37.0 + random.nextDouble() * 4.0; // -37.0 – -33.0°W

            Track track = new Track(designation, randomType, latitude, longitude);

            track.setSpeed(random.nextDouble(scenario.minSpeed, scenario.maxSpeed));
            track.setHeading(random.nextDouble() * 360);

            // Roll randomly between the scenarios ranges for the type of track
            Track.Classification classification;
            double range = random.nextDouble();
            if (range < scenario.friendlyRange) {
                classification = Track.Classification.FRIENDLY;
            } else if (range < scenario.neutralRange) {
                classification = Track.Classification.NEUTRAL;
            } else if (range < scenario.hostileRange) {
                classification = Track.Classification.HOSTILE;
            } else if (range < scenario.suspectRange) {
                classification = Track.Classification.SUSPECT;
            } else {
                classification = Track.Classification.UNKNOWN;
            }

            track.setClassification(classification);

            tracks.add(track);
        }
    }

    /**
     * Flat-earth dead reckoning. 2s real time = 1 sim minute, so 30x timescale.
     */
    private void updateTracks() {
        for (Track track : tracks) {
            double speed = track.getSpeed();
            double heading = track.getHeading();

            double distance = speed * (60.0 / 3600);
            double delta = distance / 60;
            double deltaLat = delta * Math.cos(Math.toRadians(heading));
            double deltaLon = (delta * Math.sin(Math.toRadians(heading))) / Math.cos(Math.toRadians(track.getLatitude()));

            track.setLatitude(deltaLat + track.getLatitude());
            track.setLongitude(deltaLon + track.getLongitude());
        }
    }

    /** Registers a listener to be notified on every simulation tick. */
    public void addListener(TrackUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TrackUpdateListener listener) {
        listeners.remove(listener);
    }

    /** Notifies all registered listeners with the current track list. */
    private void notifyListeners() {
        for (TrackUpdateListener listener : listeners) {
            listener.onTracksUpdated(tracks);
        }
    }

    /** Starts the 2-second tick loop on a daemon thread so the JVM can exit when the UI closes. */
    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) { // if a timer is already running shut it down first
            scheduler.shutdown();
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> {updateTracks(); notifyListeners();}, 0, 2, TimeUnit.SECONDS);
    }

    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    public List<Track> getTracks() {
        return new ArrayList<>(tracks);
    }

    public Scenario getScenario() {
        return scenario;
    }
}