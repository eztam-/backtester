package com.early_reflection.api;

public class Trade {

    private final int quantity;
    public enum Type {BUY,SELL}
    private final boolean isSell, isBuy;

    public Trade(Type type, int quantity){
        this.quantity = quantity;
        isBuy = type == Type.BUY;
        isSell = type == Type.SELL;
    }

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
