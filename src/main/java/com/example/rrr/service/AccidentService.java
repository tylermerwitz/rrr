package com.example.rrr.service;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import com.example.rrr.model.EventType;
import com.example.rrr.model.HumiliationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccidentService {

    private final HumiliationCalculator humiliationCalculator;

    public void checkAccidents(PlayerMeta meta, PlayerRun run) {

        if (run.getBladderPercent() <= 0) {
            triggerInvoluntaryWet(meta, run);
        }

        if (run.getBowelPercent() <= 0) {
            triggerInvoluntaryMess(meta, run);
        }
    }

    private void triggerInvoluntaryWet(PlayerMeta meta, PlayerRun run) {

        meta.addRegression(12);

        int humiliation = humiliationCalculator.calculate(
                new HumiliationEvent(250, EventType.BABY_TREATMENT),
                meta,
                run
        );

        run.addHumiliation(humiliation);
        run.resetBladder();
    }

    private void triggerInvoluntaryMess(PlayerMeta meta, PlayerRun run) {

        meta.addRegression(32);

        int humiliation = humiliationCalculator.calculate(
                new HumiliationEvent(600, EventType.BABY_TREATMENT),
                meta,
                run
        );

        run.addHumiliation(humiliation);
        run.resetBowel();
    }
}
