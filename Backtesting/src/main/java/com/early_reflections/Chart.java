package com.early_reflections;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.joda.time.LocalDate;

import com.early_reflections.indicators.Indicator;
import com.early_reflections.json.Quote;

public class Chart  {

	private static final long serialVersionUID = 1L;

	private List<Quote> quotes;

	private List<Indicator> indicators;

	private void addIndicatorSeries(TimeSeriesCollection dataset) {
		for (Indicator indicator : indicators) {
			TimeSeries series = createIndicatorSeries(indicator);
			dataset.addSeries(series);
		}

	}

	private TimeSeries createIndicatorSeries(Indicator indicator) {
		TimeSeries series = new TimeSeries(indicator.getName());
		for (Entry<LocalDate, Double> value : indicator.getValues().entrySet()) {
			series.add(new Day(value.getKey().toDate()), value.getValue());
		}
		return series;
	}

	private TimeSeries createQuoteSeries() {
		TimeSeries series = new TimeSeries("Symbol TODO"); // TODO
		for (Quote quote : quotes) {
			LocalDate date = quote.getDate();
			double open = quote.getOpen();
			series.add(new Day(date.toDate()), open);
		}
		return series;
	}



}