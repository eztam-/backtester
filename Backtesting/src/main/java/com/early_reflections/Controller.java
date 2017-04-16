package com.early_reflections;

import com.early_reflections.json.Quote;
import com.early_reflections.json.YahooData;
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

import java.net.URL;
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

            YahooData t = new YahooData();
            List<Quote> quotes = t.fetchQuotesPastYears(6); // TODO move this old stuff to separate class

            for (final Quote q : quotes) {
                if (isCancelled()) { // TODO ad cancel button in UI
                    break;
                }
                Thread.sleep(tickSleepMs); // TODO make this configurable in UI via slider
                final Trade trade =  strategy.processTick(q);
                broker.trade(trade, q);
                final double accountWorth = broker.getAccountWorth(q);

                // TODO performance could be improved by adding the data block wise to the chart (collect and add every 100ms or so). Do this avter extracted the MVC pattern
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        XYChart.Data quoteData = new XYChart.Data(q.getDate().toString(), q.getOpen());
                        addTradeNode(quoteData, trade);
                        quoteSeries.getData().add(quoteData);

                        XYChart.Data balanceData = new XYChart.Data(q.getDate().toString(), accountWorth);
                        balanceSeries.getData().add(balanceData);
                    }
                });
            }
            return 0;
        }
    };


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
