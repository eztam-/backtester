package com.early_reflections;

import java.util.ArrayList;
import java.util.List;

import com.early_reflections.json.Quote;

public abstract class Strategy {

	private List<Quote> quotes = new ArrayList<>();

	public void performTradingDay(Quote quote) {
		this.quotes.add(quote);
		tradingDayTick(quote);
	}

	/**
	 * Gets called on each tick (daily).
	 * 
	 * @param quote
	 *            The quote of the current tick
	 */
	protected abstract void tradingDayTick(Quote quote);

	/**
	 * Returns the calculated performance result of the strategy. This method
	 * gets called after the simulation is finished
	 * 
	 * @return The performance of the strategy in percentage.
	 */
	public abstract double getPerformancePercent();

	public List<Quote> getQuotes() {
		return quotes;
	}

}
