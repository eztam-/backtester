package com.early_reflections;

import com.early_reflections.json.Quote;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;


public class Main extends Application {

     private XYChart.Series quoteSeries = new XYChart.Series();
     private XYChart.Series balanceSeries = new XYChart.Series();
    private Broker broker = new Broker();

    private Strategy strategy = new Strategy200();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Line Chart Sample");

        Scene scene  = new Scene(createUiComponents(),800,600);
        stage.setScene(scene);
        stage.show();

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
                Thread.sleep(10); // TODO make this configurable in UI via slider
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



     VBox createUiComponents() {

        // Create quote chart
         final CategoryAxis xAxisQuoteChart = new CategoryAxis();
         final NumberAxis yAxisQuoteChart = new NumberAxis();
         final LineChart<String,Number> lineChart = new LineChart(xAxisQuoteChart,yAxisQuoteChart);
         lineChart.setTitle("Quotes");
         lineChart.getData().add(quoteSeries);
         lineChart.setCreateSymbols(false);
         lineChart.setAnimated(false);
         lineChart.setLegendVisible(false);

         // Create balance chart
         final CategoryAxis xAxisBalanceChart = new CategoryAxis();
         final NumberAxis yAxisBalanceChart = new NumberAxis();
         final LineChart<String,Number> balanceChart = new LineChart(xAxisBalanceChart,yAxisBalanceChart);
         balanceChart.setTitle("Account balance");
         balanceChart.getData().add(balanceSeries);
         balanceChart.setCreateSymbols(false);
         balanceChart.setAnimated(false);
         balanceChart.setLegendVisible(false);

         // Speed slider
         Slider slider = new Slider();
         slider.setMin(0);
         slider.setMax(100);
         slider.setValue(0);
         slider.setShowTickLabels(true);
         slider.setShowTickMarks(true);
         slider.setMajorTickUnit(50);
         slider.setMinorTickCount(5);
         slider.setBlockIncrement(10);

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);
        vbox.getChildren().add(lineChart);
        vbox.getChildren().add(balanceChart);
        vbox.getChildren().add(slider);

        Text title = new Text("Data");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Hyperlink options[] = new Hyperlink[]{
                new Hyperlink("Sales"),
                new Hyperlink("Marketing"),
                new Hyperlink("Distribution"),
                new Hyperlink("Costs")};

        for (int i = 0; i < 4; i++) {
            VBox.setMargin(options[i], new Insets(0, 0, 0, 8));
            vbox.getChildren().add(options[i]);
        }

        return vbox;

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

    private Trade strategyTick(Quote q) {
        return new Trade();
    }

    public static void main(String[] args) {
        launch(args);
    }
}