package com.example.rrr.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Random;

@Entity
@Table(name = "runs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private PlayerMeta player;

    private int runNumber;

    private int humiliation;          // 0 - 2000
    private double bladderPercent;    // 0 - 100
    private double bowelPercent;      // 0 - 100

    private int currentFloor;
    private boolean active;

    private LocalDateTime startedAt;

    private long worldSeed;

    @JsonIgnore
    @Transient
    private Random random;

    @Version
    private Long version;

    /* ========================
       CONSTRUCTOR
       ======================== */

    public PlayerRun(PlayerMeta player) {
        this(player, new Random().nextLong());
    }

    public PlayerRun(PlayerMeta player, long seed) {
        this.player = player;
        this.runNumber = player.getTotalDeaths() + 1;
        this.humiliation = 0;
        this.bladderPercent = player.getBladderBase();
        this.bowelPercent = player.getBowelBase();
        this.currentFloor = 1;
        this.active = true;
        this.startedAt = LocalDateTime.now();
        this.worldSeed = seed;
        this.random = new Random(seed);
    }

    /* ========================
       HUMILIATION METHODS
       ======================== */

    public void addHumiliation(int amount) {
        this.humiliation += amount;
    }

    public void reduceHumiliation(int amount) {
        this.humiliation = Math.max(0, this.humiliation - amount);
    }

    public boolean isBroken() {
        return humiliation >= 2000;
    }

    /* ========================
       CONTROL METHODS
       ======================== */

    public void drainBladder(double amount) {
        this.bladderPercent = Math.max(0, this.bladderPercent - amount);
    }

    public void drainBowel(double amount) {
        this.bowelPercent = Math.max(0, this.bowelPercent - amount);
    }

    public void resetBladder() {
        this.bladderPercent = player.getBladderBase();
    }

    public void resetBowel() {
        this.bowelPercent = player.getBowelBase();
    }

    /* ========================
       FLOOR PROGRESSION
       ======================== */

    public void advanceFloor() {
        this.currentFloor++;
    }

    /* ========================
       RESPAWN
       ======================== */

    public void resetForRespawn() {
        this.humiliation = 0;
        this.bladderPercent = player.getBladderBase();
        this.bowelPercent = player.getBowelBase();
        this.currentFloor = 1;
        this.active = false;
    }

    public void equip(Equipment equipment) {
        // actual persistence handled by service layer
    }

    @PostLoad
    private void initRandomAfterLoad() {
        if (this.random == null) {
            this.random = new Random(this.worldSeed);
        }
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public PlayerMeta getPlayer()
    {
        return player;
    }

    public void setPlayer(PlayerMeta player)
    {
        this.player = player;
    }

    public int getRunNumber()
    {
        return runNumber;
    }

    public void setRunNumber(int runNumber)
    {
        this.runNumber = runNumber;
    }

    public int getHumiliation()
    {
        return humiliation;
    }

    public void setHumiliation(int humiliation)
    {
        this.humiliation = humiliation;
    }

    public double getBladderPercent()
    {
        return bladderPercent;
    }

    public void setBladderPercent(double bladderPercent)
    {
        this.bladderPercent = bladderPercent;
    }

    public double getBowelPercent()
    {
        return bowelPercent;
    }

    public void setBowelPercent(double bowelPercent)
    {
        this.bowelPercent = bowelPercent;
    }

    public int getCurrentFloor()
    {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor)
    {
        this.currentFloor = currentFloor;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public LocalDateTime getStartedAt()
    {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt)
    {
        this.startedAt = startedAt;
    }

    public long getWorldSeed()
    {
        return worldSeed;
    }

    public void setWorldSeed(long worldSeed)
    {
        this.worldSeed = worldSeed;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public Random getRandom() {
        return random;
    }
}
