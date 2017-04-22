package com.early_reflections.ui;

import com.early_reflections.Strategy;
import com.early_reflections.indicators.Indicator;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import org.joda.time.LocalDate;

import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by x on 16/04/2017.
 */
public class IndicatorSeries {


    private final AreaChart<Number, Number> chart;
    private Map<Indicator, XYChart.Series> indicatorSeries = new HashMap<>();


    public IndicatorSeries(Strategy strategy, AreaChart<Number, Number> quotesChart) {
        this.chart = quotesChart;
        for(Indicator i: strategy.getIndicators()){
            XYChart.Series series = new XYChart.Series();
            chart.getData().add(series);
            indicatorSeries.put(i,series);
        }
    }

    public void updateChart() {
        for(Entry<Indicator, XYChart.Series> i: indicatorSeries.entrySet()){

            List<XYChart.Data> chartData = new ArrayList<>();
            Indicator indicator = i.getKey();

            long start =0;


            Iterator<Entry<LocalDate, Double>> it = indicator.getValues().entrySet().iterator();
            while (it.hasNext()) {
                //Double value = indicator.getValues().get(index);
                Entry<LocalDate, Double> entry = it.next();
                Double value = entry.getValue();
                long ts = entry.getKey().toDateTimeAtStartOfDay().getMillis();
                // if(value!=null) {
                if (start == 0) {
                    start = ts;
                }

                // XYChart.Data data = new XYChart.Data(ts-start, entry.getValue());
                XYChart.Data data = new XYChart.Data(ts, value);
                chartData.add(data);
                // }
            }
            i.getValue().getData().setAll(chartData);

        }
    }

    public void clear() {
        for(Entry<Indicator, XYChart.Series> i: indicatorSeries.entrySet()){
            i.getValue().getData().clear();
        }
    }
}
