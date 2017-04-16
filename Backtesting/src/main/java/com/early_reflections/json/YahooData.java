package com.early_reflections.json;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.gson.Gson;

public class YahooData {

    public class DataFetchException extends RuntimeException {
        public DataFetchException() {
            super("Error while fetching quotes from server");
        }
        public DataFetchException( Exception e) {
            super("Error while fetching quotes from server", e);
        }
    }

    public List<Quote> fetchQuotesPastYears(int lastYears) {
        JsonArray quotes = new JsonArray();
        int year = new LocalDate().getYear();
        for (int i = 0; i < lastYears; i++) {
            JsonArray q = fetchQuotes(year);
            if (q == null) {
               throw new DataFetchException();
            }
            quotes.addAll(q);
            year--;
        }
        Quote[] q = new Gson().fromJson(quotes, Quote[].class);
        return sortQuotes(Arrays.asList(q));
    }

    private List<Quote> sortQuotes(List<Quote> quotes) {
        quotes.sort(Comparator.comparing(Quote::getDate));
        return quotes;
    }

    private JsonArray fetchQuotes(int year) {
        LocalDate from = new LocalDate(year, 1, 1);
        LocalDate to = from.dayOfYear().withMaximumValue();
        if (to.getYear() == new LocalDate().getYear()) {
            to = new LocalDate();
        }
        return fetchQuotes(from, to);
    }

    /**
     * Maximum range between from and to is 1 year
     */
    private JsonArray fetchQuotes(LocalDate from, LocalDate to) {
        try {
            URI uri = buildQuery(from, to);
            HttpGet httpget = new HttpGet(uri);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new DataFetchException();
            }
            InputStreamReader reader = new InputStreamReader(entity.getContent());
            JsonArray quotes = new Gson().fromJson(reader, JsonObject.class)
                    .getAsJsonObject("query")
                    .getAsJsonObject("results")
                    .getAsJsonArray("quote");
            reader.close();
            return quotes;
        } catch (URISyntaxException | IOException e) {
            throw new DataFetchException(e);
        }
    }

    private URI buildQuery(LocalDate from, LocalDate to) throws URISyntaxException {
        String toStr = toDateString(to);
        String fromStr = toDateString(from);
        return new URIBuilder()//
                .setScheme("http")//
                .setHost("query.yahooapis.com")//
                .setPath("/v1/public/yql")//
                .setParameter("q", "select * from yahoo.finance.historicaldata where symbol = \"^GDAXI\" and startDate = \""
                        + fromStr + "\" and endDate = \"" + toStr + "\"")//
                .setParameter("format", "json")//
                .setParameter("env", "store://datatables.org/alltableswithkeys")//
                .build();
    }

    private static String toDateString(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(Quote.DATE_FORMAT);
        return date.toString(formatter);
    }

}
