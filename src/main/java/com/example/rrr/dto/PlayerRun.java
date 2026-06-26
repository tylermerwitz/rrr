package com.example.rrr.dto;

import com.example.rrr.model.GameLocation;
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
    private double bladderPercent;    // 0 - 100 (control remaining; 0 = wetting accident)
    private double bowelPercent;      // 0 - 100 (control remaining; 0 = messing accident)

    /**
     * Accumulating saturation of the currently-worn diaper. These persist across turns and
     * floors within a run (there's no mid-run change), and only reset to 0 — a fresh diaper —
     * on a new run or respawn. Each wetting/messing accident adds to them; the magnitude lets
     * the narrator describe not just whether the diaper is used, but how heavily.
     */
    private int diaperWetness;        // 0 = dry, grows with each wetting
    private int diaperMessiness;      // 0 = clean, grows with each messing

    private int currentFloor;
    private boolean active;

    /** Id of the room the player currently occupies on the active floor (null in the hub). */
    private String currentRoomId;

    @Enumerated(EnumType.STRING)
    private GameLocation location;

    private LocalDateTime startedAt;

    private long worldSeed;

    /**
     * Number of random draws taken so far this run. Persisted so the RNG can be
     * replayed to its current position after each stateless request reload —
     * otherwise the transient Random resets to the seed every turn and every roll
     * is identical.
     */
    private long randomDraws;

    /**
     * The most recent screen (narration + choices), serialized as JSON, so the floor screen can be
     * re-rendered on a plain state reload without re-rolling the outcome. Reset with the floor.
     */
    @JsonIgnore
    @Lob
    private String lastNarration;

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
        this.diaperWetness = 0;
        this.diaperMessiness = 0;
        this.currentFloor = 1;
        this.active = true;
        this.location = GameLocation.HUB;
        this.startedAt = LocalDateTime.now();
        this.worldSeed = seed;
        this.randomDraws = 0;
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

    /** Restores bladder control, clamped at the player's permanent max. The inverse of a drain. */
    public void fillBladder(double amount) {
        this.bladderPercent = Math.min(player.getBladderBase(), this.bladderPercent + amount);
    }

    /** Restores bowel control, clamped at the player's permanent max. The inverse of a drain. */
    public void fillBowel(double amount) {
        this.bowelPercent = Math.min(player.getBowelBase(), this.bowelPercent + amount);
    }

    public void resetBladder() {
        this.bladderPercent = player.getBladderBase();
    }

    public void resetBowel() {
        this.bowelPercent = player.getBowelBase();
    }

    /* ========================
       DIAPER STATE
       ======================== */

    /** Records a wetting into the diaper, deepening its saturation. */
    public void wetDiaper(int amount) {
        this.diaperWetness += amount;
    }

    /** Records a messing into the diaper, deepening its soiling. */
    public void messDiaper(int amount) {
        this.diaperMessiness += amount;
    }

    /** A fresh, clean, dry diaper. */
    public void changeDiaper() {
        this.diaperWetness = 0;
        this.diaperMessiness = 0;
    }

    public boolean isDiaperWet() {
        return diaperWetness > 0;
    }

    public boolean isDiaperMessy() {
        return diaperMessiness > 0;
    }

    /* ========================
       FLOOR PROGRESSION
       ======================== */

    public void advanceFloor() {
        this.currentFloor++;
    }

    /* ========================
       HUB / FLOOR TRANSITIONS
       ======================== */

    public void enterFloor() {
        this.location = GameLocation.FLOOR;
        this.currentFloor = 1;
        this.currentRoomId = "START";
        changeDiaper();
        clearNarration();
    }

    public void returnToHub() {
        this.location = GameLocation.HUB;
        this.currentRoomId = null;
        clearNarration();
    }

    /** Drops the persisted screen so the next floor starts fresh. */
    public void clearNarration() {
        this.lastNarration = null;
    }

    /* ========================
       RESPAWN
       ======================== */

    public void resetForRespawn() {
        this.humiliation = 0;
        this.bladderPercent = player.getBladderBase();
        this.bowelPercent = player.getBowelBase();
        this.currentFloor = 1;
        this.currentRoomId = null;
        this.location = GameLocation.HUB;
        this.active = false;
        changeDiaper();
        clearNarration();
    }

    public void equip(Equipment equipment) {
        // actual persistence handled by service layer
    }

    /**
     * The single source of randomness for a turn. Lazily rebuilds the RNG from the
     * seed and fast-forwards it past every draw already taken this run, so outcomes
     * advance across stateless reloads instead of repeating.
     */
    public double nextDouble() {
        ensureRandom();
        double value = random.nextDouble();
        randomDraws++;
        return value;
    }

    private void ensureRandom() {
        if (random == null) {
            random = new Random(worldSeed);
            for (long i = 0; i < randomDraws; i++) {
                random.nextDouble();
            }
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

    public int getDiaperWetness()
    {
        return diaperWetness;
    }

    public void setDiaperWetness(int diaperWetness)
    {
        this.diaperWetness = diaperWetness;
    }

    public int getDiaperMessiness()
    {
        return diaperMessiness;
    }

    public void setDiaperMessiness(int diaperMessiness)
    {
        this.diaperMessiness = diaperMessiness;
    }

    public int getCurrentFloor()
    {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor)
    {
        this.currentFloor = currentFloor;
    }

    public String getCurrentRoomId()
    {
        return currentRoomId;
    }

    public void setCurrentRoomId(String currentRoomId)
    {
        this.currentRoomId = currentRoomId;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public GameLocation getLocation()
    {
        return location;
    }

    public void setLocation(GameLocation location)
    {
        this.location = location;
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

    public String getLastNarration()
    {
        return lastNarration;
    }

    public void setLastNarration(String lastNarration)
    {
        this.lastNarration = lastNarration;
    }
}
