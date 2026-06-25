package com.example.rrr.model;

import com.example.rrr.dto.Equipment;

import java.util.List;

public class EquipmentCalculator {

    public int totalFlatDefense(List<Equipment> equipment) {
        return equipment.stream()
                .mapToInt(Equipment::getFlatDefense)
                .sum();
    }
}
