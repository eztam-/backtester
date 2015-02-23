package com.early_reflections.json;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;

@Generated("org.jsonschema2pojo")
public class HistoricalData {

	@Expose
	private Query query;

	/**
	 * 
	 * @return The query
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * 
	 * @param query
	 *            The query
	 */
	public void setQuery(Query query) {
		this.query = query;
	}

}
