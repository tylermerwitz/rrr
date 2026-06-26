package com.example.rrr.model;

/**
 * The kind of check a choice's pass/fail represents. Tied to each {@link EventChoice}, this is the
 * aptitude the attempt tests, so a future skill-check layer can scale {@code successChance} off the
 * matching stat. Today it's purely descriptive (surfaced in the choice UI); the scaling hooks come
 * later.
 *
 * <p>The first six are skill aptitudes; the last four scale off a live meter/points stat (a higher
 * value in that stat makes the matching check more likely to succeed).
 */
public enum EventCategory {

    /** Physical strength and general athletic ability. */
    ATHLETICS,
    /** Sneaking around and snagging things undetected. */
    STEALTH,
    /** Physically enduring and stomaching things. */
    GRIT,
    /** Outthinking, strategizing, and problem solving. */
    WIT,
    /** Mentally enduring hardship and asserting your will over others (wisdom). */
    WILLPOWER,
    /** Getting your way with others: deceiving, dealing, persuading. */
    SOCIAL,

    /** Scales with the regression stat — higher regression, likelier to succeed. */
    REGRESSION,
    /** Scales with the arousal stat — higher arousal, likelier to succeed. */
    AROUSAL,
    /** Scales with the bladder stat — higher bladder, likelier to succeed. */
    BLADDER,
    /** Scales with the bowel stat — higher bowel, likelier to succeed. */
    BOWEL
}
