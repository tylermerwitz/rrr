package com.example.rrr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Optional gating on a {@link GameEvent}: the conditions that must hold for the event to be eligible
 * for the random floor draw. Every field is a threshold that defaults to 0 = "no constraint", so an
 * authored {@code requirements} block only needs to specify the dimensions it actually cares about.
 * A null {@code requirements} (the common case) means the event is always eligible.
 *
 * <p>Min thresholds are inclusive. {@code maxFloor} of 0 means no upper bound. Floor gating uses the
 * run's {@code currentFloor}; the progression gates read the player's accumulated points / deaths.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventRequirements {

    private int minFloor;
    private int maxFloor;

    private int minRegression;
    private int minArousal;
    private int minHumiliation;
    private int minDeaths;

    /**
     * Diaper-state gates (default false = no constraint). {@code mustBeWet} requires the diaper to
     * currently be wet; {@code mustBeWetOrMessy} requires it to be wet OR messy. Used for events that
     * only make sense once the player has already had an accident this run.
     */
    private boolean mustBeWet;
    private boolean mustBeWetOrMessy;
}
