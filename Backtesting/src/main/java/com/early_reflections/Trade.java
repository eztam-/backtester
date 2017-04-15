package com.early_reflections;

/**
 * Created by x on 15/04/2017.
 */
public class Trade {

    public boolean isSell(){
       return  Math.random() >0.9 ? true : false;
    }

    public boolean isBuy(){
        return  Math.random() >0.9 ? true : false;
    }
}
