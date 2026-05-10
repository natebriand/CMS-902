package com.cms902.manager;

import com.cms902.model.Track;
import com.cms902.simulation.SimulationEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the current track picture and updates the UI listeners (RadarDisplay).
 * Sits between the data source (SimulationEngine) and the UI.
 */
public class TrackManager implements SimulationEngine.TrackUpdateListener {

    /**
     * Callback interface for components to be notified of tactical picture updates.
     * This will be implemented by RadarDisplay.
     */
    public interface TrackPictureListener {
        void onTrackPictureUpdated(List<Track> tracks);
    }

    private List<Track> tracks;
    private final List<TrackPictureListener> listeners = new ArrayList<>();

    public TrackManager() {
        this.tracks = new ArrayList<>();
    }

    /**
     * The incoming list is copied into an internal list before being stored.
     * The data source thread keeps mutating its own list so we need our own
     * snapshot before iterating.
     */
    @Override
    public void onTracksUpdated(List<Track> incomingTracks) {
        this.tracks = new ArrayList<>(incomingTracks);
        notifyListeners();
    }

    public List<Track> getTracks() {
        return new ArrayList<>(tracks);
    }

    public void addListener(TrackPictureListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TrackPictureListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners with a defensive copy of the
     * current track list.
     */
    private void notifyListeners() {
        for (TrackPictureListener listener : listeners) {
            listener.onTrackPictureUpdated(getTracks());
        }
    }
}