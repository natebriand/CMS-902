package com.cms902.model;

public enum Scenario {
    EXERCISE(
            "Exercise",
            10, 20,
            0.0, 0.0,
            1,
            0, 10
    ),

    PEACETIME(
            "Peacetime Patrol",
            5, 10,
            0.0, 0.05,
            0.6,
            5, 20
    ),

    ELEVATED_THREAT(
            "Elevated Threat",
            10, 15,
            0.05, 0.15,
            0.4,
            10, 35
    ),

    WARTIME(
            "Wartime Operations",
            15, 30,
            0.3, 0.6,
            0.25,
            15, 50
    );

    public final String displayName;
    public final int minTracks;
    public final int maxTracks;
    public final double minHostileProbability;
    public final double maxHostileProbability;
    public final double friendlyProbability;
    public final int minSpeed;
    public final int maxSpeed;

    Scenario(String displayName, int minTracks, int maxTracks, double minHostileProbability,
             double maxHostileProbability, double friendlyProbability, int minSpeed, int maxSpeed) {
        this.displayName = displayName;
        this.minTracks = minTracks;
        this.maxTracks = maxTracks;
        this.minHostileProbability = minHostileProbability;
        this.maxHostileProbability = maxHostileProbability;
        this.friendlyProbability = friendlyProbability;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
    }
}