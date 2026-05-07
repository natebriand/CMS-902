package com.cms902.manager;

import com.cms902.model.Track;
import com.cms902.simulation.SimulationEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains the authoritative tactical picture between the data
 * source layer and the UI layer.
 *
 * TrackManager sits in the middle of the application's three-layer architecture.
 * It receives raw track updates from the data source (currently
 * {@link SimulationEngine}, eventually a hardware sensor feed), holds the
 * current set of tracks, and notifies UI components when the picture changes.
 *
 * This middleware role serves two purposes:
 *
 * <ul>
 *   <li>Decoupling: the UI never talks directly to the data source.
 *       The data source can be swapped without affecting the UI, and vice
 *       versa.</li>
 *   <li>Operator state ownership: eventually this class will also
 *       hold UI-driven state such as the currently selected track and any
 *       active filters. That state belongs here, not in the data source
 *       as it has nothing to do with where data comes from.</li>
 * </ul>
 *
 * Threading: updates from the data source arrive on a background thread.
 * Defensive copies are made when receiving updates and when handing data
 * out, so no caller ever shares a list reference with this class. This
 * prevents the {@link java.util.ConcurrentModificationException} that
 * would otherwise occur when the UI iterates the list while a new tick
 * is arriving.
 */
public class TrackManager implements SimulationEngine.TrackUpdateListener {

    /**
     * Callback interface for the UI layer to be notified when the tactical
     * picture changes.
     *
     * Implementations receive a defensive copy of the track list and may
     * read it freely without coordinating with this class.
     */
    public interface TrackPictureListener {
        void onTrackPictureUpdated(List<Track> tracks);
    }

    private List<Track> tracks;
    private final List<TrackPictureListener> listeners = new ArrayList<>();

    /**
     * Constructs an empty TrackManager. The track list will populate once
     * a data source is registered and begins delivering updates.
     */
    public TrackManager() {
        this.tracks = new ArrayList<>();
    }

    /**
     * Receives a track update from the data source.
     *
     * The incoming list is copied into an internal list before being stored.
     * This defensive copy is what isolates this class from the data source's
     * thread and without it the UI thread would be reading the same list that
     * the simulation thread is concurrently mutating.
     *
     * @param incomingTracks the current full track list from the data source
     */
    @Override
    public void onTracksUpdated(List<Track> incomingTracks) {
        this.tracks = new ArrayList<>(incomingTracks);
        notifyListeners();
    }

    /**
     * Returns the current tactical picture as a defensive copy.
     *
     * Callers receive a snapshot they may freely read or modify without
     * affecting this class's internal state.
     *
     * @return a copy of the current track list
     */
    public List<Track> getTracks() {
        return new ArrayList<>(tracks);
    }

    /**
     * Registers a listener to be notified when the tactical picture changes.
     *
     * @param listener the listener to add
     */
    public void addListener(TrackPictureListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a previously registered listener. No effect if the listener
     * was not registered.
     *
     * @param listener the listener to remove
     */
    public void removeListener(TrackPictureListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners with a defensive copy of the
     * current track list.
     *
     * Each listener receives its own copy via {@link #getTracks()}, so no
     * listener can affect what another listener sees.
     */
    private void notifyListeners() {
        for (TrackPictureListener listener : listeners) {
            listener.onTrackPictureUpdated(getTracks());
        }
    }
}