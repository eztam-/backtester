package com.early_reflections;


import com.early_reflections.json.Quote;

public class Broker {

    private double balance = 10000;

    // Number of shares
    private int holdings = 0;


    public class BrokerException extends RuntimeException{
        public BrokerException(String message) {
            super(message);
        }
    }

    public void trade(Trade trade, Quote quote) {

        double sum = quote.getOpen() * trade.getQuantity();
        if(trade.isBuy()){
            if(sum > balance){
               throw new BrokerException("Account limit exceeded"); // TODO replace by internal exception
            }
            holdings+=trade.getQuantity();
            balance-=sum;
        }
        else if(trade.isSell()){
            if(trade.getQuantity()>holdings){
               throw new BrokerException("Cannot sell more shares than available"); // TODO replace by internal exception
            }
            holdings-=trade.getQuantity();
            balance+=sum;
        }
    }

    /**
     *
     * @param quote The worth of the account is calculated based on the given quotes open value
     * @return The whole worth of the account (holdings + cash)
     */
    public double getAccountWorth(Quote quote){
        return balance + holdings*quote.getOpen();
    }



}
