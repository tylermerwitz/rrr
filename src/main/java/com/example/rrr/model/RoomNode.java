package com.example.rrr.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RoomNode {

    private final String id;
    private final RoomType type;
    private final List<String> connectedRoomIds;

    public RoomNode(String id, RoomType type) {
        this.id = id;
        this.type = type;
        this.connectedRoomIds = new ArrayList<>();
    }

    public void connectTo(RoomNode other) {
        this.connectedRoomIds.add(other.id);
    }
}
