package com.example.rrr.service;

import com.example.rrr.model.EventCategory;
import com.example.rrr.model.GameEvent;
import com.example.rrr.model.RoomType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultRoomEventPool implements RoomEventPool {

    public List<GameEvent> getEventsFor(RoomType type, int floor) {

        if (type == RoomType.NORMAL) {
            return List.of(
                    new GameEvent("STEALTH_FAIL",
                            EventCategory.BABY_TREATMENT,
                            300 + floor * 50,
                            5,
                            0,
                            0.6,
                            true)
            );
        }

        if (type == RoomType.ELITE) {
            return List.of(
                    new GameEvent("ELITE_ENCOUNTER",
                            EventCategory.BABY_TREATMENT,
                            500 + floor * 100,
                            10,
                            5,
                            0.5,
                            true)
            );
        }

        return List.of();
    }
}
