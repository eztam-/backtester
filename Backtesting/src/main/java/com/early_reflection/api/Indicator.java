package com.early_reflection.api;

import java.util.*;

import org.joda.time.LocalDate;

public abstract class Indicator {

    private final List<Quote> quotes = new ArrayList<>();

    private final SortedMap<LocalDate, Double> values = Collections.synchronizedSortedMap(new TreeMap<>());

    /**
     * @return A unique identifier for the indicator. This identifier is also used as label on the chart.
     */
    public abstract String getId();

    /**
     * Gets called on each tick e.g. daily.
     *
     * @param quote The current quote.
     * @return The calculated indicator value for the current tick or null if the indicator has no value for the current tick.
     */
    protected abstract Double tradingDayTick(Quote quote);

    /**
     * @return The calculated indicator values for all historic quotes.
     */
    public final SortedMap<LocalDate, Double> getValues() {
        return values;
    }

    /**
     * For internal usage only.
     */
    public final void processTick(Quote quote) {
        this.quotes.add(quote);
        Double value = tradingDayTick(quote);
        if (value != null) {
            values.put(quote.getDate(), value);
        }
    }

    /**
     * @return All historic quotes.
     */
    public final List<Quote> getQuotes() {
        return quotes;
    }

    @Override
    public final int hashCode() {
        return getId().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if(obj instanceof Indicator){
            return getId().equals(((Indicator)obj).getId());
        }
        return false;
    }
}
