package com.example.rrr.controller;

import com.example.rrr.dto.*;
import com.example.rrr.model.*;
import com.example.rrr.repository.PlayerMetaRepository;
import com.example.rrr.repository.PlayerRunRepository;
import com.example.rrr.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final PlayerMetaRepository playerRepository;
    private final PlayerRunRepository runRepository;

    private final GameEngineService engineService;
    private final FloorGeneratorService floorGenerator;
    private final FloorDefinitionService definitionService;
    private final EventDefinitionService eventService;
    private final NarrationMapper narrationMapper;
    private final LlmNarrationService llmNarrationService;
    private final LlmClient llmClient;

    /* =========================
       START RUN
       ========================= */

    @PostMapping("/start")
    @Transactional
    public GameStateResponse startRun(@RequestBody StartGameRequest request) {

        PlayerMeta player = playerRepository.findById(request.playerId())
                .orElseGet(() -> playerRepository.save(
                        new PlayerMeta("Player_" + request.playerId())
                ));

        PlayerRun run = new PlayerRun(player);

        runRepository.save(run);

        FloorDefinition def =
                definitionService.getDefinition(1);

        FloorInstance floor =
                floorGenerator.generateFloor(def, run.getWorldSeed());

        return new GameStateResponse(
                player,
                run,
                floor.getRooms()
        );
    }

    /* =========================
       GET CURRENT STATE
       ========================= */

    @GetMapping("/state/{playerId}")
    public GameStateResponse getState(@PathVariable Long playerId) {

        PlayerMeta player = playerRepository.findById(playerId)
                .orElseThrow();

        PlayerRun run = runRepository
                .findByPlayerAndActiveTrue(player)
                .orElseThrow();

        FloorDefinition def =
                definitionService.getDefinition(run.getCurrentFloor());

        FloorInstance floor =
                floorGenerator.generateFloor(def, run.getWorldSeed());

        return new GameStateResponse(
                player,
                run,
                floor.getRooms()
        );
    }

    /* =========================
       MOVE ROOMS
       ========================= */

    @PostMapping("/move")
    @Transactional
    public GameStateResponse move(
            @RequestBody MoveRequest request
    ) {

        PlayerMeta player = playerRepository
                .findById(request.playerId())
                .orElseThrow();

        PlayerRun run = runRepository
                .findByPlayerAndActiveTrue(player)
                .orElseThrow();

        // In full version:
        // Validate roomId is connected
        // Update run current room state

        return getState(player.getId());
    }

    @PostMapping("/action")
    @Transactional
    public Map<String, Object> processAction(
            @RequestBody ActionRequest request
    ) {

        PlayerMeta player = playerRepository
                .findById(request.playerId())
                .orElseThrow();

        PlayerRun run = runRepository
                .findByPlayerAndActiveTrue(player)
                .orElseThrow();

        GameEvent event = eventService.getEventInstance(
                request.eventId(),
                run.getCurrentFloor()
        );

        TurnResult result =
                engineService.processAction(event, player, run);

        NarrationRequest narrationRequest =
                narrationMapper.build(result);

        NarrationResponse narration =
                llmNarrationService.generate(narrationRequest);

        return Map.of(
                "gameState", result,
                "narration", narration
        );
    }

    @PostMapping("/test-llm")
    public String testLlm() {
        return llmClient.complete("""
        Return this JSON:
        {"narration":"hello","choices":[
          {"id":"A","text":"test","riskLevel":"SAFE"},
          {"id":"B","text":"test2","riskLevel":"MODERATE"},
          {"id":"C","text":"test3","riskLevel":"RISKY"}
        ]}
    """);
    }
}
