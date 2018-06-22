package com.github.andy2003.smareader.inverter;

import java.util.Date;

public class TagValue<T> {
	private LriDef lriDef;
	private long timestampInMillis;
	private T value;

	public TagValue(LriDef lriDef, Number value, long timestampInMillis) {
		this.lriDef = lriDef;
		this.value = lriDef.convert(value);
		this.timestampInMillis = timestampInMillis;
	}

	@Override
	public String toString() {
		String s = String.valueOf(value);
		if (lriDef.getUnit() != null) {
			s += " " + lriDef.getUnit();
		}
		if (lriDef.getDescription() != null) {
			s += " [" + lriDef.getDescription() + "]";
		}
		s += " (Time: " + new Date(timestampInMillis) + ")";
		return s;
	}

	public LriDef getLriDef() {
		return lriDef;
	}

	public long getTimestampInMillis() {
		return timestampInMillis;
	}

	public T getValue() {
		return value;
	}
}
