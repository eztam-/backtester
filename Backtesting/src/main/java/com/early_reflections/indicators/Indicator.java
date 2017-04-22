package com.early_reflections.indicators;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.early_reflections.Quote;
import com.early_reflections.data.yahoo.ExtQuote;
import org.joda.time.LocalDate;

public abstract class Indicator {

    private List<Quote> quotes = new ArrayList<>();

    private SortedMap<LocalDate, Double> values = new ConcurrentSkipListMap();

    /**
     * @return A unique identifier for the indicator.
     */
    public abstract String getId();

    /**
     * Gets called on each tick e.g. daily.
     *
     * @param quote The current quote
     * @return The calculated indicator value for the current tick or null if the indicator has no value for the current tick.
     */
    protected abstract Double tradingDayTick(Quote quote);

    public SortedMap<LocalDate, Double> getValues() {
        return values;
    }

    /**
     * For internal usage only.
     *
     * @param quote
     */
    public void performTradingDay(Quote quote) {
        this.quotes.add(quote);
        Double value = tradingDayTick(quote);
        if (value != null) {
            values.put(quote.getDate(), value);
        }
    }

    public List<Quote> getQuotes() {
        return quotes;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
