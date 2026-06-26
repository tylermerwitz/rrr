package com.example.rrr.dto;

/** Advance from a resolution screen to the next encounter (the player hit Continue). */
public record ContinueRequest(
        Long playerId
) {}
