package com.early_reflections.yahoodata;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YahooDataSource {

    private final static Logger LOG = LoggerFactory.getLogger(YahooDataSource.class);

    public class DataFetchException extends RuntimeException {
        public DataFetchException() {
            super("Error while fetching quotes from server");
        }

        public DataFetchException(Exception e) {
            super("Error while fetching quotes from server", e);
        }
    }

    public List<Quote> fetchHistoricQuotes() {
        JsonArray quotes = new JsonArray();
        int year = new LocalDate().getYear();
        for (int i = 0; i < 100; i++) {
            JsonArray q = fetchQuotes(year);
            if (q == null) {
                LOG.debug("No older data than " + year + " is available. Stopping here");
                break;
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
        LOG.debug("Fetching data for year: " + year);
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
            JsonObject result = new Gson().fromJson(reader, JsonObject.class).getAsJsonObject("query");
            reader.close();
            if (result.get("results").isJsonNull()) {
               // No older data available.
                return null;
            }
            return result.getAsJsonObject("results").getAsJsonArray("quote");
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
