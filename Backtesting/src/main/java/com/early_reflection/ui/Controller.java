package com.early_reflection.ui;

import com.early_reflection.api.Broker;
import com.early_reflection.api.Quote;
import com.early_reflection.api.Strategy;
import com.early_reflection.api.Trade;
import com.early_reflection.data.DataSource;

import com.early_reflection.examples.strategies.Strategy200;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.StringConverter;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    @FXML
    private Chart quotesChart;

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

    private List<ChartQuote> quotes = Collections.synchronizedList(new ArrayList<>());
    private List<ChartBalance> balanceData = Collections.synchronizedList(new ArrayList<>());

    private Broker broker = Broker.instance();

    private Strategy strategy = new Strategy200(true);
    // private Strategy strategy = new DollarCostAveraging(true);
    // private Strategy strategy = new DollarCostAveragingMa200(true);
    // private Strategy strategy = new StrategyValue();

    private IndicatorSeries indicatorHandler;

    private volatile int tickSleepMs = 0;
    private final static Logger LOG = LoggerFactory.getLogger(Controller.class);
    private BacktestTask task = new BacktestTask();

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        quotesChart.getData().add(quoteSeries);
        balanceChart.getData().add(balanceSeries);
        indicatorHandler = new IndicatorSeries(strategy, quotesChart);
        ((NumberAxis) quotesChart.getXAxis()).setTickLabelFormatter(new XAxisLabelConverter());
        ((NumberAxis) balanceChart.getXAxis()).setTickLabelFormatter(new XAxisLabelConverter());

        ((NumberAxis) quotesChart.getXAxis()).setForceZeroInRange(false);
        ((NumberAxis) balanceChart.getXAxis()).setForceZeroInRange(false);
         // quotesChart.getXAxis().setAutoRanging(false);

        // TODO Disable autoranging and set upper and lower bounds manually in order to keep both charts
        // in sync and have no gaps at the beginning and end


        playButton.setOnAction(this::startBacktest);
        stopButton.setOnAction(event -> task.cancel());
        // TODO pauseButton

        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> tickSleepMs = newValue.intValue());

    }

    private void startBacktest(ActionEvent event) {
        // TODO Use service instead of task. See http://stackoverflow.com/questions/16037062/javafx-use-a-thread-more-than-once
        if(task.isRunning()){
            return;
        }
        // Clear all in case of a restart
        task = new BacktestTask();
        quotes.clear();
        balanceData.clear();
        indicatorHandler.clear();
        // strategy.reset(); TODO

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


     class BacktestTask extends Task<Integer> {

        @Override
        protected Integer call() throws InterruptedException {

            quotesChart.startPeriodicUpdate(() -> {
                if (quotes.size() == quoteSeries.getData().size()) { // If chart data has changed
                    return false;
                }
                // TODO Do only add changed data with addAll instead of updating the whole chart each time!
                quoteSeries.getData().setAll(getQuoteChartData());
                balanceSeries.getData().setAll(getBalanceChartData());
                indicatorHandler.updateChart();
                // quotesChart.getXAxis().setAutoRanging(true);
                return true;
            });

            List<Quote> quotes = fetchData();

            for (final Quote q : quotes) {
                if (isCancelled()) {
                    break;
                }
                Thread.sleep(tickSleepMs);
                final Trade trade = strategy.processTick(q);
                if(trade!=null)
                     broker.trade(trade, q);
                final double accountWorth = broker.getAccountWorth(q);

                // s1.add( new Day(q.getDate().toDate()), q.getOpen()); // TODO Amend performance by adding the values blockwise

                updateChartData(accountWorth, q, trade);

            }
            quotesChart.stopUpdating();

            // TODO This could be removed after using manual ranging globally
            // Auto ranging is not working properly fit JavaFX charts. Therefore set the bounds finally so that the chart
            // uses the maximum available space
            /*
            quotesChart.getXAxis().setAutoRanging(false);
            balanceChart.getXAxis().setAutoRanging(false);
            long first = quotes.get(0).getDate().toDateTimeAtStartOfDay().getMillis();
            long last = quotes.get(quotes.size()-1).getDate().toDateTimeAtStartOfDay().getMillis();
            ((NumberAxis) quotesChart.getXAxis()).setLowerBound(first);
            ((NumberAxis) quotesChart.getXAxis()).setUpperBound(last);
            ((NumberAxis) balanceChart.getXAxis()).setLowerBound(first);
            ((NumberAxis) balanceChart.getXAxis()).setUpperBound(last);
*/
            return 0;
        }
    }


    private void updateChartData(double accountWorth, Quote q, Trade trade) {
        quotes.add(new ChartQuote(q, trade));
        balanceData.add(new ChartBalance(accountWorth, q.getDate()));
    }

    private List<XYChart.Data> getQuoteChartData() {
        List<XYChart.Data> chartData = new ArrayList<>();
        for (int i = 0; i < quotes.size(); i++) {
            ChartQuote chartQuote = quotes.get(i);
            XYChart.Data data = new XYChart.Data(chartQuote.xAxisId, chartQuote.value);
            addTradeNode(data, chartQuote.trade);
            chartData.add(data);
        }
        return chartData;
    }

    public List<XYChart.Data> getBalanceChartData() {
        List<XYChart.Data> balance = new ArrayList<>();
        for (int i = 0; i < balanceData.size(); i++) {
            ChartBalance a = balanceData.get(i);
            XYChart.Data b = new XYChart.Data(a.xAxisId, a.value);
            balance.add(b);
        }
        return balance;
    }

    // TODO Extract the two types of data sources maybe by ui config
    private List<Quote> fetchData() {
        List<Quote> data = new DataSource().fromYahoo("^GDAXI"); // "^GSPC.json"
        //  List<Quote> data = DataSource().fromFile("S&P500-Index.json");

        // The Dax or SP500 are usually not directly tradable and have relatively high index numbers. Therefore simply divide all index points by 100
        for(Quote q: data){
            q.setValue(q.getValue()/100.0);
        }
        return data;
    }


    /**
     * Adds the buy or sell nodes to the chart data if a trade was performed
     */
    private void addTradeNode(XYChart.Data data, Trade trade) {
        // TODO add something more eye cyndy
        if(trade == null){
            return;
        }
        else if (trade.isBuy()) {


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
            Line line = new Line(0, 0, 0, 100);
            line.setOpacity(0.5);
            line.setStroke(Color.WHITE);
            group.getChildren().add(line);
            group.getChildren().add(new Circle(3, Color.LAWNGREEN));
            data.setNode(group);
        } else if (trade.isSell()) {
            Group group = new Group();
            Line line = new Line(0, 0, 0, 100);
            line.setOpacity(0.5);
            line.setStroke(Color.WHITE);
            group.getChildren().add(line);
            group.getChildren().add(new Circle(3, Color.ORANGERED));
            data.setNode(group);
        }
    }


    public class ChartQuote {
        double value;
        long xAxisId;
        Trade trade;

        ChartQuote(Quote quote, Trade trade) {
            value = quote.getValue();
            this.trade = trade;
            xAxisId = quote.getDate().toDateTimeAtStartOfDay().getMillis();
        }
    }


    private class ChartBalance {
        double value;
        long xAxisId;

        ChartBalance(double value, LocalDate date) {
            this.value = value;
            xAxisId = date.toDateTimeAtStartOfDay().getMillis();
        }
    }


    class XAxisLabelConverter extends StringConverter<Number> {

        @Override
        public String toString(Number object) {
            return new LocalDate(object.longValue()).toString("YYYY");
        }

        @Override
        public Number fromString(String string) {
            return null;
        }
    }
}
