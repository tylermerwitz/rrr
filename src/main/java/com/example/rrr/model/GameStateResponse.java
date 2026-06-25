package com.example.rrr.model;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GameStateResponse {

    private PlayerMeta meta;
    private PlayerRun run;
    private List<RoomNode> availableRooms;
}
