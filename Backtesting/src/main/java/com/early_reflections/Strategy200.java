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
public class Strategy200 extends Strategy {

	private MovingAverage ma200 = new MovingAverage(200);
    private MovingAverage ma60 = new MovingAverage(60);

	public Strategy200() {
		registerIndicator(ma200);
        registerIndicator(ma60);
	}

	@Override
	protected Trade tick(Quote quote) {




        int lastIndex = getQuotes().size() - 1;
        if(lastIndex<3){
            return  null;
        }
        Double last60 = ma60.getValues().get(lastIndex);
        Double last200 = ma200.getValues().get(lastIndex);
        Double seconLast60 = ma60.getValues().get(lastIndex-1);
        Double seconLast200 = ma200.getValues().get(lastIndex-1);

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
