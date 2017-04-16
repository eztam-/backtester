package com.early_reflections.ui;

import com.early_reflections.Broker;
import com.early_reflections.Strategy;
import com.early_reflections.Strategy200;
import com.early_reflections.Trade;
import com.early_reflections.yahoodata.Quote;
import com.early_reflections.yahoodata.YahooDataSource;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private LineChart<Number,Number> quotesChart;

    @FXML
    private LineChart<Number,Number> balanceChart;

    @FXML
    private Button playButton;

    @FXML
    private Button pauseButton;

    @FXML
    private Button stopButton;

    @FXML
    private Slider speedSlider;

    private XYChart.Series quoteSeries = new XYChart.Series();
    private XYChart.Series balanceSeries = new XYChart.Series();

    private List<ChartQuote> quotes = new ArrayList<>();


    private long lastChartUpdateTs = 0;

    private Broker broker = new Broker();

    private Strategy strategy = new Strategy200();
    private int tickSleepMs = 0; // TODO volatile??
    private final static Logger LOG = LoggerFactory.getLogger(YahooDataSource.class);

    public class ChartQuote extends Number {
        double value;
        boolean buy, sell;
        String label;
        int xAxisId;

        ChartQuote(Quote quote, Trade trade){
            value = quote.getOpen();
            buy = trade.isBuy();
            sell = trade.isSell();
            label = quote.getDate().toString();
           // xAxisId = quote.getDate().toDateTimeAtStartOfDay().getMillis()/100000;
        }

        @Override
        public int intValue() {
            return xAxisId;
        }

        @Override
        public long longValue() {
            return xAxisId;
        }

        @Override
        public float floatValue() {
            return xAxisId;
        }

        @Override
        public double doubleValue() {
            return xAxisId;
        }
    }

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        quotesChart.getData().add(quoteSeries);
        balanceChart.getData().add(balanceSeries);
      // ((NumberAxis)quotesChart.getXAxis()).setForceZeroInRange(false);
      // ((NumberAxis)balanceChart.getXAxis()).setForceZeroInRange(false);


        playButton.setOnAction(event -> startBacktest(event));
        stopButton.setOnAction(event -> task.cancel());
        // TODO pauseButton

        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Slider Value Changed (newValue: " + newValue.intValue() + ")");
            tickSleepMs = newValue.intValue();
        });


    }

    private void startBacktest(ActionEvent event) {
        new Thread(task).start();
        task.setOnSucceeded(evt -> System.out.println("Task succeeded!"));
        task.setOnCancelled(evt -> System.out.println("Task cancelled!"));
        task.setOnFailed(evt -> {
            if (task.getException() instanceof Broker.BrokerException) {
                System.out.println("Broker error: "+task.getException().getMessage());
            }
            else{
                task.getException().printStackTrace();
            }
        });
    }


    // TODO extract this to MVC pattern
    Task<Integer> task = new Task<Integer>() {
        @Override
        protected Integer call() throws InterruptedException {


            List<Quote> quotes = fetchData("^DAXI");

            for (final Quote q : quotes) {
                if (isCancelled()) { // TODO ad cancel button in UI
                    break;
                }
                Thread.sleep(tickSleepMs); // TODO make this configurable in UI via slider
                final Trade trade =  strategy.processTick(q);
                broker.trade(trade, q);
                final double accountWorth = broker.getAccountWorth(q);

                updateCharts(accountWorth, q, trade);

            }
            return 0;
        }
    };

    private void updateCharts(double accountWorth, Quote q, Trade trade) {
        // TODO performance could be improved by adding the data block wise to the chart (collect and add every 100ms or so). Do this avter extracted the MVC pattern

            //XYChart.Data quoteData = new XYChart.Data(q.getDate().toString(), q.getOpen());
           // addTradeNode(quoteData, trade);
            quotes.add(new ChartQuote(q,trade));
            if(System.currentTimeMillis() - lastChartUpdateTs > 100){
                List<XYChart.Data> aggregated = getAggregatedData();
                Platform.runLater(() -> {
                    quoteSeries.getData().setAll(aggregated);
                });
                lastChartUpdateTs = System.currentTimeMillis();
            }


            XYChart.Data balanceData = new XYChart.Data(q.getDate().toDateTimeAtStartOfDay().getMillis(), accountWorth);
         //   balanceSeries.getData().add(balanceData);




    }


    // TODO use a better aggregation algorithm that takes two vaues (min and max)
    private List<XYChart.Data> getAggregatedData() {
        List<XYChart.Data> aggregated = new ArrayList<>();
        int aggregationFactor = quotes.size() / 1000;
       int xAxisId =0;
        for(int i=0 ; i<quotes.size(); i++){
            if(quotes.size()<1000 || i%aggregationFactor==0){
                ChartQuote aggrQuote = quotes.get(i);
                aggrQuote.xAxisId=xAxisId++;
                System.out.println(aggrQuote.label+" "+aggrQuote.xAxisId);
                XYChart.Data data = new XYChart.Data(aggrQuote.xAxisId, aggrQuote.value);
                addTradeNode(data, aggrQuote.buy, aggrQuote.sell);
                aggregated.add(data);
            }
        }
        return aggregated;
    }

    private List<Quote> fetchData(String symbol) {
        try {
            File file = new File("^GDAXI.json");
            if(!file.exists() ){
                LOG.debug("No data file for symbol "+symbol+" found. Downloading it from internet.");
                // TODO show ui progress bar
                YahooDataSource t = new YahooDataSource();
                List<Quote> quotes = t.fetchHistoricQuotes("^GDAXI"); // TODO move this old stuff to separate class
                FileUtils.writeStringToFile(file, new Gson().toJson(quotes));
            }
            FileReader reader = new FileReader(file);
            Quote[] q = new Gson().fromJson(reader, Quote[].class);
            reader.close();
            return  Arrays.asList(q);
        } catch (IOException e) {
            throw new UiException("Error! Cannot read or write data file.");
        }
    }


    /**
     * Adds the buy or sell nodes to the chart data if a trade was performed
     */
    private void addTradeNode(XYChart.Data data,boolean buy, boolean sell) {
        // TODO add something more eye cyndy
        if( buy){
            data.setNode(new Circle(6, Color.GREEN));
        }
        else if( sell){
            data.setNode(new Circle(6, Color.RED));
        }
    }

}
