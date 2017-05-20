package com.early_reflection.api;

import org.joda.time.LocalDate;

public class Quote {

    private Double value;
    private LocalDate date;

    public Quote(){

    }

    public Quote(LocalDate date, Double value) {
        this.date = date;
        this.value= value;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = new LocalDate(date);
    }
}
