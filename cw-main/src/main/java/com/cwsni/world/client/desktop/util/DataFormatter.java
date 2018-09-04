package com.cwsni.world.client.desktop.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DataFormatter {

	public static String formatLongNumber(Long v) {
		if (v > 10000000000L) {
			return internalFormatLongNumber(v / 1000000000) + " g";
		} else if (v > 10000000) {
			return internalFormatLongNumber(v / 1000000) + " m";
		} else if (v > 10000) {
			return internalFormatLongNumber(v / 1000) + " k";
		}
		return internalFormatLongNumber(v);
	}

	public static String formatLongNumber(Integer v) {
		return formatLongNumber(new Long(v));
	}

	private static String internalFormatLongNumber(Long v) {
		Locale currentLocale = new Locale("en", "US");
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(currentLocale);
		DecimalFormat df = new DecimalFormat("#,###,###,##0", dfs);
		return df.format(v);
	}

	public static String formatFractionNumber(Double v) {
		Locale currentLocale = new Locale("en", "US");
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(currentLocale);
		DecimalFormat df = new DecimalFormat("#,###,###,##0.##", dfs);
		return df.format(v);
	}

	public static double doubleWith2points(double d) {
		return Math.round(d * 100) / 100.0;
	}

	public static double doubleWith3points(double d) {
		return Math.round(d * 1000) / 1000.0;
	}

	public static double doubleWith4points(double d) {
		return Math.round(d * 10000) / 10000.0;
	}
	
	public static double doubleWithPrecison(double d, int precision) {
		BigDecimal bd = new BigDecimal(d);
		bd = bd.setScale(precision, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static String toString(Object o) {
		return String.valueOf(o);
	}

	public static String toLong(long v) {
		return DataFormatter.formatLongNumber(v);
	}

	public static String toFraction(double v) {
		return DataFormatter.formatFractionNumber(v);
	}

}
