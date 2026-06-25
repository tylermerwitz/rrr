package com.example.rrr.service;

import com.example.rrr.dto.Equipment;
import com.example.rrr.dto.PlayerRun;
import com.example.rrr.dto.PlayerRunEquipment;
import com.example.rrr.repository.PlayerRunEquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final PlayerRunEquipmentRepository runEquipmentRepository;

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
}