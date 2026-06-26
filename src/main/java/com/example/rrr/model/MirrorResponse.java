package com.example.rrr.model;

import com.example.rrr.dto.Equipment;
import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MirrorResponse {

    private PlayerMeta meta;
    private PlayerRun run;
    private List<Equipment> equippedItems;
    private int totalFlatDefense;
    private int totalSpeedModifier;
    private double totalBladderModifier;
    private double totalBowelModifier;
}
