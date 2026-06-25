package com.example.rrr.model;

import lombok.Getter;

import java.util.List;

@Getter
public class FloorInstance {

    private final int floorNumber;
    private final List<RoomNode> rooms;

    public FloorInstance(int floorNumber, List<RoomNode> rooms) {
        this.floorNumber = floorNumber;
        this.rooms = rooms;
    }
}