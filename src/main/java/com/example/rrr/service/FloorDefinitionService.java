package com.example.rrr.service;

import com.example.rrr.model.FloorDefinition;
import org.springframework.stereotype.Service;

@Service
public class FloorDefinitionService {

    public FloorDefinition getDefinition(int floorNumber) {

        return switch (floorNumber) {

            case 1 -> new FloorDefinition(
                    1,
                    "Daycare",
                    6,
                    8,
                    1,
                    true
            );

            case 2 -> new FloorDefinition(
                    2,
                    "Nursery Ward",
                    7,
                    9,
                    2,
                    true
            );

            case 3 -> new FloorDefinition(
                    3,
                    "Playground",
                    8,
                    10,
                    3,
                    false
            );

            default -> new FloorDefinition(
                    floorNumber,
                    "Unknown Floor",
                    8,
                    12,
                    floorNumber,
                    false
            );
        };
    }
}
