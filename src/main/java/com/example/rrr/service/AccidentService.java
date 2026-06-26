package com.example.rrr.service;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import com.example.rrr.model.AccidentReport;
import com.example.rrr.model.EventType;
import com.example.rrr.model.HumiliationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccidentService {

    /** How much a single wetting/messing adds to the diaper's saturation (see PlayerRun). */
    private static final int WETNESS_PER_ACCIDENT = 34;
    private static final int MESSINESS_PER_ACCIDENT = 50;

    private final HumiliationCalculator humiliationCalculator;

    /**
     * Fires any involuntary accident the run is now due for (a meter having bottomed out), applying
     * its humiliation/regression and resetting the meter. Returns which accidents fired so the
     * caller can narrate them.
     */
    public AccidentReport checkAccidents(PlayerMeta meta, PlayerRun run) {

        boolean wetted = false;
        boolean messed = false;

        if (run.getBladderPercent() <= 0) {
            triggerInvoluntaryWet(meta, run);
            wetted = true;
        }

        if (run.getBowelPercent() <= 0) {
            triggerInvoluntaryMess(meta, run);
            messed = true;
        }

        return new AccidentReport(wetted, messed);
    }

    private void triggerInvoluntaryWet(PlayerMeta meta, PlayerRun run) {

        meta.addRegression(12);

        int humiliation = humiliationCalculator.calculate(
                new HumiliationEvent(250, EventType.BABY_TREATMENT),
                meta,
                run
        );

        run.addHumiliation(humiliation);
        run.wetDiaper(WETNESS_PER_ACCIDENT);
        // The bladder meter is reset by the engine after the turn (see GameEngineService).
    }

    private void triggerInvoluntaryMess(PlayerMeta meta, PlayerRun run) {

        meta.addRegression(32);

        int humiliation = humiliationCalculator.calculate(
                new HumiliationEvent(600, EventType.BABY_TREATMENT),
                meta,
                run
        );

        run.addHumiliation(humiliation);
        run.messDiaper(MESSINESS_PER_ACCIDENT);
        // The bowel meter is reset by the engine after the turn (see GameEngineService).
    }
}
