package com.example.rrr.model;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import lombok.Getter;

import java.util.List;

@Getter
public class HubStateResponse {

    private static final List<HubOption> OPTIONS = List.of(
            HubOption.CHANGE_EQUIPMENT,
            HubOption.VISIT_SHOP,
            HubOption.LOOK_IN_MIRROR,
            HubOption.ENTER_REGRESSION_REALM
    );

    private final PlayerMeta meta;
    private final PlayerRun run;
    private final List<HubOption> options;

    public HubStateResponse(PlayerMeta meta, PlayerRun run) {
        this.meta = meta;
        this.run = run;
        this.options = OPTIONS;
    }
}
