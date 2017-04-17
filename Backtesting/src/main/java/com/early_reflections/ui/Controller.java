package com.early_reflections.ui;

import com.early_reflections.*;
import com.early_reflections.yahoodata.Quote;
import com.early_reflections.yahoodata.YahooDataSource;
import com.google.gson.Gson;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.java2d.loops.FillRect;

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
   // private Strategy strategy = new StrategyBuyAndHold();
    private IndicatorSeries indicatorHandler;

    private int tickSleepMs = 0; // TODO volatile??
    private final static Logger LOG = LoggerFactory.getLogger(YahooDataSource.class);

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
                // TODO with fast speed, this lead to problems because of overlapping executions. Ensure to not run a new execution until the previous has not been finished
                if (quotes.size() != quoteSeries.getData().size()) { // If chart data has changed
                    quoteSeries.getData().setAll(getQuoteChartData());
                    balanceSeries.getData().setAll(getBalanceChartData());
                    indicatorHandler.updateChart();
                }
            }));
            periodicChartUpdater.setCycleCount(Timeline.INDEFINITE);
            periodicChartUpdater.play();


            List<Quote> quotes = fetchData("^GDAXI");
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
            addTradeNode(data, chartQuote.trade);
            chartData.add(data);
        }
        return chartData;
    }

    public Collection getBalanceChartData() {
        List<XYChart.Data> balance = new ArrayList<>();
        for (int i = 0; i < balanceData.size(); i++) {
            XYChart.Data b = new XYChart.Data(i, balanceData.get(i));
            balance.add(b);
        }
        return balance;
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
    private void addTradeNode(XYChart.Data data, Trade trade) {
        // TODO add something more eye cyndy

        if (trade.isBuy()) {


        /*
            Canvas canvas = new Canvas(51, 113);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setStroke(Color.WHITE);
            gc.setGlobalAlpha(0.5);
            gc.strokeLine(26,13,26,113);
            gc.setStroke(Color.ORANGE);
            gc.setFill(Color.ORANGE);
            gc.setGlobalAlpha(1);
            gc.fillOval(23,12,6,6);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.TOP);
            gc.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.EXTRA_LIGHT, 9));
            gc.setStroke(null);
            gc.fillText(
                    "288",
                    Math.round(canvas.getWidth()  / 2),
                   0
            );
            data.setNode(canvas);
        */

            Group group = new Group();
            Line line = new Line(0,0,0,100);
            line.setOpacity(0.5);
            line.setStroke(Color.WHITE);
            group.getChildren().add(line);
            group.getChildren().add(new Circle(3,Color.LAWNGREEN));
            data.setNode(group);
        } else if (trade.isSell()) {
            Group group = new Group();
            Line line = new Line(0,0,0,100);
            line.setOpacity(0.5);
            line.setStroke(Color.WHITE);
            group.getChildren().add(line);
            group.getChildren().add(new Circle(3,Color.ORANGERED));
            data.setNode(group);
        }
    }


    public class ChartQuote {
        double value;
        String label;
        int xAxisId;
        Trade trade;

        ChartQuote(Quote quote, Trade trade) {
            value = quote.getOpen();
            this.trade = trade;
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
