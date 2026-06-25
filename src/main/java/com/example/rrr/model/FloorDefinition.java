package com.example.rrr.model;

public record FloorDefinition(
        int floorNumber,
        String name,
        int minRooms,
        int maxRooms,
        int difficultyRating,
        boolean hasSafeRoom
) {}
