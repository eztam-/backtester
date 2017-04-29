package com.early_reflections.indicators;

import com.early_reflections.Quote;
import com.early_reflections.data.yahoo.ExtQuote;


public class MovingAverage extends Indicator {

    private int period;

    public MovingAverage(int period) {
        this.period = period;
    }

    @Override
    public String getId() {
        return  period + " days moving average";
    }

    @Override
    protected Double tradingDayTick(Quote quote) {
        if (getQuotes().size() < period)
            return null;
        double sum = 0;
        for (int i = getQuotes().size() - period; i < getQuotes().size(); i++) {
            sum += getQuotes().get(i).getValue();
        }
        return sum / period;
    }
}