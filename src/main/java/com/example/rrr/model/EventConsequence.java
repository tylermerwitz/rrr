package com.example.rrr.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The fully-authored result of one branch of a choice (its success OR its failure): the prose the
 * player reads on the resolution screen, plus the exact mechanical consequences applied to the run.
 * Loaded from {@code events_floor_1.json}; this is the unit of "granular control" over what each outcome does.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventConsequence {

    /** The resolution narration for this branch — shown verbatim, no model in the loop. */
    private String narration;

    private int humiliation;
    private int regression;
    private int arousal;
    private int coins;

    /** Whether this outcome forces a wetting / messing of the diaper. */
    private boolean wet;
    private boolean mess;

    /**
     * Whether this outcome puts the player in a fresh diaper, resetting wetness AND messiness back to
     * 0/0 (Dry / Clean). Applied last — after any passive accident this turn — so a change always
     * leaves the diaper clean. Unlike an accident it does NOT touch the bladder/bowel meters; it only
     * cleans the diaper.
     */
    private boolean changeDiaper;

    /**
     * Extra bladder/bowel control lost this turn (on top of the passive per-turn drain). Unlike
     * {@link #wet}/{@link #mess} this doesn't force an accident outright — it just erodes the meter,
     * which can tip the player into an involuntary accident later (or this same turn if it bottoms out).
     */
    private int bladderDrain;
    private int bowelDrain;

    /**
     * The inverse of {@link #bladderDrain}/{@link #bowelDrain}: control restored this turn (the
     * outcome eased the meter back toward full). Applied via run.fillBladder/fillBowel, which clamp
     * at the player's permanent max so a fill can never push control above {@code bladderBase}.
     */
    private int bladderFill;
    private int bowelFill;

    /**
     * Name of an equipment item this outcome grants (unlocks for the player). Null/blank = none.
     * Meant to be rare — only a few authored branches drop loot. The named item must exist in the
     * equipment catalog; an unknown name is simply ignored. {@code itemGained} is accepted as an
     * alias for the same field (the authored JSON uses both spellings interchangeably).
     */
    @JsonAlias("itemGained")
    private String itemReward;

    /**
     * Authored, NOT yet implemented. A named status effect this outcome would apply (e.g.
     * "Claiming Potty Trained", "Enemy: Strict Nanny"). Captured from {@code events_floor_1.json} so the
     * authored data is preserved for a future status-effect system, but nothing reads it yet — it
     * has no mechanical effect today.
     */
    private String statusEffect;
}
