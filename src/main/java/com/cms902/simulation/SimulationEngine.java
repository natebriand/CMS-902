package com.cms902.simulation;

import com.cms902.model.Track;
import com.cms902.model.Scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimulationEngine {
    public interface TrackUpdateListener {
        void onTracksUpdated(List<Track> tracks);
    }

    private final Scenario scenario; // Current scenario
    private final List<Track> tracks; // List of current tracks
    private final List<TrackUpdateListener> listeners; // List of listeners
    private ScheduledExecutorService scheduler;
    private final Random random = new Random();

    public SimulationEngine(Scenario scenario) {
        this.scenario = scenario;
        this.tracks = new ArrayList<>();
        this.listeners = new ArrayList<>();
        generateTracks();
    }

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

            Track.Classification classification;
            double friendlyRoll = random.nextDouble();
            if (friendlyRoll < scenario.friendlyProbability) {
                classification = Track.Classification.FRIENDLY;
            } else {
                // Not friendly, pick a random hostile threshold from within
                // the scenario's hostile probability range (min to max)
                double hostileThreshold = scenario.minHostileProbability +
                        random.nextDouble() * (scenario.maxHostileProbability - scenario.minHostileProbability);
                // Roll again, if this roll falls below the threshold, track is hostile
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

    private void updateTracks() {
        for (Track track : tracks) {
            double speed = track.getSpeed();
            double heading = track.getHeading();

            double distance = speed * (2.0 / 3600);
            double delta = distance / 60;
            // Split delta into lat and lon components based on heading
            double deltaLat = delta * Math.cos(Math.toRadians(heading));
            // Correct for longitude compression as we move away from equator
            double deltaLon = (delta * Math.sin(Math.toRadians(heading))) / Math.cos(Math.toRadians(track.getLatitude()));

            track.setLatitude(deltaLat + track.getLatitude());
            track.setLongitude(deltaLon + track.getLongitude());
        }
    }


    public void addListener(TrackUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TrackUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (TrackUpdateListener listener : listeners) {
            listener.onTracksUpdated(tracks);
        }
    }


    public void start() {
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
}

