package com.early_reflection.data;

import com.early_reflection.api.Quote;
import com.early_reflection.data.local.LocalDataSource;
import com.early_reflection.data.yahoo.YahooDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


public class DataSource {

    private final static Logger LOG = LoggerFactory.getLogger(DataSource.class);

    public List<Quote> fromFile(String filename){
        URI uri = null;
        try {
            uri = getClass().getClassLoader().getResource(filename).toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace(); // TODO
        }
        File file = new File(uri);
        return new LocalDataSource().getFromFile(file);
    }

    public List<Quote> fromYahoo(String symbol){
        File file = new File(symbol+".json");
        if (!file.exists()) {
            LOG.debug("No data file for symbol " + symbol + " found. Downloading it from internet.");
            // TODO show ui progress bar
            YahooDataSource t = new YahooDataSource();
            List<Quote> quotes = t.fetchHistoricQuotes(symbol); // TODO move this old stuff to separate class
            new LocalDataSource().writeToFile(quotes,file);
        }
        return new LocalDataSource().getFromFile(file);
    }






}
