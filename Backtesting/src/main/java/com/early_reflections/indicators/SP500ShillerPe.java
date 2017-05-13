package com.early_reflections.indicators;

import com.early_reflections.Quote;
import com.early_reflections.data.local.LocalDataSource;
import org.joda.time.LocalDate;

import java.util.*;

// TODO is painted suboptimal on the chart sice it has a completely different y axis scale
public class SP500ShillerPe extends Indicator{

    private SortedMap<LocalDate, Double> shillerPeValues = new TreeMap<>();

    public SP500ShillerPe(){
        List<Quote> data = new LocalDataSource().getFromFile("indicators/S&P500-Shiller-PE.json");
        for(Quote q: data){
            shillerPeValues.put(q.getDate(),q.getValue());
        }
    }

    @Override
    public String getId() {
        return "SP500 Shiller PE";
    }

    @Override
    protected Double tradingDayTick(Quote quote) {
        return shillerPeValues.get(quote.getDate());
    }
}
