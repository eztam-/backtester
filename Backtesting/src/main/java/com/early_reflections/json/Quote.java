package com.early_reflections.json;

import javax.annotation.Generated;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Quote {

	public final static String DATE_FORMAT = "yyyy-MM-dd";

	@Expose
	private String Symbol;
	@Expose
	private String Date;
	@Expose
	private Double Open;
	@Expose
	private Double High;
	@Expose
	private Double Low;
	@Expose
	private Double Close;
	@Expose
	private Double Volume;
	@SerializedName("Adj_Close")
	@Expose
	private String AdjClose;

	public String getSymbol() {
		return Symbol;
	}

	public void setSymbol(String symbol) {
		Symbol = symbol;
	}

	public LocalDate getDate() {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_FORMAT);
		return formatter.parseLocalDate(Date);
	}

	public void setDate(String date) {
		Date = date;
	}

	public Double getOpen() {
		return Open;
	}

	public void setOpen(Double open) {
		Open = open;
	}

	public Double getHigh() {
		return High;
	}

	public void setHigh(Double high) {
		High = high;
	}

	public Double getLow() {
		return Low;
	}

	public void setLow(Double low) {
		Low = low;
	}

	public Double getClose() {
		return Close;
	}

	public void setClose(Double close) {
		Close = close;
	}

	public Double getVolume() {
		return Volume;
	}

	public void setVolume(Double volume) {
		Volume = volume;
	}

	public String getAdjClose() {
		return AdjClose;
	}

	public void setAdjClose(String adjClose) {
		AdjClose = adjClose;
	}

}
