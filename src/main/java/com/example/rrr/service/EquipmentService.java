package com.example.rrr.service;

import com.example.rrr.dto.Equipment;
import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import com.example.rrr.dto.PlayerRunEquipment;
import com.example.rrr.dto.PlayerUnlockedEquipment;
import com.example.rrr.repository.EquipmentRepository;
import com.example.rrr.repository.PlayerRunEquipmentRepository;
import com.example.rrr.repository.PlayerUnlockedEquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

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

    @Transactional
    public void unequip(PlayerRun run, Long equipmentId) {
        runEquipmentRepository.findByRun(run)
                .stream()
                .filter(equipped -> equipped.getEquipment().getId().equals(equipmentId))
                .forEach(runEquipmentRepository::delete);
    }
}