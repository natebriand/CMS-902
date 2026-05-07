package com.cms902.model;

import java.util.UUID;

/**
 * Represents a single tactical track in the combat management system.
 *
 * A track is any detected object such as air, surface, subsurface, or land
 * that the system is currently aware of. Each track has a unique identifier,
 * a designation, a kinematic state (position, heading, speed), and tactical
 * metadata (classification, threat level).
 *
 * Tracks are created by the data source layer (currently SimulationEngine)
 * and managed by TrackManager, which holds the authoritative tactical picture.
 */
public class Track {

    /**
     * Tactical classification of a track which indicates its allegiance.
     */
    public enum Classification {
        UNKNOWN, FRIENDLY, NEUTRAL, HOSTILE, SUSPECT
    }

    /**
     * The domain the track operates in.
     */
    public enum TrackType {
        AIR, LAND, SURFACE, SUBSURFACE
    }

    /**
     * Threat level posed by the track.
     */
    public enum ThreatLevel {
        NONE, LOW, MEDIUM, HIGH, CRITICAL
    }

    // Unique identifier assigned once at construction and never changes.
    // Used internally to distinguish tracks.
    private final String trackId;

    private String designation;
    private Classification classification;
    private TrackType trackType;
    private ThreatLevel threatLevel;

    private double latitude;
    private double longitude;
    private double heading;
    private double speed;


    /**
     * Constructs a new track with the given identity and initial position.
     *
     * Classification defaults to UNKNOWN and threat level to NONE — these
     * are typically set later by the simulation or by operator action once
     * more information about the track is available. Heading and speed
     * default to zero and should be set explicitly before the track is
     * considered fully initialized.
     *
     * @param designation human-readable label for the track (e.g. "T-001")
     * @param trackType the physical domain of the track
     * @param latitude initial latitude in decimal degrees
     * @param longitude initial longitude in decimal degrees
     */
    public Track(String designation, TrackType trackType, double latitude, double longitude) {
        this.trackId = UUID.randomUUID().toString();
        this.designation = designation;
        this.trackType = trackType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.classification = Classification.UNKNOWN;
        this.threatLevel = ThreatLevel.NONE;
        this.heading = 0;
        this.speed = 0;
    }


    // Getters and setters
    public String getTrackId() { return trackId; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public Classification getClassification() { return classification; }
    public void setClassification(Classification classification) { this.classification = classification; }

    public TrackType getTrackType() { return trackType; }
    public void setTrackType(TrackType trackType) { this.trackType = trackType; }

    public ThreatLevel getThreatLevel() { return threatLevel; }
    public void setThreatLevel(ThreatLevel threatLevel) { this.threatLevel = threatLevel; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getHeading() { return heading; }
    public void setHeading(double heading) { this.heading = heading; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }


    /**
     * Returns a single-line summary of the track's current state.
     * Intended for diagnostic and console output, not for UI display.
     *
     * @return formatted string with designation, type, classification,
     *         position, heading, and speed
     */
    @Override
    public String toString() {
        return String.format("Track[%s | %s | %s | %.4f°N %.4f°E | HDG %.0f° SPD %.0f kts]",
                designation, trackType, classification, latitude, longitude, heading, speed);
    }
}