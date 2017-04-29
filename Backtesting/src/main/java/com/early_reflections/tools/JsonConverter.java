package com.early_reflections.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A little tool for converting json dates and values
 */
public class JsonConverter {

    public static void main(String argv[]) throws URISyntaxException, FileNotFoundException {
        URL url = JsonConverter.class.getClassLoader().getResource("S&P500-Index.json");
        Quote1[] data = new Gson().fromJson(new FileReader(new File(url.toURI())), Quote1[].class);

        List<Quote2>  q2 = new ArrayList<>();
        for(int i=0; i<data.length; i++){
           q2.add( new Quote2(data[i]));
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String str = gson.toJson(q2).toString();
        System.out.println(str);
    }

    private static class Quote2 {

        private final String date;
        double value =0;
        public Quote2(Quote1 q) {
            this.value = Double.parseDouble(q.value.trim().replace(",",""));
            LocalDate date = LocalDate.parse(q.date.trim(), DateTimeFormat.forPattern("MMM d, yyyy"));
            this.date = date.toString();
        }
    }


    public class Quote1 {

        public String value;
        public String date;
    }

}
