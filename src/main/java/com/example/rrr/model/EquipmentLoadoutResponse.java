package com.example.rrr.model;

import com.example.rrr.dto.Equipment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EquipmentLoadoutResponse {

    private List<Equipment> unlockedEquipment;
    private List<Equipment> equippedEquipment;
}
