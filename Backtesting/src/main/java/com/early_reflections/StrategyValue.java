package com.early_reflections;

import com.early_reflections.indicators.SP500ShillerPe;

public class StrategyValue extends Strategy{

    private SP500ShillerPe shillerPe = new SP500ShillerPe();

    public StrategyValue() {
        registerIndicator(shillerPe);
    }

    @Override
    protected Trade tick(Quote quote) {
        Double spe = shillerPe.getValues().get(quote.getDate());
        if(spe!=null && spe<10){
            return new Trade(Trade.Type.BUY,getMaxQuantity(quote));
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
