package com.example.rrr.service;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import com.example.rrr.model.AccidentReport;
import com.example.rrr.model.ActionType;
import com.example.rrr.model.EventOutcome;
import com.example.rrr.model.GameEvent;
import com.example.rrr.model.TurnResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameEngineService {

    private final EventResolverService resolver;
    private final TurnProcessor turnProcessor;

    /**
     * Canned lines for a passive desperation-accident that fires on top of the chosen action. The
     * choice's own outcome is authored per-event, but losing control from sheer desperation isn't,
     * so we append one of these to the resolution prose rather than changing the diaper silently.
     */
    private static final String PASSIVE_WET_LINE =
            " And then the pressure you've been fighting all this time finally wins — warmth floods "
            + "your padding before you can clench against it, the last of your control simply gone.";
    private static final String PASSIVE_MESS_LINE =
            " Worse still, your body betrays you the rest of the way; your stomach cramps and lets go, "
            + "filling your diaper in a hot, helpless rush you can do nothing to stop.";

    @Transactional
    public TurnResult processAction(
            GameEvent event,
            ActionType actionType,
            PlayerMeta meta,
            PlayerRun run
    ) {

        // The chosen action resolves first, against its authored branch.
        EventOutcome outcome = resolver.resolve(event, actionType, meta, run);

        // Then passive mechanics (drain) may trigger an involuntary accident on top of it.
        AccidentReport passive = turnProcessor.processTurn(meta, run);

        if (passive.any()) {
            StringBuilder narration = new StringBuilder(
                    outcome.getNarration() == null ? "" : outcome.getNarration());
            if (passive.wetted()) {
                narration.append(PASSIVE_WET_LINE);
            }
            if (passive.messed()) {
                narration.append(PASSIVE_MESS_LINE);
            }
            outcome.setNarration(narration.toString());
        }

        // Fold the chosen branch's accident together with any passive one, and reset the affected
        // meter(s) here — last, after the drain — so an accident always leaves a full meter behind
        // (a fresh diaper means fresh control). The outcome carries both flags so the client can say
        // exactly what happened: wet, messed, or both.
        boolean wetted = outcome.isWetted() || passive.wetted();
        boolean messed = outcome.isMessed() || passive.messed();
        if (wetted) {
            run.resetBladder();
        }
        if (messed) {
            run.resetBowel();
        }
        outcome.setWetted(wetted);
        outcome.setMessed(messed);

        // A diaper change is the very last thing to happen this turn, so it wins over both the chosen
        // branch's accident AND any passive one: the player ends in a fresh diaper (0/0), no matter
        // what soiled it earlier in the turn. The wetted/messed flags stay as-is so the resolution can
        // still narrate what happened before the change ("you wet yourself... then got changed").
        if (outcome.isDiaperChanged()) {
            run.changeDiaper();
        }

        return new TurnResult(run, meta, outcome);
    }
}
