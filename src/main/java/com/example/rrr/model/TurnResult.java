package com.example.rrr.model;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TurnResult {

    private PlayerRun runState;
    private PlayerMeta metaState;
    private EventOutcome outcome;
}
