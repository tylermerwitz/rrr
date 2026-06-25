package com.example.rrr.service;

import org.springframework.stereotype.Service;

@Service
public class TierService {

    public int getRegressionTier(int regressionPoints) {
        if (regressionPoints >= 4000) return 5;
        if (regressionPoints >= 2000) return 4;
        if (regressionPoints >= 1000) return 3;
        if (regressionPoints >= 400) return 2;
        return 1;
    }

    public int getArousalTier(int arousalPoints) {
        if (arousalPoints >= 4000) return 5;
        if (arousalPoints >= 2000) return 4;
        if (arousalPoints >= 1000) return 3;
        if (arousalPoints >= 400) return 2;
        return 1;
    }
}