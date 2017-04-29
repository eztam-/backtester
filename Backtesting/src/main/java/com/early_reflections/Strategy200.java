package com.early_reflections;

import com.early_reflections.indicators.MovingAverage;
import com.early_reflections.data.yahoo.ExtQuote;
import org.joda.time.LocalDate;

/**
 * This strategy places a long order if the 60 days moving average goes
 * above the moving average of the last 200 days. If the price falls down ans the opposite happens,
 * all open positions will be closed.
 *
 * @author Matthias Birschl
 *
 */
public class Strategy200 extends Strategy {

    private final boolean isCostAveraging;
    private MovingAverage ma200 = new MovingAverage(200);
    private MovingAverage ma60 = new MovingAverage(60);
    private int month = 0;

    /**
     *
     * @param isCostAveraging If false, a fixed account balance is used. If true the account increases by a monthly deposit
     */
	public Strategy200(boolean isCostAveraging) {
	    this.isCostAveraging = isCostAveraging;
		registerIndicator(ma200);
        registerIndicator(ma60);
	}

	@Override
	protected Trade tick(Quote quote) {

        int m = quote.getDate().getMonthOfYear();
        if(isCostAveraging && m!=month){
            month = m;
            // Monthly savings
            Broker.instance().deposit(1600);
        }


        int lastIndex = getQuotes().size() - 1;
        if(lastIndex<3){
            return  null;
        }

        LocalDate lastDate = getQuotes().get(lastIndex).getDate();
        LocalDate secondLastDate = getQuotes().get(lastIndex-1).getDate();

        Double last60 = ma60.getValues().get(lastDate);
        Double last200 = ma200.getValues().get(lastDate);
        Double seconLast60 = ma60.getValues().get(secondLastDate);
        Double seconLast200 = ma200.getValues().get(secondLastDate);

        if(last60== null || last200 == null || seconLast60 == null || seconLast200==null){
            return null;
        }

        if(last60 > last200 && seconLast60 < seconLast200) {
		    return new Trade(Trade.Type.BUY, getMaxQuantity(quote));
		}else if(last60 < last200 && seconLast60 > seconLast200) {
            int numShares = Broker.instance().getHoldings();
			return new Trade(Trade.Type.SELL, numShares);
		}

		return null;
	}

    public int getMaxQuantity(Quote quote) {
        double cash = Broker.instance().getCash();

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
