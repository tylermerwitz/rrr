package com.example.rrr.service;

import com.example.rrr.dto.Equipment;
import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import com.example.rrr.dto.PlayerRunEquipment;
import com.example.rrr.dto.PlayerUnlockedEquipment;
import com.example.rrr.model.EquipmentType;
import com.example.rrr.repository.EquipmentRepository;
import com.example.rrr.repository.PlayerRunEquipmentRepository;
import com.example.rrr.repository.PlayerUnlockedEquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    /** Neutral baseline gear the player always starts a run wearing. */
    private static final String STARTER_DIAPER = "Plain Diaper";
    private static final String STARTER_OUTFIT = "Plain Outfit";

    private final PlayerRunEquipmentRepository runEquipmentRepository;
    private final PlayerUnlockedEquipmentRepository unlockedEquipmentRepository;
    private final EquipmentRepository equipmentRepository;

    public int calculateFlatDefense(PlayerRun run) {
        return runEquipmentRepository.findByRun(run)
                .stream()
                .map(PlayerRunEquipment::getEquipment)
                .mapToInt(Equipment::getFlatDefense)
                .sum();
    }

    public int calculateSpeedModifier(PlayerRun run) {
        return runEquipmentRepository.findByRun(run)
                .stream()
                .map(PlayerRunEquipment::getEquipment)
                .mapToInt(Equipment::getSpeedModifier)
                .sum();
    }

    public double calculateBladderModifier(PlayerRun run) {
        return runEquipmentRepository.findByRun(run)
                .stream()
                .map(PlayerRunEquipment::getEquipment)
                .mapToDouble(Equipment::getBladderModifier)
                .sum();
    }

    public double calculateBowelModifier(PlayerRun run) {
        return runEquipmentRepository.findByRun(run)
                .stream()
                .map(PlayerRunEquipment::getEquipment)
                .mapToDouble(Equipment::getBowelModifier)
                .sum();
    }

    public List<Equipment> getUnlockedEquipment(PlayerMeta player) {
        return unlockedEquipmentRepository.findByPlayer(player)
                .stream()
                .map(PlayerUnlockedEquipment::getEquipment)
                .toList();
    }

    public List<Equipment> getEquippedItems(PlayerRun run) {
        return runEquipmentRepository.findByRun(run)
                .stream()
                .map(PlayerRunEquipment::getEquipment)
                .toList();
    }

    /**
     * Equips an unlocked piece of equipment, swapping out anything already
     * equipped of the same EquipmentType (the run can only have one item per type).
     */
    @Transactional
    public void equip(PlayerMeta player, PlayerRun run, Long equipmentId) {

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown equipment: " + equipmentId));

        boolean unlocked = unlockedEquipmentRepository.findByPlayer(player)
                .stream()
                .anyMatch(unlock -> unlock.getEquipment().getId().equals(equipmentId));

        if (!unlocked) {
            throw new IllegalStateException("Equipment is not unlocked: " + equipment.getName());
        }

        List<PlayerRunEquipment> currentlyEquipped = runEquipmentRepository.findByRun(run);

        boolean alreadyEquipped = currentlyEquipped.stream()
                .anyMatch(equipped -> equipped.getEquipment().getId().equals(equipmentId));

        if (alreadyEquipped) {
            return;
        }

        currentlyEquipped.stream()
                .filter(equipped -> equipped.getEquipment().getType() == equipment.getType())
                .forEach(runEquipmentRepository::delete);

        runEquipmentRepository.save(new PlayerRunEquipment(run, equipment));
    }

    /**
     * Unlocks a piece of equipment for the player by name (used by event "New Item" rewards). No-op
     * if the name isn't in the catalog or the player already owns it. Returns true only if a new
     * unlock was actually granted, so the caller can announce it.
     */
    @Transactional
    public boolean grantItemByName(PlayerMeta player, String name) {
        Optional<Equipment> match = equipmentRepository.findByName(name);
        if (match.isEmpty()) {
            return false;
        }
        Equipment equipment = match.get();

        boolean owned = unlockedEquipmentRepository.findByPlayer(player)
                .stream()
                .anyMatch(unlock -> unlock.getEquipment().getId().equals(equipment.getId()));
        if (owned) {
            return false;
        }

        unlockedEquipmentRepository.save(new PlayerUnlockedEquipment(player, equipment));
        return true;
    }

    @Transactional
    public void unequip(PlayerRun run, Long equipmentId) {
        runEquipmentRepository.findByRun(run)
                .stream()
                .filter(equipped -> equipped.getEquipment().getId().equals(equipmentId))
                .forEach(runEquipmentRepository::delete);
    }

    /**
     * Unlocks the neutral starter gear (creating the catalog entries on first use)
     * and equips it on the given run. Called whenever a new run begins so the player
     * always enters the realm wearing a diaper and an outfit.
     */
    @Transactional
    public void grantStarterLoadout(PlayerMeta player, PlayerRun run) {

        Equipment diaper = getOrCreateStarter(STARTER_DIAPER, EquipmentType.DIAPER);
        Equipment outfit = getOrCreateStarter(STARTER_OUTFIT, EquipmentType.OUTFIT);

        unlockIfMissing(player, diaper);
        unlockIfMissing(player, outfit);

        equipIfMissing(run, diaper);
        equipIfMissing(run, outfit);
    }

    private Equipment getOrCreateStarter(String name, EquipmentType type) {
        return equipmentRepository.findByName(name)
                .orElseGet(() -> equipmentRepository.save(new Equipment(
                        name,
                        type,
                        0,      // flatDefense — neutral
                        0,      // speedModifier — neutral
                        0.0,    // bladderModifier — neutral
                        0.0,    // bowelModifier — neutral
                        0,      // unlockCost — free
                        0,      // requiredRegressionTier
                        0,      // requiredArousalTier
                        false   // not legendary
                )));
    }

    private void unlockIfMissing(PlayerMeta player, Equipment equipment) {
        boolean owned = unlockedEquipmentRepository.findByPlayer(player)
                .stream()
                .anyMatch(unlock -> unlock.getEquipment().getId().equals(equipment.getId()));
        if (!owned) {
            unlockedEquipmentRepository.save(new PlayerUnlockedEquipment(player, equipment));
        }
    }

    private void equipIfMissing(PlayerRun run, Equipment equipment) {
        boolean equipped = runEquipmentRepository.findByRun(run)
                .stream()
                .anyMatch(item -> item.getEquipment().getId().equals(equipment.getId()));
        if (!equipped) {
            runEquipmentRepository.save(new PlayerRunEquipment(run, equipment));
        }
    }
}