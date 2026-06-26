package com.example.rrr.service;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import com.example.rrr.model.AccidentReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TurnProcessor {

    private final AccidentService accidentService;

    /**
     * Applies this turn's passive mechanics to the run (bladder/bowel drain, accident checks),
     * mutating it in place, and returns which involuntary accidents fired so they can be narrated.
     * Death is NOT handled here: a turn can push humiliation past the breaking point, but the
     * respawn is deferred to the orchestration layer so the breaking can be narrated against the
     * dying run before it's reset (see {@link DeathService#isFatal}).
     */
    public AccidentReport processTurn(PlayerMeta meta, PlayerRun run) {

        drainBladder(run);
        drainBowel(run);

        return accidentService.checkAccidents(meta, run);
    }

    private void drainBladder(PlayerRun run) {

        double drain = 1.0 + run.nextDouble(); // 1.0 to 2.0
        run.setBladderPercent(
                Math.max(0, run.getBladderPercent() - drain)
        );
    }

    private void drainBowel(PlayerRun run) {

        double bladderDrain = 1.0 + run.nextDouble();
        double bowelDrain = bladderDrain / 4.0;

        run.setBowelPercent(
                Math.max(0, run.getBowelPercent() - bowelDrain)
        );
    }
}
