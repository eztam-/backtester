package com.early_reflection.api;

import java.util.ArrayList;
import java.util.List;

public abstract class Strategy {

	private List<Quote> quotes = new ArrayList<>();

	private List<Indicator> indicators = new ArrayList<>();

	/**
	 * For internal use only.
     */
	public final Trade processTick(Quote quote) {
		this.quotes.add(quote);
		for(Indicator i: indicators){
		    i.processTick(quote);
        }
		return tick(quote);
	}

	/**
	 * Is called on each quote tick (e.g. daily).
	 * 
	 * @param quote The quote of the current tick.
	 * @return A buy or sell trade if the strategy decides to perform a trade. Otherwise null.
	 */
	protected abstract Trade tick(Quote quote);

    /**
     * @return All historic quotes.
     */
	public final List<Quote> getQuotes() {
		return quotes;
	}

	/**
	 * Registers a new {@link Indicator}. Indicators will automatically be calculated on the fly on each tick.
	 * Multiple indicators can be added and all of them are drawn on the main chart.
	 * @param indicator
	 */
	protected final void registerIndicator(Indicator indicator){
        indicators.add(indicator);
    }

	/**
	 * @return A list of all registered {@link Indicator}s.
	 */
    public final List<Indicator> getIndicators(){
	    return indicators;
    }

}
