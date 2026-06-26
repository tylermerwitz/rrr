package com.example.rrr.service;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import com.example.rrr.model.ActionType;
import com.example.rrr.model.EventChoice;
import com.example.rrr.model.EventConsequence;
import com.example.rrr.model.EventOutcome;
import com.example.rrr.model.GameEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Resolves a chosen action against an authored event: rolls the choice's success chance, selects
 * the matching success/failure branch, and applies that branch's exact authored consequences. No
 * model, no computed humiliation — every number and every line of prose comes from {@code events_floor_1.json}.
 */
@Service
@RequiredArgsConstructor
public class EventResolverService {

    /** Saturation a single forced wetting/messing adds to the diaper (mirrors AccidentService). */
    private static final int WETNESS_PER_ACCIDENT = 34;
    private static final int MESSINESS_PER_ACCIDENT = 50;

    private final EquipmentService equipmentService;

    public EventOutcome resolve(
            GameEvent event,
            ActionType actionType,
            PlayerMeta meta,
            PlayerRun run
    ) {
        EventChoice choice = selectChoice(event, actionType);

        boolean success = run.nextDouble() <= clamp(choice.getSuccessChance());
        EventConsequence branch = success ? choice.getSuccess() : choice.getFailure();

        run.addHumiliation(branch.getHumiliation());
        meta.addRegression(branch.getRegression());
        meta.addArousal(branch.getArousal());
        if (branch.getCoins() != 0) {
            meta.addCoins(branch.getCoins());
        }

        // Deepen the diaper's saturation now, but leave the bladder/bowel meters to the engine: it
        // resets them AFTER this turn's passive drain so an accident always ends at a full meter.
        if (branch.isWet()) {
            run.wetDiaper(WETNESS_PER_ACCIDENT);
        }
        if (branch.isMess()) {
            run.messDiaper(MESSINESS_PER_ACCIDENT);
        }

        // Erode control without forcing an accident outright (may tip into one downstream).
        if (branch.getBladderDrain() > 0) {
            run.drainBladder(branch.getBladderDrain());
        }
        if (branch.getBowelDrain() > 0) {
            run.drainBowel(branch.getBowelDrain());
        }

        // Restore control (the inverse of a drain), clamped at the player's max by the run.
        if (branch.getBladderFill() > 0) {
            run.fillBladder(branch.getBladderFill());
        }
        if (branch.getBowelFill() > 0) {
            run.fillBowel(branch.getBowelFill());
        }

        // Rare loot: unlock the named item for the player. Only report it if a new unlock happened.
        String itemReward = null;
        if (branch.getItemReward() != null && !branch.getItemReward().isBlank()) {
            if (equipmentService.grantItemByName(meta, branch.getItemReward())) {
                itemReward = branch.getItemReward();
            }
        }

        // A diaper change is carried on the outcome but applied by the engine LAST (after any passive
        // accident this turn) so the diaper is guaranteed to end clean — see GameEngineService.
        return new EventOutcome(
                success,
                branch.getHumiliation(),
                branch.getRegression(),
                branch.getArousal(),
                branch.isWet(),
                branch.isMess(),
                run.isBroken(),
                branch.getNarration(),
                itemReward,
                branch.isChangeDiaper()
        );
    }

    /** The authored choice for the player's risk approach, defaulting to MODERATE if unmatched. */
    private EventChoice selectChoice(GameEvent event, ActionType actionType) {
        ActionType action = (actionType == ActionType.SAFE || actionType == ActionType.RISKY)
                ? actionType
                : ActionType.MODERATE;

        EventChoice choice = event.choiceFor(action);
        if (choice == null) {
            choice = event.choiceFor(ActionType.MODERATE);
        }
        if (choice == null) {
            throw new IllegalStateException("Event " + event.getId() + " has no playable choices");
        }
        return choice;
    }

    private double clamp(double chance) {
        return Math.max(0.0, Math.min(1.0, chance));
    }
}
