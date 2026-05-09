package com.cms902.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Unit tests for the Track class.
 */
class TrackTest {

    @Test
    void newTrackHasGivenInitialValues() {
        Track track = new Track("T-001", Track.TrackType.AIR, 50.0, -35.0);

        assertEquals("T-001", track.getDesignation());
        assertEquals(Track.TrackType.AIR, track.getTrackType());
        assertEquals(50.0, track.getLatitude());
        assertEquals(-35.0, track.getLongitude());
    }

    @Test
    void newTrackHasDefaultClassificationAndThreatLevel() {
        Track track = new Track("T-001", Track.TrackType.AIR, 50.0, -35.0);

        assertEquals(Track.Classification.UNKNOWN, track.getClassification());
        assertEquals(Track.ThreatLevel.NONE, track.getThreatLevel());
    }

    @Test
    void newTrackHasZeroHeadingAndSpeed() {
        Track track = new Track("T-001", Track.TrackType.AIR, 50.0, -35.0);

        assertEquals(0, track.getHeading());
        assertEquals(0, track.getSpeed());
    }

    @Test
    void newTrackHasNonNullId() {
        Track track = new Track("T-001", Track.TrackType.AIR, 50.0, -35.0);

        assertNotNull(track.getTrackId());
    }

    @Test
    void twoTracksHaveDifferentIds() {
        Track track1 = new Track("T-001", Track.TrackType.AIR, 50.0, -35.0);
        Track track2 = new Track("T-002", Track.TrackType.SURFACE, 51.0, -36.0);

        assertNotEquals(track1.getTrackId(), track2.getTrackId());
    }

    @Test
    void settersUpdateValues() {
        Track track = new Track("T-001", Track.TrackType.AIR, 50.0, -35.0);

        track.setSpeed(15.5);
        track.setHeading(90.0);
        track.setClassification(Track.Classification.HOSTILE);
        track.setThreatLevel(Track.ThreatLevel.HIGH);

        assertEquals(15.5, track.getSpeed());
        assertEquals(90.0, track.getHeading());
        assertEquals(Track.Classification.HOSTILE, track.getClassification());
        assertEquals(Track.ThreatLevel.HIGH, track.getThreatLevel());
    }
}