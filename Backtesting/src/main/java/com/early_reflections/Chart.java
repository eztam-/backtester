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

public class Chart extends ApplicationFrame {

	private static final long serialVersionUID = 1L;

	private List<Quote> quotes;

	private List<Indicator> indicators;

	public Chart(final String title, List<Quote> quotes, List<Indicator> indicators) {
		super(title);
		this.quotes = quotes;
		this.indicators = indicators;
		final XYDataset dataset = createDataset();
		final JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(1000, 700));
		chartPanel.setMouseZoomable(true, false);
		setContentPane(chartPanel);
	}

	private XYDataset createDataset() {
		final TimeSeries eur = createQuoteSeries();
		// final TimeSeries mav = MovingAverage.createMovingAverage(eur,
		// "200 day moving average", 200, 200);
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(eur);
		// dataset.addSeries(mav);
		addIndicatorSeries(dataset);
		return dataset;
	}

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

	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart chart = ChartFactory.createTimeSeriesChart("Time Series", "Date", "Value", dataset, true,
				true, false);
		final XYItemRenderer renderer = chart.getXYPlot().getRenderer();

		final StandardXYToolTipGenerator g = new StandardXYToolTipGenerator(
				StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, new SimpleDateFormat("d-MMM-yyyy"),
				new DecimalFormat("0.00"));
		renderer.setBaseToolTipGenerator(g);
		return chart;
	}

}