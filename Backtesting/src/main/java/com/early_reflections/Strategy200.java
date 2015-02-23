package com.early_reflections;

import com.early_reflections.indicators.MovingAverage;
import com.early_reflections.json.Quote;

/**
 * This strategy places a long order if the price moves from a lower value to
 * above the moving average of the last 200 days. If the price falls down to a
 * value lower than the moving average then all open positions will be closed.
 * 
 * @author Matthias Birschl
 *
 */
public class Strategy200 extends Strategy {

	private double deposit = 1000;

	private MovingAverage movingAverage;

	public Strategy200(MovingAverage movingAverage200) {
		this.movingAverage = movingAverage200;
	}

	@Override
	public double getPerformancePercent() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void tradingDayTick(Quote quote) {

		System.out.println("Good morning, today is " + quote.getDate() + " have a successfull trading day! ");

		// TODO
	}
}
