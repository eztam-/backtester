package com.early_reflections.data.yahoo;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import com.early_reflections.Quote;
import com.early_reflections.ui.UiException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

    public List<Quote> fetchHistoricQuotes(String symbol) {
        List<ExtQuote> quotes = new LinkedList<>();
        int year = new LocalDate().getYear();
        for (int i = 0; i < 100; i++) {
            List<ExtQuote> q = fetchQuotes(year, symbol);
            if (q == null) {
                LOG.debug("No older data than " + year + " is available. Stopping here");
                break;
            }
            quotes.addAll(q);
            year--;
        }
        List<Quote> q = convertQuotes(quotes);
        q.sort(Comparator.comparing(Quote::getDate));
        return q;
    }

    // TODO streams
    private List<com.early_reflections.Quote> convertQuotes(List<ExtQuote> extQuotes) {
        List<com.early_reflections.Quote> quotes = new ArrayList<>();
        for(ExtQuote q: extQuotes){
            quotes.add(new com.early_reflections.Quote(q.getDate(), q.getOpen()));
        }
        return quotes;
    }


    private List<ExtQuote> fetchQuotes(int year, String symbol) {
        LOG.debug("Fetching data for year: " + year);
        LocalDate from = new LocalDate(year, 1, 1);
        LocalDate to = from.dayOfYear().withMaximumValue();
        if (to.getYear() == new LocalDate().getYear()) {
            to = new LocalDate();
        }
        return fetchQuotes(from, to, symbol);
    }

    /**
     * Maximum range between from and to is 1 year
     */
    private List<ExtQuote> fetchQuotes(LocalDate from, LocalDate to, String symbol) {
        try {
            URI uri = buildQuery(from, to, symbol);
            HttpGet httpget = new HttpGet(uri);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new UiException("Error while fetching quotes from server");
            }
            InputStreamReader reader = new InputStreamReader(entity.getContent());
            JsonElement results = new Gson().fromJson(reader, JsonObject.class).getAsJsonObject("query").get("results");
            reader.close();
            if (results.isJsonNull()) {
                // No older data available.
                return null;
            }
            JsonArray quote = results.getAsJsonObject().getAsJsonArray("quote");
            ExtQuote[] q = new Gson().fromJson(quote, ExtQuote[].class);
            return Arrays.asList(q);

        } catch (URISyntaxException | IOException e) {
            throw new UiException("Error while fetching quotes from server",e);
        }
    }

    private URI buildQuery(LocalDate from, LocalDate to, String symbol) throws URISyntaxException {
        String toStr = toDateString(to);
        String fromStr = toDateString(from);
        return new URIBuilder()//
                .setScheme("http")//
                .setHost("query.yahooapis.com")//
                .setPath("/v1/public/yql")//
                .setParameter("q", "select * from yahoo.finance.historicaldata where symbol = \""+symbol+"\" and startDate = \""
                        + fromStr + "\" and endDate = \"" + toStr + "\"")//
                .setParameter("format", "json")//
                .setParameter("env", "store://datatables.org/alltableswithkeys")//
                .build();
    }

    private static String toDateString(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(ExtQuote.DATE_FORMAT);
        return date.toString(formatter);
    }

}
