package com.early_reflections;

import com.early_reflections.indicators.MovingAverage;
import com.early_reflections.yahoodata.Quote;

/**
 * This strategy places a long order if the price moves from a lower value to
 * above the moving average of the last 200 days. If the price falls down to a
 * value lower than the moving average then all open positions will be closed.
 * 
 * @author Matthias Birschl
 *
 */
public class StrategyBuyAndHold extends Strategy {

	public StrategyBuyAndHold() {

	}

	@Override
	protected Trade tick(Quote quote) {
        int lastIndex = getQuotes().size() - 1;
        if(lastIndex<200){
            return  null;
        }
        int qty = getMaxQuantity(quote);
        if(qty==0){
            return null;
        }
        return new Trade(Trade.Type.BUY,qty );
	}

    public int getMaxQuantity(Quote quote) {
        double cash = Broker.instance().getCash();
        
        Double price = quote.getOpen();

        int maxQty =0;
        for(; maxQty*price<cash; maxQty++){

        }
        if(maxQty>0){
            maxQty--;
        }
        return maxQty;

    }
}
