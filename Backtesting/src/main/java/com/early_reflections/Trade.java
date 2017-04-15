package com.early_reflections;

/**
 * Created by x on 15/04/2017.
 */
public class Trade {

    private int quantity;
    public enum Type {BUY,SELL}
    private boolean isSell, isBuy;

    public Trade(Type type, int quantity){
        this.quantity = quantity;
        isBuy = type == Type.BUY;
        isSell = type == Type.SELL;
    }

    public Trade(){}

    public boolean isSell(){
       return  isSell;
    }

    public boolean isBuy(){
        return  isBuy;
    }

    public int getQuantity() {
        return quantity;
    }
}
