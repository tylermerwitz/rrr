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
    private final EquipmentService equipmentService;

    /**
     * Whether this run has crossed the humiliation threshold that breaks the player. Pure check —
     * it does NOT mutate the run, so the caller can narrate the breaking before committing to the
     * respawn (which wipes the dying run's floor transcript).
     */
    public boolean isFatal(PlayerRun run) {
        return run.getHumiliation() >= 2000;
    }

    /**
     * Applies the death penalty, resets the dying run, and returns a brand-new hub run to take its
     * place. Call this only AFTER any death narration has been generated — {@code resetForRespawn}
     * clears the floor transcript and state the breaking is narrated from.
     */
    public PlayerRun respawn(PlayerMeta meta, PlayerRun run) {

        meta.incrementDeaths();

        meta.addRegression(25);
        meta.addArousal(25);

        meta.reduceBladderControl(5);
        meta.reduceBowelControl(5);

        run.resetForRespawn();

        PlayerRun fresh = runRepository.save(new PlayerRun(meta));
        equipmentService.grantStarterLoadout(meta, fresh);
        return fresh;
    }
}
