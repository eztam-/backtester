package com.early_reflections.indicators;

import java.util.*;

import org.joda.time.LocalDate;

import com.early_reflections.yahoodata.Quote;

public class MovingAverage extends Indicator {

	private List<Double> values;
	private int period;

	public MovingAverage(int period) {
		values = new ArrayList();
		this.period = period;
	}

	@Override
	public String getId() {
		return "Moving Average " + period;
	}

	@Override
	public List<Double> getValues() {
		return values;
	}

	@Override
	protected void tradingDayTick(Quote quote) {
		if (getQuotes().size() < period) {
			values.add( null);
			return;
		}
		double sum = 0;
		for (int i = getQuotes().size() - period; i < getQuotes().size(); i++) {
			sum += getQuotes().get(i).getOpen();
		}

		values.add( sum / period);
	}
}
