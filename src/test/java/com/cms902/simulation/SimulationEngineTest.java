package com.cms902.simulation;

import com.cms902.model.Scenario;
import com.cms902.model.Track;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimulationEngine primarily on initial track generation.
 * Threading and tick behavior are not tested yet.
 */
class SimulationEngineTest {

    @Test
    void trackCountIsWithinScenarioBounds() {
        SimulationEngine engine = new SimulationEngine(Scenario.PEACETIME);
        int count = engine.getTracks().size();

        assertTrue(count >= Scenario.PEACETIME.minTracks,
                "Expected at least " + Scenario.PEACETIME.minTracks + " tracks, got " + count);
        assertTrue(count <= Scenario.PEACETIME.maxTracks,
                "Expected at most " + Scenario.PEACETIME.maxTracks + " tracks, got " + count);
    }

    @Test
    void trackSpeedsAreWithinScenarioBounds() {
        SimulationEngine engine = new SimulationEngine(Scenario.WARTIME);

        for (Track track : engine.getTracks()) {
            assertTrue(track.getSpeed() >= Scenario.WARTIME.minSpeed,
                    "Track " + track.getDesignation() + " has speed below scenario minimum");
            assertTrue(track.getSpeed() <= Scenario.WARTIME.maxSpeed,
                    "Track " + track.getDesignation() + " has speed above scenario maximum");
        }
    }

    @Test
    void trackHeadingsAreInValidRange() {
        SimulationEngine engine = new SimulationEngine(Scenario.PEACETIME);

        for (Track track : engine.getTracks()) {
            assertTrue(track.getHeading() >= 0);
            assertTrue(track.getHeading() < 360);
        }
    }

    @Test
    void trackPositionsAreInOperatingArea() {
        SimulationEngine engine = new SimulationEngine(Scenario.PEACETIME);

        for (Track track : engine.getTracks()) {
            assertTrue(track.getLatitude() >= 48.0 && track.getLatitude() <= 52.0,
                    "Track " + track.getDesignation() + " latitude out of range");
            assertTrue(track.getLongitude() >= -40.0 && track.getLongitude() <= -30.0,
                    "Track " + track.getDesignation() + " longitude out of range");
        }
    }

    @Test
    void allTracksHaveNonNullClassification() {
        SimulationEngine engine = new SimulationEngine(Scenario.PEACETIME);

        for (Track track : engine.getTracks()) {
            assertNotNull(track.getClassification());
        }
    }

    @Test
    void exerciseScenarioProducesNoHostiles() {
        SimulationEngine engine = new SimulationEngine(Scenario.EXERCISE);

        for (Track track : engine.getTracks()) {
            assertNotSame(Track.Classification.HOSTILE, track.getClassification(),
                    "EXERCISE scenario should not produce hostile tracks");
        }
    }
}