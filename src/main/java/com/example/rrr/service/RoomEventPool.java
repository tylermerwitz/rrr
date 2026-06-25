package com.example.rrr.service;

import com.example.rrr.model.GameEvent;
import com.example.rrr.model.RoomType;

import java.util.List;

public interface RoomEventPool {

    List<GameEvent> getEventsFor(RoomType type, int floorNumber);
}
