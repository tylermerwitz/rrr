package com.example.rrr.service;

import com.example.rrr.dto.EventCatalog;
import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import com.example.rrr.model.EventRequirements;
import com.example.rrr.model.GameEvent;
import com.example.rrr.model.RoomType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The catalog of hand-authored events, loaded once from {@code events_floor_1.json} at startup. Events are
 * immutable definitions used directly (no per-floor scaling or instantiation), so the loaded
 * instances are shared across requests.
 */
@Service
public class EventDefinitionService {

    private final Map<String, GameEvent> events = new LinkedHashMap<>();
    private final List<GameEvent> floorPool;

    public EventDefinitionService(ObjectMapper objectMapper) {
        EventCatalog catalog = load(objectMapper);

        if (catalog.getEvents() == null || catalog.getEvents().isEmpty()) {
            throw new IllegalStateException("events_floor_1.json defines no events");
        }
        for (GameEvent event : catalog.getEvents()) {
            events.put(event.getId(), event);
        }

        // A floor's continuous encounter draws from its NORMAL and ELITE beats; BOSS events are
        // defined but reached deliberately, not by the random floor draw.
        this.floorPool = events.values().stream()
                .filter(e -> e.getRoomType() == RoomType.NORMAL || e.getRoomType() == RoomType.ELITE)
                .toList();

        if (floorPool.isEmpty()) {
            throw new IllegalStateException("events_floor_1.json defines no NORMAL or ELITE floor events");
        }
    }

    private EventCatalog load(ObjectMapper objectMapper) {
        try (InputStream in = new ClassPathResource("events_floor_1.json").getInputStream()) {
            return objectMapper.readValue(in, EventCatalog.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load events_floor_1.json", e);
        }
    }

    public GameEvent getEventInstance(String id) {
        GameEvent event = events.get(id);
        if (event == null) {
            throw new IllegalArgumentException("Unknown event: " + id);
        }
        return event;
    }

    /**
     * Picks the next event for a floor's continuous encounter, drawing from the NORMAL/ELITE pool
     * restricted to the events whose {@link EventRequirements} the player currently meets. The draw
     * uses the run's RNG so it advances deterministically with the rest of the turn. If nothing is
     * eligible (no unconditioned event qualifies), it falls back to the full pool so a floor is never
     * left with nothing to present.
     */
    public GameEvent getRandomEventForFloor(PlayerMeta player, PlayerRun run) {
        List<GameEvent> pool = floorPool.stream()
                .filter(event -> isEligible(event, player, run))
                .toList();
        if (pool.isEmpty()) {
            pool = floorPool;
        }

        int index = (int) (run.nextDouble() * pool.size());
        if (index >= pool.size()) {
            index = pool.size() - 1; // guard the nextDouble()==1.0 edge
        }
        return pool.get(index);
    }

    /** Whether the player's current state satisfies an event's optional gating requirements. */
    private boolean isEligible(GameEvent event, PlayerMeta player, PlayerRun run) {
        EventRequirements req = event.getRequirements();
        if (req == null) {
            return true;
        }
        if (req.getMinFloor() > 0 && run.getCurrentFloor() < req.getMinFloor()) {
            return false;
        }
        if (req.getMaxFloor() > 0 && run.getCurrentFloor() > req.getMaxFloor()) {
            return false;
        }
        if (run.getHumiliation() < req.getMinHumiliation()) {
            return false;
        }
        if (player.getRegressionPoints() < req.getMinRegression()) {
            return false;
        }
        if (player.getArousalPoints() < req.getMinArousal()) {
            return false;
        }
        if (player.getTotalDeaths() < req.getMinDeaths()) {
            return false;
        }
        if (req.isMustBeWet() && !run.isDiaperWet()) {
            return false;
        }
        if (req.isMustBeWetOrMessy() && !run.isDiaperWet() && !run.isDiaperMessy()) {
            return false;
        }
        return true;
    }
}
