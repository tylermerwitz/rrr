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
    private final EquipmentService equipmentService;
    private final ShopService shopService;

    /* =========================
       START RUN
       ========================= */

    @PostMapping("/start")
    @Transactional
    public Object startRun(@RequestBody StartGameRequest request) {

        PlayerMeta player = playerRepository.findById(request.playerId())
                .orElseGet(() -> playerRepository.save(
                        new PlayerMeta("Player_" + request.playerId())
                ));

        PlayerRun run = runRepository.findByPlayerAndActiveTrue(player)
                .orElseGet(() -> runRepository.save(new PlayerRun(player)));

        return buildState(player, run);
    }

    /* =========================
       GET CURRENT STATE
       ========================= */

    @GetMapping("/state/{playerId}")
    public Object getState(@PathVariable Long playerId) {

        PlayerMeta player = playerRepository.findById(playerId)
                .orElseThrow();

        PlayerRun run = runRepository
                .findByPlayerAndActiveTrue(player)
                .orElseThrow();

        return buildState(player, run);
    }

    private Object buildState(PlayerMeta player, PlayerRun run) {

        if (run.getLocation() == GameLocation.HUB) {
            return new HubStateResponse(player, run);
        }

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
       HUB OPTIONS
       ========================= */

    @PostMapping("/hub/enter")
    @Transactional
    public GameStateResponse enterRegressionRealm(@RequestBody StartGameRequest request) {

        PlayerMeta player = playerRepository.findById(request.playerId())
                .orElseThrow();

        PlayerRun run = runRepository.findByPlayerAndActiveTrue(player)
                .orElseThrow();

        if (run.getLocation() != GameLocation.HUB) {
            throw new IllegalStateException("Player is not in the hub");
        }

        run.enterFloor();

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

    @GetMapping("/hub/mirror/{playerId}")
    public MirrorResponse lookInMirror(@PathVariable Long playerId) {

        PlayerMeta player = playerRepository.findById(playerId)
                .orElseThrow();

        PlayerRun run = runRepository.findByPlayerAndActiveTrue(player)
                .orElseThrow();

        return new MirrorResponse(
                player,
                run,
                equipmentService.getEquippedItems(run),
                equipmentService.calculateFlatDefense(run),
                equipmentService.calculateSpeedModifier(run),
                equipmentService.calculateBladderModifier(run),
                equipmentService.calculateBowelModifier(run)
        );
    }

    @GetMapping("/hub/equipment/{playerId}")
    public EquipmentLoadoutResponse getEquipmentLoadout(@PathVariable Long playerId) {

        PlayerMeta player = playerRepository.findById(playerId)
                .orElseThrow();

        PlayerRun run = runRepository.findByPlayerAndActiveTrue(player)
                .orElseThrow();

        return new EquipmentLoadoutResponse(
                equipmentService.getUnlockedEquipment(player),
                equipmentService.getEquippedItems(run)
        );
    }

    @PostMapping("/hub/equipment/equip")
    @Transactional
    public EquipmentLoadoutResponse equip(@RequestBody EquipRequest request) {

        PlayerMeta player = playerRepository.findById(request.playerId())
                .orElseThrow();

        PlayerRun run = runRepository.findByPlayerAndActiveTrue(player)
                .orElseThrow();

        equipmentService.equip(player, run, request.equipmentId());

        return new EquipmentLoadoutResponse(
                equipmentService.getUnlockedEquipment(player),
                equipmentService.getEquippedItems(run)
        );
    }

    @PostMapping("/hub/equipment/unequip")
    @Transactional
    public EquipmentLoadoutResponse unequip(@RequestBody EquipRequest request) {

        PlayerMeta player = playerRepository.findById(request.playerId())
                .orElseThrow();

        PlayerRun run = runRepository.findByPlayerAndActiveTrue(player)
                .orElseThrow();

        equipmentService.unequip(run, request.equipmentId());

        return new EquipmentLoadoutResponse(
                equipmentService.getUnlockedEquipment(player),
                equipmentService.getEquippedItems(run)
        );
    }

    @GetMapping("/hub/shop/{playerId}")
    public ShopCatalogResponse getShopCatalog(@PathVariable Long playerId) {

        PlayerMeta player = playerRepository.findById(playerId)
                .orElseThrow();

        return new ShopCatalogResponse(
                player.getCoins(),
                shopService.getCatalog(player)
        );
    }

    @PostMapping("/hub/shop/purchase")
    @Transactional
    public ShopCatalogResponse purchase(@RequestBody PurchaseRequest request) {

        PlayerMeta player = playerRepository.findById(request.playerId())
                .orElseThrow();

        shopService.purchase(player, request.equipmentId());

        return new ShopCatalogResponse(
                player.getCoins(),
                shopService.getCatalog(player)
        );
    }

    /* =========================
       MOVE ROOMS
       ========================= */

    @PostMapping("/move")
    @Transactional
    public Object move(
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

        return buildState(player, run);
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
