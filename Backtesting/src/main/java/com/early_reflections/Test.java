package com.early_reflections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.ShapeUtilities;

/**
 * A time series chart with a custom renderer that varies the shapes and fill
 * colors by item.
 */
public class Test extends ApplicationFrame {

	public static class Renderer extends XYLineAndShapeRenderer {
		private Shape[] shapes;
		private Paint[] fillPaints;

		public Renderer() {
			setSeriesPaint(0, Color.blue);
			setSeriesShapesFilled(0, true);
			setSeriesFillPaint(0, Color.white);

			setSeriesOutlinePaint(0, Color.black);
			setUseOutlinePaint(true);
			setUseFillPaint(true);
			this.fillPaints = new Paint[] { Color.red, Color.green, Color.yellow };
			this.shapes = new Shape[3];
			this.shapes[0] = ShapeUtilities.createDiamond(6.0f);
			this.shapes[1] = ShapeUtilities.createDiagonalCross(5.0f, 1.3f);
			this.shapes[2] = ShapeUtilities.createDownTriangle(5.0f);
		}

		public Shape getItemShape(int row, int column) {
			// we use a simple rule here, but use any lookup function you
			// want to return an appropriate shape
			return this.shapes[column % 3];
		}

		public Paint getItemFillPaint(int row, int column) {
			return this.fillPaints[column % 3];
		}
	};

	/**
	 * A demonstration application showing how to create a simple time series
	 * chart. This example uses monthly data.
	 *
	 * @param title
	 *            the frame title.
	 */
	public Test(String title) {
		super(title);
		JPanel chartPanel = createDemoPanel();
		setContentPane(chartPanel);
	}

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *            a dataset.
	 * 
	 * @return A chart.
	 */
	private static JFreeChart createChart(XYDataset dataset) {

		JFreeChart chart = ChartFactory.createTimeSeriesChart("A Sample Chart", // title
				"Date", // x-axis label
				"Percentage", // y-axis label
				dataset, // data
				true, // create legend?
				true, // generate tooltips?
				false // generate URLs?
				);

		chart.setBackgroundPaint(Color.white);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);

		Renderer renderer = new Renderer();
		renderer.setBaseShapesVisible(true);
		renderer.setBaseShapesFilled(true);
		LegendItemCollection items = new LegendItemCollection();
		items.add(new LegendItem("Point Type 0", "", null, null, ShapeUtilities.createDiamond(5.0f), Color.red,
				new BasicStroke(1.0f), Color.black));
		items.add(new LegendItem("Point Type 1", "", null, null, ShapeUtilities.createDiagonalCross(4.0f, 1.0f),
				Color.green, new BasicStroke(1.0f), Color.black));
		items.add(new LegendItem("Point Type 2", "", null, null, ShapeUtilities.createDownTriangle(4.0f), Color.yellow,
				new BasicStroke(1.0f), Color.black));
		plot.setFixedLegendItems(items);
		plot.setRenderer(renderer);

		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setTickUnit(new DateTickUnit(DateTickUnit.YEAR, 1));
		axis.setDateFormatOverride(new SimpleDateFormat("yyyy"));

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setLabel("Percentage");
		rangeAxis.setNumberFormatOverride(NumberFormat.getPercentInstance());

		return chart;

	}

	/**
	 * Creates a dataset, consisting of two series of monthly data.
	 *
	 * @return The dataset.
	 */
	private static XYDataset createDataset() {

		TimeSeries s1 = new TimeSeries("Series 1", Month.class);
		s1.add(new Month(6, 1999), 0.0475);
		s1.add(new Month(1, 2000), 0.0475);
		s1.add(new Month(2, 2000), 0.0675);
		s1.add(new Month(4, 2000), 0.0225);
		s1.add(new Month(5, 2000), 0.0475);
		s1.add(new Month(6, 2000), 0.04);
		s1.add(new Month(2, 2001), 0.04);
		s1.add(new Month(3, 2001), 0.05);
		s1.add(new Month(4, 2001), 0.045);
		s1.add(new Month(2, 2002), 0.045);
		s1.add(new Month(4, 2002), 0.07);
		s1.add(new Month(5, 2002), 0.045);
		s1.add(new Month(6, 2002), 0.04);
		s1.add(new Month(8, 2002), 0.02);
		s1.add(new Month(12, 2002), 0.03);
		s1.add(new Month(1, 2003), 0.025);
		s1.add(new Month(11, 2003), 0.025);
		s1.add(new Month(12, 2003), 0.03);

		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(s1);

		dataset.setDomainIsPointsInTime(true);

		return dataset;

	}

	/**
	 * Creates a panel for the demo (used by SuperDemo.java).
	 * 
	 * @return A panel.
	 */
	public static JPanel createDemoPanel() {
		JFreeChart chart = createChart(createDataset());
		return new ChartPanel(chart);
	}

	/**
	 * Starting point for the demonstration application.
	 *
	 * @param args
	 *            ignored.
	 */
	public static void main(String[] args) {

		Test demo = new Test("Time Series Chart Demo 15");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}