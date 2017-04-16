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
import javafx.scene.chart.LineChart;
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
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private LineChart<String,Number> quotesChart;

    @FXML
    private LineChart<String,Number> balanceChart;

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
    private Broker broker = new Broker();

    private Strategy strategy = new Strategy200();
    private int tickSleepMs = 0; // TODO volatile??
    private final static Logger LOG = LoggerFactory.getLogger(YahooDataSource.class);


    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        quotesChart.getData().add(quoteSeries);
        balanceChart.getData().add(balanceSeries);

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

                // TODO performance could be improved by adding the data block wise to the chart (collect and add every 100ms or so). Do this avter extracted the MVC pattern
                Platform.runLater(() -> {
                    XYChart.Data quoteData = new XYChart.Data(q.getDate().toString(), q.getOpen());
                    addTradeNode(quoteData, trade);
                    quoteSeries.getData().add(quoteData);

                    XYChart.Data balanceData = new XYChart.Data(q.getDate().toString(), accountWorth);
                    balanceSeries.getData().add(balanceData);
                });
            }
            return 0;
        }
    };

    private List<Quote> fetchData(String symbol) {
        try {
            File file = new File("^GDAXI.json");
            if(!file.exists() ){
                LOG.debug("No data file for symbol "+symbol+" found. Downloading it from internet.");
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
    private void addTradeNode(XYChart.Data data, Trade trade) {
        // TODO add something more eye cyndy
        if( trade.isBuy()){
            data.setNode(new Circle(6, Color.GREEN));
        }
        else if( trade.isSell()){
            data.setNode(new Circle(6, Color.RED));
        }
    }

}
