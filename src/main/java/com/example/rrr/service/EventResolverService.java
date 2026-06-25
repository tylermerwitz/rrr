package com.example.rrr.service;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import com.example.rrr.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class EventResolverService {

    private final HumiliationCalculator humiliationCalculator;
    private final AccidentService accidentService;
    private final DeathService deathService;

    public EventOutcome resolve(
            GameEvent event,
            PlayerMeta meta,
            PlayerRun run
    ) {

        boolean success = run.getRandom().nextDouble() <= event.getSuccessChance();

        int humiliation = 0;
        int regression = 0;
        int arousal = 0;
        boolean accident = false;

        if (!success) {

            humiliation = humiliationCalculator.calculate(
                    new HumiliationEvent(
                            event.getBaseHumiliation(),
                            mapCategory(event.getCategory())
                    ),
                    meta,
                    run
            );

            run.addHumiliation(humiliation);
        }

        regression = event.getRegressionGain();
        arousal = event.getArousalGain();

        meta.addRegression(regression);
        meta.addArousal(arousal);

        if (event.isCanCauseAccident() && run.getRandom().nextDouble() < 0.25) {
            accidentService.checkAccidents(meta, run);
            accident = true;
        }

        deathService.checkDeath(meta, run);

        return new EventOutcome(
                success,
                humiliation,
                regression,
                arousal,
                accident,
                run.isBroken()
        );
    }

    private EventType mapCategory(EventCategory category) {
        return switch (category) {
            case BABY_TREATMENT -> EventType.BABY_TREATMENT;
            case KINKY_EVENT -> EventType.KINKY_EVENT;
            default -> EventType.NEUTRAL;
        };
    }
}
