package com.early_reflections;

import com.early_reflections.indicators.MovingAverage;
import com.early_reflections.indicators.SP500ShillerPe;

/**
 * Created by x on 22/04/2017.
 */
public class StrategyValue extends Strategy{

    private SP500ShillerPe shillerPe = new SP500ShillerPe();

    public StrategyValue() {
        registerIndicator(shillerPe);
    }

    @Override
    protected Trade tick(Quote quote) {
        return null;
    }
}
