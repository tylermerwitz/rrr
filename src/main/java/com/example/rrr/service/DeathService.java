package com.example.rrr.service;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import com.example.rrr.repository.PlayerRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeathService {

    private final PlayerRunRepository runRepository;

    /**
     * Returns the run that should be considered active going forward:
     * the same run if the player survived, or a brand-new hub run if they died.
     */
    public PlayerRun checkDeath(PlayerMeta meta, PlayerRun run) {

        if (run.getHumiliation() >= 2000) {
            return triggerDeath(meta, run);
        }

        return run;
    }

    private PlayerRun triggerDeath(PlayerMeta meta, PlayerRun run) {

        meta.incrementDeaths();

        meta.addRegression(25);
        meta.addArousal(25);

        meta.reduceBladderControl(5);
        meta.reduceBowelControl(5);

        run.resetForRespawn();

        return runRepository.save(new PlayerRun(meta));
    }
}
