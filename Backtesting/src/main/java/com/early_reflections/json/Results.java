package com.early_reflections.json;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;

@Generated("org.jsonschema2pojo")
public class Results {

	@Expose
	private List<Quote> quote = new ArrayList<Quote>();

	/**
	 * 
	 * @return The quote
	 */
	public List<Quote> getQuote() {
		return quote;
	}

	/**
	 * 
	 * @param quote
	 *            The quote
	 */
	public void setQuote(List<Quote> quote) {
		this.quote = quote;
	}

}
