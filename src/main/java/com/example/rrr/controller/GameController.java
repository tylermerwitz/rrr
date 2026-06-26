package com.example.rrr.controller;

import com.example.rrr.dto.*;
import com.example.rrr.model.*;
import com.example.rrr.repository.PlayerMetaRepository;
import com.example.rrr.repository.PlayerRunRepository;
import com.example.rrr.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final PlayerMetaRepository playerRepository;
    private final PlayerRunRepository runRepository;

    private final GameEngineService engineService;
    private final DeathService deathService;
    private final EventDefinitionService eventService;
    private final EquipmentService equipmentService;
    private final ShopService shopService;
    private final ObjectMapper objectMapper;

    /**
     * The breaking paragraph appended to the fatal action's resolution prose on the death screen.
     * Authored, not generated — the per-event failure narration describes the blow; this describes
     * the collapse it finally triggers.
     */
    private static final String DEATH_LINE =
            " And that is the blow that finally breaks you. Whatever was still holding — the last "
            + "stubborn scrap of the person who walked in here — lets go all at once, and you come "
            + "completely undone, body and will surrendering together as everything goes soft and "
            + "dark and far away. When you surface, you are back in the Hub, smaller than before.";

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
                .orElseGet(() -> {
                    PlayerRun fresh = runRepository.save(new PlayerRun(player));
                    equipmentService.grantStarterLoadout(player, fresh);
                    return fresh;
                });

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

        // On a plain reload we don't spend an LLM call — we re-serve the last narration.
        return floorPayload(player, run, null, readLastNarration(run));
    }

    /**
     * The shape the floor screen consumes: current run + player for the topbar, the optional
     * outcome of the action just taken, and the narrator's current narration + choices.
     */
    private Map<String, Object> floorPayload(
            PlayerMeta player,
            PlayerRun run,
            EventOutcome outcome,
            NarrationResponse narration
    ) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("meta", player);
        payload.put("run", run);
        payload.put("outcome", outcome);     // null on the opening beat / a plain reload
        payload.put("narration", narration); // null only if the player has died back to the hub
        payload.put("phase", phaseFor(run, narration));
        return payload;
    }

    /**
     * Tells the client what kind of screen this payload is, so it knows what control to show:
     * <ul>
     *   <li>{@code RESOLUTION} — narration with no choices; show a Continue button (-> /continue).</li>
     *   <li>{@code EVENT} — narration with choices; show the choice buttons (-> /action).</li>
     * </ul>
     * The DEATH beat is tagged {@code DEAD} explicitly by {@code /action} (it overrides this), so the
     * hub-location {@code ENDED} branch here is just a defensive fallback.
     */
    private String phaseFor(PlayerRun run, NarrationResponse narration) {
        if (run.getLocation() == GameLocation.HUB) {
            return "ENDED";
        }
        boolean hasChoices = narration != null
                && narration.getChoices() != null
                && !narration.getChoices().isEmpty();
        return hasChoices ? "EVENT" : "RESOLUTION";
    }

    private NarrationResponse readLastNarration(PlayerRun run) {
        String json = run.getLastNarration();
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, NarrationResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    /* =========================
       HUB OPTIONS
       ========================= */

    @PostMapping("/hub/enter")
    @Transactional
    public Map<String, Object> enterRegressionRealm(@RequestBody StartGameRequest request) {

        PlayerMeta player = playerRepository.findById(request.playerId())
                .orElseThrow();

        PlayerRun run = runRepository.findByPlayerAndActiveTrue(player)
                .orElseThrow();

        if (run.getLocation() != GameLocation.HUB) {
            throw new IllegalStateException("Player is not in the hub");
        }

        run.enterFloor();

        // Open the floor's encounter: pick the first event and present its authored scene + choices.
        GameEvent firstEvent = eventService.getRandomEventForFloor(player, run);
        NarrationResponse narration = eventBeat(firstEvent);
        persistLastNarration(run, narration);

        return floorPayload(player, run, null, narration);
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
       PLAY A TURN
       ========================= */

    @PostMapping("/action")
    @Transactional
    public Map<String, Object> processAction(@RequestBody ActionRequest request) {

        PlayerMeta player = playerRepository
                .findById(request.playerId())
                .orElseThrow();

        PlayerRun run = runRepository
                .findByPlayerAndActiveTrue(player)
                .orElseThrow();

        GameEvent event = eventService.getEventInstance(request.eventId());

        TurnResult result =
                engineService.processAction(event, request.actionType(), player, run);

        PlayerRun activeRun = result.getRunState();
        PlayerMeta activeMeta = result.getMetaState();
        EventOutcome outcome = result.getOutcome();

        // If this turn pushed humiliation past the breaking point, the resolution becomes a DEATH
        // beat: the authored failure prose plus the canned breaking paragraph. We build it against
        // the dying run and only THEN respawn (which resets the run and drops a fresh one in the
        // hub). The payload is tagged phase=DEAD so the client shows the death banner.
        if (deathService.isFatal(activeRun)) {
            String deathText = appendLine(outcome.getNarration(), DEATH_LINE);
            NarrationResponse death = new NarrationResponse(deathText, List.of());

            PlayerRun fresh = deathService.respawn(activeMeta, activeRun);

            Map<String, Object> payload = floorPayload(activeMeta, fresh, outcome, death);
            payload.put("phase", "DEAD");
            return payload;
        }

        // Otherwise show how THIS action resolved — its own beat, the authored branch prose with no
        // choices. The player reads it, then hits Continue (/continue) to advance to the next event.
        NarrationResponse resolution = new NarrationResponse(outcome.getNarration(), List.of());
        persistLastNarration(activeRun, resolution);

        return floorPayload(activeMeta, activeRun, outcome, resolution);
    }

    /**
     * Advance from a resolution screen to the next encounter. Called when the player hits Continue
     * after reading how their last action played out: picks the next event and narrates its scene.
     */
    @PostMapping("/continue")
    @Transactional
    public Map<String, Object> continueEncounter(@RequestBody ContinueRequest request) {

        PlayerMeta player = playerRepository
                .findById(request.playerId())
                .orElseThrow();

        PlayerRun run = runRepository
                .findByPlayerAndActiveTrue(player)
                .orElseThrow();

        if (run.getLocation() != GameLocation.FLOOR) {
            throw new IllegalStateException("No active floor encounter to continue");
        }

        GameEvent nextEvent = eventService.getRandomEventForFloor(player, run);
        NarrationResponse narration = eventBeat(nextEvent);
        persistLastNarration(run, narration);

        return floorPayload(player, run, null, narration);
    }

    /* =========================
       AUTHORED NARRATION HELPERS
       ========================= */

    /**
     * Builds the EVENT beat for an authored event: its scene as the narration, and its choices as
     * submittable options (each bound to its risk/action and the event id).
     */
    private NarrationResponse eventBeat(GameEvent event) {
        List<ChoiceOption> choices = new ArrayList<>();
        char id = 'A';
        for (EventChoice choice : event.getChoices()) {
            ChoiceOption option = new ChoiceOption(
                    String.valueOf(id++), choice.getLabel(), choice.getRisk().name());
            option.setActionType(choice.getRisk());
            option.setEventId(event.getId());
            if (choice.getCategory() != null) {
                option.setCategory(choice.getCategory().name());
            }
            choices.add(option);
        }
        return new NarrationResponse(event.getScene(), choices);
    }

    /** Persists the current screen so a plain state reload re-serves it without re-rolling. */
    private void persistLastNarration(PlayerRun run, NarrationResponse narration) {
        try {
            run.setLastNarration(objectMapper.writeValueAsString(narration));
        } catch (Exception e) {
            // Non-fatal: a reload simply won't be able to re-serve this exact screen.
        }
    }

    private String appendLine(String base, String line) {
        return (base == null ? "" : base) + line;
    }
}
