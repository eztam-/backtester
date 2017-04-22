package com.early_reflections.indicators;

import com.early_reflections.Quote;
import com.early_reflections.data.local.LocalDataSource;
import com.early_reflections.data.yahoo.ExtQuote;

import java.util.List;

/**
 * Created by x on 22/04/2017.
 */
public class SP500ShillerPe extends Indicator{

    public SP500ShillerPe(){
        List<com.early_reflections.Quote> data = new LocalDataSource().getFromFile("indicators/S&P500-Shiller-PE.json");
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    protected Double tradingDayTick(Quote quote) {
        return null;
    }
}
