package com.example.rrr.dto;

import com.example.rrr.model.EventCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameEventDefinition {

    private String id;
    private EventCategory category;
    private int baseHumiliation;
    private int regressionGain;
    private int arousalGain;
    private double baseSuccessChance;
    private boolean canCauseAccident;
}