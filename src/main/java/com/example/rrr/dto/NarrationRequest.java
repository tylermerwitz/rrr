package com.example.rrr.dto;

import com.example.rrr.model.EventOutcome;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NarrationRequest {

    private String floorName;
    private int floorNumber;

    private int humiliation;
    private int humiliationMax;

    private int regression;
    private int arousal;

    private double bladderPercent;
    private double bowelPercent;

    private String roomType;

    private EventOutcome outcome;

    private List<String> visibleRoomConnections;

    private List<String> equippedItems;
}
