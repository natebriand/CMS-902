package com.cms902.model;

/**
 * Defines the operational context for track generation in the simulation.
 *
 * Each scenario represents a distinct tactical environment and bundles together the
 * parameters that shape what the tactical picture looks like. Such as how many tracks
 * appear, how fast they move, and how likely they are to be friendly, hostile,
 * or unknown.
 *
 * Scenarios are consumed by SimulationEngine when generating the initial set
 * of tracks. They are not used at runtime to alter behavior of existing tracks
 * but instead only influence how tracks are created.
 *
 *
 * Classification of a generated track is determined in two stages:
 *
 * Stage 1 — a random value is rolled against {@code friendlyProbability}.
 * If it succeeds, the track is classified FRIENDLY and classification ends.
 *
 * Stage 2 — if the friendly roll failed, a hostile threshold is selected
 * uniformly at random from the range [{@code minHostileProbability},
 * {@code maxHostileProbability}], and a second random value is rolled
 * against that threshold. Success means HOSTILE; failure means UNKNOWN.
 */
public enum Scenario {

    /** Training exercise: many tracks, no hostiles, low speeds. */
    EXERCISE(
            "Exercise",
            10, 20,
            0.0, 0.0,
            1,
            0, 10
    ),

    /** Peacetime patrol: few tracks, mostly friendly or unknown,
     *  occasional low-probability hostile contact. */
    PEACETIME(
            "Peacetime Patrol",
            5, 10,
            0.0, 0.05,
            0.6,
            5, 20
    ),

    /** Elevated tension: moderate track count, moderate hostile probability,
     *  fewer friendlies, moderate speeds. */
    ELEVATED_THREAT(
            "Elevated Threat",
            10, 15,
            0.05, 0.15,
            0.4,
            10, 35
    ),

    /** Wartime combat: high track count, high hostile probability, few friendlies,
     *  high speeds. */
    WARTIME(
            "Wartime Operations",
            15, 30,
            0.3, 0.6,
            0.25,
            15, 50
    );

    // Fields are public final by design: scenarios are
    // immutable and are read directly by SimulationEngine.

    /** Human-readable name suitable for display in the UI. */
    public final String displayName;

    /** Inclusive lower bound on the number of tracks generated for this scenario. */
    public final int minTracks;

    /** Inclusive upper bound on the number of tracks generated for this scenario. */
    public final int maxTracks;

    /** Lower bound of the hostile probability range used in the second classification roll. */
    public final double minHostileProbability;

    /** Upper bound of the hostile probability range used in the second classification roll. */
    public final double maxHostileProbability;

    /** Probability (0.0–1.0) that any given track is classified FRIENDLY on the first roll. */
    public final double friendlyProbability;

    /** Inclusive lower bound on track speed in knots. */
    public final int minSpeed;

    /** Inclusive upper bound on track speed in knots. */
    public final int maxSpeed;

    /**
     * Constructs a scenario with the given track-generation parameters.
     *
     * @param displayName human-readable name for UI display
     * @param minTracks minimum number of tracks to generate
     * @param maxTracks maximum number of tracks to generate
     * @param minHostileProbability lower bound of the hostile probability range
     * @param maxHostileProbability upper bound of the hostile probability range
     * @param friendlyProbability probability of a track being classified friendly
     * @param minSpeed minimum track speed in knots
     * @param maxSpeed maximum track speed in knots
     */
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