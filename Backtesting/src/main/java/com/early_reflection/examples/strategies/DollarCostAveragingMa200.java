package com.early_reflection.examples.strategies;

import com.early_reflection.api.Broker;
import com.early_reflection.api.Quote;
import com.early_reflection.api.Strategy;
import com.early_reflection.api.Trade;
import com.early_reflection.examples.indicators.MovingAverage;
import org.joda.time.LocalDate;

/**
 * This strategy was created in order to find out, weather it makes a difference to only invest in increasing or decreasing markets.
 * It buys monthly like dollar cost averaging but only on declining or increasing markets which can be configured.
 * A declining market is detected when the 60 days moving average is below the 200 days moving average.
 * This is a buy and hold strategy that never sells but may hoard bigger cash stakes.
 * The monthly invested amount is maximum twice the monthly savings rate.
 * 
 * @author Matthias Birschl
 *
 */
public class DollarCostAveragingMa200 extends Strategy {

    private static final int MONTHLY_SAVINGS = 1600;
    private final boolean buyOnDecline;
    private MovingAverage ma200 = new MovingAverage(200);
    private MovingAverage ma60 = new MovingAverage(60);


    private int month =0;
    public DollarCostAveragingMa200(boolean buyOnDecline) {
        registerIndicator(ma200);
        registerIndicator(ma60);
        this.buyOnDecline=buyOnDecline;
	}

	@Override
	protected Trade tick(Quote quote) {
        int m = quote.getDate().getMonthOfYear();
        if(m!=month){
            month = m;
            // Monthly savings
            Broker.instance().deposit(MONTHLY_SAVINGS);


            int lastIndex = getQuotes().size() - 1;
            if(lastIndex<3){
                return  null;
            }


            LocalDate lastDate = getQuotes().get(lastIndex).getDate();
            Double last60 = ma60.getValues().get(lastDate);
            Double last200 = ma200.getValues().get(lastDate);


            if(last60== null || last200 == null){
                return null;
            }

            if(buyOnDecline && last60 > last200) {
                return new Trade(Trade.Type.BUY, getPreferedQuantity(quote));
            }
            if(!buyOnDecline && last60 < last200) {
                return new Trade(Trade.Type.BUY, getPreferedQuantity(quote));
            }

        }

        return null;
	}

    public int getPreferedQuantity(Quote quote) {
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
