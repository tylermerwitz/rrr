package com.example.rrr.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventOutcome {

    private boolean success;

    private int humiliationAdded;
    private int regressionAdded;
    private int arousalAdded;

    private boolean triggeredAccident;
    private boolean triggeredDeath;
}
