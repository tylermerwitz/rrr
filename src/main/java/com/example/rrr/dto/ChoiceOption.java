package com.example.rrr.dto;

import com.example.rrr.model.ActionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChoiceOption {

    private String id;
    private String text;
    private String riskLevel; // SAFE, MODERATE, RISKY

    /** The check category this choice tests (STEALTH, SOCIAL, …), shown so the player can pick by aptitude. */
    private String category;

    /** Resolved from riskLevel — the action the backend runs if this choice is taken. */
    private ActionType actionType;

    /** The event this choice acts on, so the choice can be submitted directly. */
    private String eventId;

    public ChoiceOption(String id, String text, String riskLevel) {
        this.id = id;
        this.text = text;
        this.riskLevel = riskLevel;
    }
}
