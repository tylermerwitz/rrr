package com.example.rrr.dto;

import com.example.rrr.model.ActionType;

public record PlayerAction(
        ActionType type,
        String eventId
) {}
