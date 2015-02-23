package com.early_reflections.indicators;

import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;

import com.early_reflections.json.Quote;

public class MovingAverage extends Indicator {

	private SortedMap<LocalDate, Double> values;
	private int period;

	public MovingAverage(int period) {
		values = new TreeMap<>();
		this.period = period;
	}

	@Override
	public String getName() {
		return "Moving Average " + period;
	}

	@Override
	public SortedMap<LocalDate, Double> getValues() {
		return values;
	}

	@Override
	protected void tradingDayTick(Quote quote) {
		if (getQuotes().size() < period)
			return;
		double sum = 0;
		for (int i = getQuotes().size() - period; i < getQuotes().size(); i++) {
			sum += getQuotes().get(i).getOpen();
		}

		values.put(quote.getDate(), sum / period);
	}
}
