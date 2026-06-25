package com.example.rrr.service;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import com.example.rrr.model.EquipmentCalculator;
import com.example.rrr.model.HumiliationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HumiliationCalculator {

    private final HumiliationModifierService modifierService;

    @Autowired
    private EquipmentService equipmentService;

    public int calculate(
            HumiliationEvent event,
            PlayerMeta meta,
            PlayerRun run
    )
    {

        double value = event.baseValue();

        double regressionMulti = modifierService
                .regressionMultiplier(meta.getRegressionPoints(), event.type());

        double arousalMulti = modifierService
                .arousalMultiplier(meta.getArousalPoints(), event.type());

        value = value * regressionMulti * arousalMulti;

        int flatDefense = equipmentService.calculateFlatDefense(run);

        value -= flatDefense;

        return Math.max(0, (int) Math.round(value));
    }
}