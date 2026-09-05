package com.cms902.model;

/** Scenarios that drive how SimulationEngine generates its initial track set. */
public enum Scenario {

    EXERCISE(
            "Exercise",
            10, 20,
            1.0, 0.0, 0.0, 0.0, 0.0,
            0, 10
    ),

    PEACETIME(
            "Peacetime Patrol",
            5, 10,
            0.8, 1.0, 0.0, 0.0, 0.0,
            5, 20
    ),

    ELEVATED_THREAT(
            "Elevated Threat",
            10, 15,
            0.4, 0.5, 0.8, 0.9, 1.0,
            10, 35
    ),

    WARTIME(
            "Wartime Operations",
            15, 30,
            0.2, 0.3, 0.8, 0.9, 1.0,
            15, 50
    );


    public final String displayName;
    public final int minTracks;
    public final int maxTracks;
    public final double friendlyRange;
    public final double neutralRange;
    public final double hostileRange;
    public final double suspectRange;
    public final double unknownRange;
    public final int minSpeed;
    public final int maxSpeed;

    Scenario(String displayName, int minTracks, int maxTracks, double friendlyRange,
             double neutralRange, double hostileRange, double suspectRange, double unknownRange, int minSpeed, int maxSpeed) {
        this.displayName = displayName;
        this.minTracks = minTracks;
        this.maxTracks = maxTracks;
        this.friendlyRange = friendlyRange;
        this.neutralRange = neutralRange;
        this.hostileRange = hostileRange;
        this.suspectRange = suspectRange;
        this.unknownRange = unknownRange;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
    }
}