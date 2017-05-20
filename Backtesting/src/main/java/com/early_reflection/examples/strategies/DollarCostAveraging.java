package com.early_reflection.examples.strategies;

import com.early_reflection.api.Broker;
import com.early_reflection.api.Quote;
import com.early_reflection.api.Strategy;
import com.early_reflection.api.Trade;

/**
 * Simple dollar cost averaging strategy.
 * 
 * @author Matthias Birschl
 *
 */
public class DollarCostAveraging extends Strategy {

    private static final int MONTHLY_SAVINGS = 1600;
    private final boolean isCostAveraging;
    private int month =0;
    public DollarCostAveraging(boolean isCostAveraging) {
        this.isCostAveraging = isCostAveraging;

	}

	@Override
	protected Trade tick(Quote quote) {
        int m = quote.getDate().getMonthOfYear();
        if(isCostAveraging && m!=month){
            month = m;
            // Monthly savings
            Broker.instance().deposit(MONTHLY_SAVINGS);
        }



        int lastIndex = getQuotes().size() - 1;
        if(lastIndex<200){ // Just to be fair when comparing results with 200 day MA strategy
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

        // Dont invest more then twice the monthly savings at once
        if(cash >MONTHLY_SAVINGS*2){
            cash = MONTHLY_SAVINGS *2;
        }

        Double price = quote.getValue();

        int maxQty =0;
        for(; maxQty*price<cash; maxQty++){

        }
        if(maxQty>0){
            maxQty--;
        }
        return maxQty;

    }
}
