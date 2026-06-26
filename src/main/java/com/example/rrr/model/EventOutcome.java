package com.example.rrr.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class EventOutcome {

    private boolean success;

    private int humiliationAdded;
    private int regressionAdded;
    private int arousalAdded;

    /**
     * Which kind(s) of accident this turn produced, so the client can tell the player exactly what
     * happened (wet, messed, or both) rather than a generic "accident". These cover BOTH the chosen
     * branch's authored consequence and any passive desperation accident the engine folds in, so
     * they're settable.
     */
    @Setter
    private boolean wetted;
    @Setter
    private boolean messed;

    private boolean triggeredDeath;

    /** True if either kind of accident occurred this turn. */
    public boolean isAccident() {
        return wetted || messed;
    }

    /**
     * The authored resolution prose for this outcome (the chosen branch's narration). The engine
     * may append a line for any passive accident that also fired this turn, so this is settable.
     */
    @Setter
    private String narration;

    /** Name of an equipment item this outcome granted, or null if none — so the client can announce it. */
    private String itemReward;

    /** True if this outcome put the player in a fresh diaper (wetness/mess reset to 0/0). */
    private boolean diaperChanged;
}
