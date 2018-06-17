package com.cwsni.world.client.desktop.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DataFormatter {

	public static String formatIntNumber(Integer v) {
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

	public static double doubleWith3points(double d) {
		return ((int) (d * 1000)) / 1000.0;
	}

}
