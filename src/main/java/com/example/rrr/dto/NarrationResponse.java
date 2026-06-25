package com.example.rrr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NarrationResponse {

    private String narration;

    private List<ChoiceOption> choices;
}
