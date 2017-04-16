package com.early_reflections.indicators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.early_reflections.yahoodata.Quote;

public abstract class Indicator {

	private List<Quote> quotes = new ArrayList<>();

	public abstract String getId();

	public abstract List<Double> getValues();

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

	@Override
	public int hashCode() {
		return getId().hashCode();
	}
}
