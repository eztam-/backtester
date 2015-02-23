package com.early_reflections.json;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;

@Generated("org.jsonschema2pojo")
public class Query {

	@Expose
	private Integer count;
	@Expose
	private String created;
	@Expose
	private String lang;
	@Expose
	private Results results;

	/**
	 * 
	 * @return The count
	 */
	public Integer getCount() {
		return count;
	}

	/**
	 * 
	 * @param count
	 *            The count
	 */
	public void setCount(Integer count) {
		this.count = count;
	}

	/**
	 * 
	 * @return The created
	 */
	public String getCreated() {
		return created;
	}

	/**
	 * 
	 * @param created
	 *            The created
	 */
	public void setCreated(String created) {
		this.created = created;
	}

	/**
	 * 
	 * @return The lang
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * 
	 * @param lang
	 *            The lang
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}

	/**
	 * 
	 * @return The results
	 */
	public Results getResults() {
		return results;
	}

	/**
	 * 
	 * @param results
	 *            The results
	 */
	public void setResults(Results results) {
		this.results = results;
	}

}
