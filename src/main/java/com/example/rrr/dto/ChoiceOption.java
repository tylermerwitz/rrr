package com.example.rrr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChoiceOption {

    private String id;
    private String text;
    private String riskLevel; // SAFE, MODERATE, RISKY
}
