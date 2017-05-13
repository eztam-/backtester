package com.early_reflections;


public class Broker {

    private double balance = 50000;

    // Number of shares
    private int holdings = 0;
    private static Broker instance;

    private Broker(){}

    public class BrokerException extends RuntimeException{
        public BrokerException(String message) {
            super(message);
        }
    }

    public void trade(Trade trade, Quote quote) {

        double sum = quote.getValue() * trade.getQuantity();
        if(trade.isBuy()){
            if(sum > balance){
                // TODO
                System.out.println("Account limit exceeded");
               // throw new BrokerException("Account limit exceeded");
                return ;
            }
            holdings+=trade.getQuantity();
            balance-=sum;
        }
        else if(trade.isSell()){
            if(trade.getQuantity()>holdings){
                System.out.println("Cannot sell more shares than available");
                return ;
                // TODO
              // throw new BrokerException("Cannot sell more shares than available");
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
        return balance + holdings*quote.getValue();
    }

    public static Broker instance(){
        if(instance ==null){
            instance = new Broker();
        }
        return instance;
    }

    public int getHoldings() {
        return holdings;
    }
    public double getCash() {
        return balance;
    }
    public void deposit(double amount){
        balance+=amount;
    }
}
