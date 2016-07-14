package com.xabber.android.utils.rounded;

public class RoundedUtil {

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public static String roundInteger(int value, int places) {
		String result = String.valueOf(value);
		while (result.length() < places) {
			result = "0" + result;

		}

		return result;
	}
}
