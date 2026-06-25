package com.example.rrr.service;

import com.example.rrr.model.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HumiliationModifierService {

    private final TierService tierService;

    public double regressionMultiplier(int regressionPoints, EventType type) {

        int tier = tierService.getRegressionTier(regressionPoints);

        if (type != EventType.BABY_TREATMENT) {
            return adultExpectationModifierIfTier5(tier, type);
        }

        return switch (tier) {
            case 2 -> 0.75;
            case 3 -> 0.50;
            case 4 -> 0.25;
            case 5 -> 0.10;
            default -> 1.0;
        };
    }

    public double arousalMultiplier(int arousalPoints, EventType type) {

        int tier = tierService.getArousalTier(arousalPoints);

        if (type != EventType.KINKY_EVENT) {
            return vanillaModifierIfTier5(tier, type);
        }

        return switch (tier) {
            case 2 -> 0.75;
            case 3 -> 0.50;
            case 4 -> 0.25;
            case 5 -> 0.10;
            default -> 1.0;
        };
    }

    private double adultExpectationModifierIfTier5(int tier, EventType type) {
        if (tier == 5 && type == EventType.ADULT_EXPECTATION) {
            return 1.5;
        }
        return 1.0;
    }

    private double vanillaModifierIfTier5(int tier, EventType type) {
        if (tier == 5 && type == EventType.ADULT_EXPECTATION) {
            return 1.5;
        }
        return 1.0;
    }
}
