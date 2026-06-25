package com.example.rrr.service;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class TurnProcessor {

    private final AccidentService accidentService;
    private final DeathService deathService;

    public void processTurn(PlayerMeta meta, PlayerRun run) {

        drainBladder(run);
        drainBowel(run);

        accidentService.checkAccidents(meta, run);

        deathService.checkDeath(meta, run);
    }

    private void drainBladder(PlayerRun run) {

        double drain = 1.0 + run.getRandom().nextDouble(); // 1.0 to 2.0
        run.setBladderPercent(
                Math.max(0, run.getBladderPercent() - drain)
        );
    }

    private void drainBowel(PlayerRun run) {

        double bladderDrain = 1.0 + run.getRandom().nextDouble();
        double bowelDrain = bladderDrain / 4.0;

        run.setBowelPercent(
                Math.max(0, run.getBowelPercent() - bowelDrain)
        );
    }
}
