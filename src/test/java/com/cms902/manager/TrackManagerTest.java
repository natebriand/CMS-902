package com.cms902.manager;

import com.cms902.model.Track;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyList;

/**
 * Unit tests for TrackManager.
 */
class TrackManagerTest {

    /** Helper that builds a small list of tracks for use in tests. */
    private List<Track> sampleTracks() {
        List<Track> tracks = new ArrayList<>();
        tracks.add(new Track("T-001", Track.TrackType.AIR, 50.0, -35.0));
        tracks.add(new Track("T-002", Track.TrackType.SURFACE, 51.0, -36.0));
        return tracks;
    }

    @Test
    void onTracksUpdatedStoresTheTracks() {
        TrackManager manager = new TrackManager();
        manager.onTracksUpdated(sampleTracks());

        assertEquals(2, manager.getTracks().size());
    }

    @Test
    void getTracksReturnsADefensiveCopy() {
        TrackManager manager = new TrackManager();
        manager.onTracksUpdated(sampleTracks());

        List<Track> firstCopy = manager.getTracks();
        firstCopy.clear();

        // The internal list should be unaffected by clearing the returned copy
        // as it does not reference original track list.
        assertEquals(2, manager.getTracks().size());
    }

    @Test
    void onTracksUpdatedMakesADefensiveCopyOfInput() {
        TrackManager manager = new TrackManager();
        List<Track> incoming = sampleTracks();
        manager.onTracksUpdated(incoming);

        // Modifying the original list after passing it should not affect the manager.
        incoming.clear();

        assertEquals(2, manager.getTracks().size());
    }

    @Test
    void getTracksReturnsADifferentListInstanceEachCall() {
        TrackManager manager = new TrackManager();
        manager.onTracksUpdated(sampleTracks());

        List<Track> first = manager.getTracks();
        List<Track> second = manager.getTracks();

        assertNotSame(first, second);
    }

    @Test
    void listenerIsNotifiedWhenTracksAreUpdated() {
        TrackManager manager = new TrackManager();
        TrackManager.TrackPictureListener listener = mock(TrackManager.TrackPictureListener.class);
        manager.addListener(listener);

        manager.onTracksUpdated(sampleTracks());

        // Mockito syntax: verify(mock, count).methodName(args) checks how many times
        // that method was called. In this case, it should be once.
        verify(listener, times(1)).onTrackPictureUpdated(anyList());
    }

    @Test
    void removedListenerIsNotNotified() {
        TrackManager manager = new TrackManager();
        TrackManager.TrackPictureListener listener = mock(TrackManager.TrackPictureListener.class);
        manager.addListener(listener);
        manager.removeListener(listener);

        manager.onTracksUpdated(sampleTracks());

        // Mockito syntax: verify(mock, count).methodName(args) checks how many times
        // that method was called. In this case, it should be never.
        verify(listener, never()).onTrackPictureUpdated(anyList());
    }
}