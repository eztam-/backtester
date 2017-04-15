package com.early_reflections;

import com.early_reflections.json.Quote;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.List;


public class Main extends Application {

    static XYChart.Series quoteSeries = new XYChart.Series();


    @Override
    public void start(Stage stage) {
        stage.setTitle("Line Chart Sample");

        final CategoryAxis xAxisLineChart = new CategoryAxis();
        final NumberAxis yAxisLineChart = new NumberAxis();
        xAxisLineChart.setLabel("Number of Month");
        final LineChart<String,Number> lineChart = new LineChart(xAxisLineChart,yAxisLineChart);
        lineChart.setTitle("Stock Monitoring, 2010");
        quoteSeries.setName("My portfolio");
        lineChart.getData().add(quoteSeries);
        lineChart.setCreateSymbols(false);
        lineChart.setAnimated(false);


        Scene scene  = new Scene(lineChart,800,600);


        stage.setScene(scene);
        stage.show();

        new Thread(task).start();


    }


    Task<Integer> task = new Task<Integer>() {
        @Override protected Integer call() throws Exception {


            YahooData t = new YahooData();
            List<Quote> quotes = t.fetchQuotesPastYears(6); // TODO move this old stuff to separate class
            int i=0;
            for (final Quote q : quotes) {
                if (isCancelled()) {
                    break;
                }
final int i2 = i++;
                Thread.sleep(100); // TODO make this configurable in UI via slider
                // TODO performance could be improved by adding the data block wise to the chart (collect and add every 100ms or so)
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        XYChart.Data data = new XYChart.Data(q.getDate().toString(), q.getOpen());


                            if(i2%10==0) {
                                data.setNode(new Circle(6, Color.GREEN));

                            }
                        if(i2%15==0) {
                            data.setNode(new Circle(6, Color.ORANGE));

                        }
                        quoteSeries.getData().add(data);
                    }
                });
            }
            return 0;
        }
    };

    public static void main(String[] args) {
        launch(args);
    }
}