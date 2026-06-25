package com.example.rrr.service;

import com.example.rrr.model.FloorDefinition;
import com.example.rrr.model.FloorInstance;
import com.example.rrr.model.RoomNode;
import com.example.rrr.model.RoomType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class FloorGeneratorService {

    public FloorInstance generateFloor(
            FloorDefinition definition,
            long worldSeed
    ) {

        Random random = new Random(worldSeed + definition.floorNumber());

        int roomCount = random.nextInt(
                definition.maxRooms() - definition.minRooms() + 1
        ) + definition.minRooms();

        List<RoomNode> rooms = new ArrayList<>();

        // Start room
        RoomNode start = new RoomNode("START", RoomType.START);
        rooms.add(start);

        // Generate normal rooms
        for (int i = 1; i < roomCount - 1; i++) {

            RoomType type = rollRoomType(random, definition);

            rooms.add(new RoomNode("R" + i, type));
        }

        // Boss room
        RoomNode boss = new RoomNode("BOSS", RoomType.BOSS);
        rooms.add(boss);

        // Connect linearly for now (can evolve to graph later)
        for (int i = 0; i < rooms.size() - 1; i++) {
            rooms.get(i).connectTo(rooms.get(i + 1));
        }

        return new FloorInstance(definition.floorNumber(), rooms);
    }

    private RoomType rollRoomType(Random random, FloorDefinition def) {

        int roll = random.nextInt(100);

        if (def.hasSafeRoom() && roll < 10) return RoomType.SAFE;
        if (roll < 20) return RoomType.ELITE;
        if (roll < 30) return RoomType.SHOP;

        return RoomType.NORMAL;
    }
}
