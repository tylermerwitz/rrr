package com.example.rrr.model;

/**
 * What passive desperation-accidents (bladder/bowel bottoming out) fired during a turn, so the
 * orchestration layer can narrate them — these aren't authored per-event, so they get a canned
 * line appended to the resolution prose rather than being left silent.
 */
public record AccidentReport(boolean wetted, boolean messed) {

    public static final AccidentReport NONE = new AccidentReport(false, false);

    public boolean any() {
        return wetted || messed;
    }
}
