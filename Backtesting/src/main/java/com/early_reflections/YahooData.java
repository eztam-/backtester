package com.early_reflections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jfree.ui.RefineryUtilities;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.early_reflections.indicators.Indicator;
import com.early_reflections.indicators.MovingAverage;
import com.early_reflections.json.HistoricalData;
import com.early_reflections.json.Quote;
import com.google.gson.Gson;

public class YahooData {


	List<Quote> fetchQuotesPastYears(int lastYears) {
		List<Quote> quotes = new ArrayList<Quote>();
		int year = new LocalDate().getYear();
		for (int i = 0; i < lastYears; i++) {
			List<Quote> q = fetchQuotes(year);
			if (q == null) {
				// TODO
			}
			quotes.addAll(q);
			year--;
		}
		return sortQuotes(quotes);
	}

	private List<Quote> sortQuotes(List<Quote> quotes) {
		Collections.sort(quotes, new Comparator<Quote>() {

			@Override
			public int compare(Quote o1, Quote o2) {
				return o1.getDate().compareTo(o2.getDate());
			}
		});
		return quotes;
	}

	private List<Quote> fetchQuotes(int year) {
		LocalDate from = new LocalDate(year, 1, 1);
		LocalDate to = from.dayOfYear().withMaximumValue();
		if (to.compareTo(new LocalDate()) == 1) {
			to = new LocalDate();
		}
		return fetchQuotes(from, to);
	}

	/**
	 * Maximum range between from and to is 1 year
	 */
	private List<Quote> fetchQuotes(LocalDate from, LocalDate to) {
		try {
			URI uri = buildQuery(from, to);
			HttpGet httpget = new HttpGet(uri);
			CloseableHttpClient httpclient = HttpClients.createDefault();
			CloseableHttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				if (instream != null) {
					final Gson gson = new Gson();
					final BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
					HistoricalData data = gson.fromJson(reader, HistoricalData.class);
					List<Quote> quotes = data.getQuery().getResults().getQuote();
					instream.close();
					return sortQuotes(quotes);
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private URI buildQuery(LocalDate from, LocalDate to) throws URISyntaxException {
		String toStr = toDateString(to);
		String fromStr = toDateString(from);
		URI uri = new URIBuilder()//
				.setScheme("http")//
				.setHost("query.yahooapis.com")//
				.setPath("/v1/public/yql")//
				.setParameter(
						"q",
						"select * from yahoo.finance.historicaldata where symbol = \"^GDAXI\" and startDate = \""
								+ fromStr + "\" and endDate = \"" + toStr + "\"")//
				.setParameter("format", "json")//
				.setParameter("env", "store://datatables.org/alltableswithkeys")//
				.build();
		return uri;
	}

	public static String toDateString(LocalDate date) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(Quote.DATE_FORMAT);
		return date.toString(formatter);
	}

}
