package com.cms902.model;

import java.util.UUID;

/** A detected object the system is tracking: air, surface, subsurface, or land. */
public class Track {

    public enum Classification {
        UNKNOWN, FRIENDLY, NEUTRAL, HOSTILE, SUSPECT
    }

    public enum TrackType {
        AIR, LAND, SURFACE, SUBSURFACE
    }

    public enum ThreatLevel {
        NONE, LOW, MEDIUM, HIGH, CRITICAL
    }

    private final String trackId;

    private String designation;
    private Classification classification;
    private TrackType trackType;
    private ThreatLevel threatLevel;

    private double latitude;
    private double longitude;
    private double heading;
    private double speed;


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

    @Override
    public String toString() {
        return String.format("Track[%s | %s | %s | %.4f°N %.4f°E | HDG %.0f° SPD %.0f kts]",
                designation, trackType, classification, latitude, longitude, heading, speed);
    }
}