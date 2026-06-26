package com.example.rrr;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import com.example.rrr.model.ActionType;
import com.example.rrr.model.EventOutcome;
import com.example.rrr.model.GameEvent;
import com.example.rrr.model.TurnResult;
import com.example.rrr.repository.PlayerMetaRepository;
import com.example.rrr.repository.PlayerRunRepository;
import com.example.rrr.service.EventDefinitionService;
import com.example.rrr.service.GameEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameSimulationRunner {

    private final EventDefinitionService eventService;
    private final GameEngineService engineService;
    private final PlayerMetaRepository playerRepository;
    private final PlayerRunRepository runRepository;

    public void runSimulation() {

        PlayerMeta player = new PlayerMeta("TestPlayer");
        playerRepository.save(player);

        PlayerRun run = new PlayerRun(player);
        runRepository.save(run);

        for (int i = 1; i <= 10; i++) {

            System.out.println("---- TURN " + i + " ----");

            // Draw a real event from the current floor pool (the authored ids change as content is
            // rewritten, so the sim picks whatever is eligible rather than a hard-coded id) and take
            // one of its actual choices.
            GameEvent event = eventService.getRandomEventForFloor(player, run);
            ActionType action = event.getChoices().get(0).getRisk();

            TurnResult result =
                    engineService.processAction(event, action, player, run);

            printState(result);

            if (run.isBroken()) {
                System.out.println("Player broke on turn " + i);
                break;
            }
        }
    }

    private void printState(TurnResult result) {

        PlayerRun run = result.getRunState();
        PlayerMeta meta = result.getMetaState();
        EventOutcome outcome = result.getOutcome();

        System.out.println("Humiliation: " + run.getHumiliation());
        System.out.println("Regression: " + meta.getRegressionPoints());
        System.out.println("Arousal: " + meta.getArousalPoints());
        System.out.println("Bladder: " + run.getBladderPercent());
        System.out.println("Bowel: " + run.getBowelPercent());
        System.out.println("Success: " + outcome.isSuccess());
        System.out.println("Humiliation Added: " + outcome.getHumiliationAdded());
        System.out.println();
    }
}
