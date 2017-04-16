package com.early_reflections.ui;

import com.early_reflections.Broker;
import com.early_reflections.Strategy;
import com.early_reflections.Strategy200;
import com.early_reflections.Trade;
import com.early_reflections.yahoodata.Quote;
import com.early_reflections.yahoodata.YahooDataSource;
import com.google.gson.Gson;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    @FXML
    private AreaChart<Number, Number> quotesChart;

    @FXML
    private AreaChart<Number, Number> balanceChart;

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
    private List<Double> balanceData = new ArrayList<>();

    private Broker broker =  Broker.instance();

    private Strategy strategy = new Strategy200();
    private IndicatorSeries indicatorHandler;

    private int tickSleepMs = 0; // TODO volatile??
    private final static Logger LOG = LoggerFactory.getLogger(YahooDataSource.class);

    public Collection getBalanceChartData() {
        List<XYChart.Data> balance = new ArrayList<>();
        for (int i = 0; i < balanceData.size(); i++) {
            XYChart.Data b = new XYChart.Data(i, balanceData.get(i));
            balance.add(b);
        }
        return balance;
    }

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        quotesChart.getData().add(quoteSeries);
        balanceChart.getData().add(balanceSeries);
        indicatorHandler = new IndicatorSeries(strategy, quotesChart);
        ((NumberAxis) quotesChart.getXAxis()).setTickLabelFormatter(new XAxisLabelConverter());

        playButton.setOnAction(event -> startBacktest(event));
        stopButton.setOnAction(event -> task.cancel());
        // TODO pauseButton

        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            tickSleepMs = newValue.intValue();
        });
    }

    private void startBacktest(ActionEvent event) {
        new Thread(task).start();
        task.setOnSucceeded(evt -> System.out.println("Task succeeded!"));
        task.setOnCancelled(evt -> System.out.println("Task cancelled!"));
        task.setOnFailed(evt -> {
            if (task.getException() instanceof Broker.BrokerException) {
                System.out.println("Broker error: " + task.getException().getMessage());
            } else {
                task.getException().printStackTrace();
            }
        });
    }


    Task<Integer> task = new Task<Integer>() {

        @Override
        protected Integer call() throws InterruptedException {
            // Updating the chart periodically after some time is much more performant than updating on each new data
            Timeline periodicChartUpdater = new Timeline(new KeyFrame(Duration.millis(100), event -> {
                if (quotes.size() != quoteSeries.getData().size()) { // If chart data has changed
                    quoteSeries.getData().setAll(getQuoteChartData());
                    balanceSeries.getData().setAll(getBalanceChartData());
                    indicatorHandler.updateChart();
                }
            }));
            periodicChartUpdater.setCycleCount(Timeline.INDEFINITE);
            periodicChartUpdater.play();


            List<Quote> quotes = fetchData("^DAXI");
            for (final Quote q : quotes) {
                if (isCancelled()) {
                    break;
                }
                Thread.sleep(tickSleepMs);
                final Trade trade = strategy.processTick(q);
                broker.trade(trade, q);
                final double accountWorth = broker.getAccountWorth(q);

                updateChartData(accountWorth, q, trade);

            }
            periodicChartUpdater.setCycleCount(1); // Run one last time and update the result
            return 0;
        }
    };


    private void updateChartData(double accountWorth, Quote q, Trade trade) {
        quotes.add(new ChartQuote(q, trade));
        balanceData.add(accountWorth); // TODO performance could be improved by only adding balance data on change and not for each tick
    }

    private List<XYChart.Data> getQuoteChartData() {
        List<XYChart.Data> chartData = new ArrayList<>();
        int xAxisId = 0;
        for (int i = 0; i < quotes.size(); i++) {
            ChartQuote chartQuote = quotes.get(i);
            chartQuote.xAxisId = xAxisId++;
            XYChart.Data data = new XYChart.Data(chartQuote.xAxisId, chartQuote.value);
            addTradeNode(data, chartQuote.buy, chartQuote.sell);
            chartData.add(data);
        }
        return chartData;
    }


    private List<Quote> fetchData(String symbol) {
        try {
            File file = new File("^GDAXI.json");
            if (!file.exists()) {
                LOG.debug("No data file for symbol " + symbol + " found. Downloading it from internet.");
                // TODO show ui progress bar
                YahooDataSource t = new YahooDataSource();
                List<Quote> quotes = t.fetchHistoricQuotes("^GDAXI"); // TODO move this old stuff to separate class
                FileUtils.writeStringToFile(file, new Gson().toJson(quotes));
            }
            FileReader reader = new FileReader(file);
            Quote[] q = new Gson().fromJson(reader, Quote[].class);
            reader.close();
            return Arrays.asList(q);
        } catch (IOException e) {
            throw new UiException("Error! Cannot read or write data file.");
        }
    }


    /**
     * Adds the buy or sell nodes to the chart data if a trade was performed
     */
    private void addTradeNode(XYChart.Data data, boolean buy, boolean sell) {
        // TODO add something more eye cyndy
        if (buy) {
            data.setNode(new Circle(6, Color.GREEN));
        } else if (sell) {
            data.setNode(new Circle(6, Color.RED));
        }
    }


    public class ChartQuote {
        double value;
        boolean buy, sell;
        String label;
        int xAxisId;

        ChartQuote(Quote quote, Trade trade) {
            value = quote.getOpen();
            buy = trade.isBuy();
            sell = trade.isSell();
            label = quote.getDate().toString();
        }
    }

    class XAxisLabelConverter extends StringConverter<Number> {

        @Override
        public String toString(Number object) {
            if (object.intValue() < quotes.size()) {
                return quotes.get(object.intValue()).label;
            }
            return "";
        }

        @Override
        public Number fromString(String string) {
            return null;
        }
    }

}
