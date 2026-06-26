package com.example.rrr.model;

public record ShopListing(
        Long equipmentId,
        String name,
        EquipmentType type,
        int flatDefense,
        int speedModifier,
        double bladderModifier,
        double bowelModifier,
        int unlockCost,
        int requiredRegressionTier,
        int requiredArousalTier,
        boolean legendary,
        boolean owned,
        boolean eligible
) {}
