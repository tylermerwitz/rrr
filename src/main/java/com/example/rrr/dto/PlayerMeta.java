package com.example.rrr.dto;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "players")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int totalDeaths;

    private int regressionPoints;  // 0 - 4000+
    private int arousalPoints;     // 0 - 4000+

    private double bladderBase;    // permanent max %
    private double bowelBase;

    private int coins;

    private LocalDateTime createdAt;

    @Version
    private Long version;

    /* ========================
       CONSTRUCTOR
       ======================== */

    public PlayerMeta(String name) {
        this.name = name;
        this.totalDeaths = 0;
        this.regressionPoints = 0;
        this.arousalPoints = 0;
        this.bladderBase = 100.0;
        this.bowelBase = 100.0;
        this.coins = 0;
        this.createdAt = LocalDateTime.now();
    }

    /* ========================
       PROGRESSION METHODS
       ======================== */

    public void addRegression(int amount) {
        this.regressionPoints += amount;
    }

    public void addArousal(int amount) {
        this.arousalPoints += amount;
    }

    public void transferRegressionToArousal(int amount) {
        if (regressionPoints < amount) {
            throw new IllegalArgumentException("Not enough regression points");
        }
        this.regressionPoints -= amount;
        this.arousalPoints += amount;
    }

    public void incrementDeaths() {
        this.totalDeaths++;
    }

    /* ========================
       CONTROL DEGRADATION
       ======================== */

    public void reduceBladderControl(double amount) {
        this.bladderBase = Math.max(0, this.bladderBase - amount);
    }

    public void reduceBowelControl(double amount) {
        this.bowelBase = Math.max(0, this.bowelBase - amount);
    }

    /* ========================
       ECONOMY
       ======================== */

    public void addCoins(int amount) {
        this.coins += amount;
    }

    public void spendCoins(int amount) {
        if (coins < amount) {
            throw new IllegalArgumentException("Not enough coins");
        }
        this.coins -= amount;
    }
}
