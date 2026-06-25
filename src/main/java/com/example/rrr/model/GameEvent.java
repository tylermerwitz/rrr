package com.example.rrr.model;

public class GameEvent {

    private final String id;
    private final EventCategory category;

    private final int baseHumiliation;
    private final int regressionGain;
    private final int arousalGain;

    private final double successChance; // 0.0 - 1.0
    private final boolean canCauseAccident;

    public GameEvent(
            String id,
            EventCategory category,
            int baseHumiliation,
            int regressionGain,
            int arousalGain,
            double successChance,
            boolean canCauseAccident
    ) {
        this.id = id;
        this.category = category;
        this.baseHumiliation = baseHumiliation;
        this.regressionGain = regressionGain;
        this.arousalGain = arousalGain;
        this.successChance = successChance;
        this.canCauseAccident = canCauseAccident;
    }

    public String getId()
    {
        return id;
    }

    public EventCategory getCategory()
    {
        return category;
    }

    public int getBaseHumiliation()
    {
        return baseHumiliation;
    }

    public int getRegressionGain()
    {
        return regressionGain;
    }

    public int getArousalGain()
    {
        return arousalGain;
    }

    public double getSuccessChance()
    {
        return successChance;
    }

    public boolean isCanCauseAccident()
    {
        return canCauseAccident;
    }
}
