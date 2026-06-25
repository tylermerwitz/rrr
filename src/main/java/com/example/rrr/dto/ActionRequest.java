package com.example.rrr.dto;

import com.example.rrr.model.ActionType;

public record ActionRequest(
        Long playerId,
        String eventId,
        ActionType actionType
) {}