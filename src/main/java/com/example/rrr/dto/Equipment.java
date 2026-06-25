package com.example.rrr.dto;

import com.example.rrr.model.EquipmentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "equipment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private EquipmentType type;

    private int flatDefense;      // subtract after % mitigation
    private int speedModifier;    // can be negative
    private double bladderModifier; // future use
    private double bowelModifier;

    private int unlockCost;

    private int requiredRegressionTier;
    private int requiredArousalTier;

    private boolean legendary;

    public Equipment(
            String name,
            EquipmentType type,
            int flatDefense,
            int speedModifier,
            double bladderModifier,
            double bowelModifier,
            int unlockCost,
            int requiredRegressionTier,
            int requiredArousalTier,
            boolean legendary
    ) {
        this.name = name;
        this.type = type;
        this.flatDefense = flatDefense;
        this.speedModifier = speedModifier;
        this.bladderModifier = bladderModifier;
        this.bowelModifier = bowelModifier;
        this.unlockCost = unlockCost;
        this.requiredRegressionTier = requiredRegressionTier;
        this.requiredArousalTier = requiredArousalTier;
        this.legendary = legendary;
    }
}
