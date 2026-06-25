package com.example.rrr.service;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import org.springframework.stereotype.Service;

@Service
public class DeathService {

    public void checkDeath(PlayerMeta meta, PlayerRun run) {

        if (run.getHumiliation() >= 2000) {
            triggerDeath(meta, run);
        }
    }

    private void triggerDeath(PlayerMeta meta, PlayerRun run) {

        meta.incrementDeaths();

        meta.addRegression(25);
        meta.addArousal(25);

        meta.reduceBladderControl(5);
        meta.reduceBowelControl(5);

        run.resetForRespawn();
    }
}
