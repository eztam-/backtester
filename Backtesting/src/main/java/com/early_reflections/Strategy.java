package com.early_reflections;

import java.util.ArrayList;
import java.util.List;

import com.early_reflections.indicators.Indicator;
import com.early_reflections.yahoodata.Quote;

public abstract class Strategy {

	private List<Quote> quotes = new ArrayList<>();

	private List<Indicator> indicators = new ArrayList();

	public Trade processTick(Quote quote) {
		this.quotes.add(quote);
		for(Indicator i: indicators){
		    i.performTradingDay(quote);
        }
		Trade trade = tick(quote);
		return trade == null ? new Trade() : trade;
	}

	/**
	 * Is called on each quote tick (e.g. daily).
	 * 
	 * @param quote The quote of the current tick
	 * @return  A buy or sell trade if the strategy decides to perform a trade. Otherwise null.
	 */
	protected abstract Trade tick(Quote quote);

    /**
     *
     * @return All historic quotes
     */
	public List<Quote> getQuotes() {
		return quotes;
	}


	protected void registerIndicator(Indicator indicator){
        indicators.add(indicator);
    }

    public List<Indicator> getIndicators(){
	    return indicators;
    }

}
