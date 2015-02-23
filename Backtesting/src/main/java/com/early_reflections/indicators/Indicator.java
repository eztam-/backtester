package com.early_reflections.indicators;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.joda.time.LocalDate;

import com.early_reflections.json.Quote;

public abstract class Indicator {

	private List<Quote> quotes = new ArrayList<>();

	public abstract String getName();

	public abstract SortedMap<LocalDate, Double> getValues();

	public void performTradingDay(Quote quote) {
		this.quotes.add(quote);
		tradingDayTick(quote);
	}

	public List<Quote> getQuotes() {
		return quotes;
	}

	/**
	 * Gets called on each tick (daily).
	 * 
	 * @param quote
	 *            The quote of the current tick
	 */
	protected abstract void tradingDayTick(Quote quote);
}
