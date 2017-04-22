package com.early_reflections.data.local;


import com.early_reflections.Quote;
import com.early_reflections.tools.Test;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LocalDataSource {


    public List<Quote> getFromFile(File file) {
        List<Quote> quotes = new ArrayList<>();
        try {
            ExtQuote[] data = new Gson().fromJson(new FileReader(file), ExtQuote[].class);
            for (int i = 0; i < data.length; i++) {
                ExtQuote extQuote = data[i];
                quotes.add(new Quote(new LocalDate(extQuote.date), extQuote.value));
            }
        } catch (FileNotFoundException e) {
            // TODO
            e.printStackTrace();
        }
        quotes.sort(Comparator.comparing(Quote::getDate));
        return quotes;

    }

    public List<Quote> getFromFile(String filename) {
        try {
            URL url = Test.class.getClassLoader().getResource(filename);
            return getFromFile(new File(url.toURI()));
        } catch (URISyntaxException e) {
            // TODO
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void writeToFile(List<Quote> quotes, File file) {
        try {
            ArrayList<ExtQuote> extQuotes = new ArrayList();
            for (Quote q : quotes) {
                extQuotes.add(new ExtQuote(q));
            }
            FileUtils.writeStringToFile(file, new Gson().toJson(extQuotes));
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }


    public class ExtQuote {
        private Double value;
        private String date;

        public ExtQuote() {
        }

        public ExtQuote(Quote q) {
            this.date = q.getDate().toString();
            this.value = q.getValue();
        }
    }

}
