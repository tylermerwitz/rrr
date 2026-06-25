package com.example.rrr.service;

import com.example.rrr.dto.GameEventDefinition;
import com.example.rrr.model.EventCategory;
import com.example.rrr.model.GameEvent;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EventDefinitionService {

    private final Map<String, GameEventDefinition> events = new HashMap<>();

    public EventDefinitionService() {

        register(new GameEventDefinition(
                "STEALTH_ATTEMPT",
                EventCategory.BABY_TREATMENT,
                300,
                5,
                0,
                0.6,
                true
        ));

        register(new GameEventDefinition(
                "ELITE_ENCOUNTER",
                EventCategory.BABY_TREATMENT,
                600,
                10,
                5,
                0.5,
                true
        ));
    }

    private void register(GameEventDefinition def) {
        events.put(def.getId(), def);
    }

    public GameEvent getEventInstance(String id, int floorNumber) {

        GameEventDefinition def = events.get(id);

        if (def == null) {
            throw new IllegalArgumentException("Unknown event: " + id);
        }

        // Apply scaling per floor here
        int scaledHumiliation = def.getBaseHumiliation() + (floorNumber * 50);

        return new GameEvent(
                def.getId(),
                def.getCategory(),
                scaledHumiliation,
                def.getRegressionGain(),
                def.getArousalGain(),
                def.getBaseSuccessChance(),
                def.isCanCauseAccident()
        );
    }
}
