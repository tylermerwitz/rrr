package com.example.rrr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One of an event's authored options. Carries the button {@link #label}, the odds the attempt
 * works ({@link #successChance}), and a fully-authored {@link #success} and {@link #failure}
 * branch so both outcomes — and their consequences — are hand-tuned per choice. Loaded from
 * {@code events.json}.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventChoice {

    /** The risk tier this choice represents (SAFE / MODERATE / RISKY); also its action id. */
    private ActionType risk;

    /**
     * The kind of check this choice's pass/fail represents (stealth, social, etc.). Tied to the
     * choice rather than the event so a single encounter can offer approaches that test different
     * aptitudes — the player picks the one their character is best suited to right now.
     */
    private EventCategory category;

    /** The text shown on the choice button. */
    private String label;

    /** Probability in [0,1] that taking this choice resolves to its success branch. */
    private double successChance;

    private EventConsequence success;
    private EventConsequence failure;
}
