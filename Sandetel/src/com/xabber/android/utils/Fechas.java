package com.xabber.android.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Clase herramienta, tranforma String en Date y viceversa
 * 
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class Fechas {

	/**
	 * 
	 * @param fecha
	 * @param format
	 * @return
	 */
	public static Date unformatFecha(String fecha, String format) {

		Date result = null;
		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat(format,
				locale);

		try {
			result = dateFormatCompleto.parse(fecha);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 
	 * @param fecha
	 * @param format
	 * @return
	 */
	public static String formatFecha(Date fecha, String format) {

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat(format,
				locale);

		return dateFormatCompleto.format(fecha);
	}

	/**
	 * 
	 * @param fecha
	 * @return
	 */
	public static String formatFecha_yyyy_mm_dd_hh_mm_ss_sqlite(Date fecha) {

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", locale);

		return dateFormatCompleto.format(fecha);
	}

	/**
	 * 
	 * @param fecha
	 * @param separator
	 * @return
	 */
	public static String formatFecha_yyyy_mm_dd(Date fecha, String separator) {

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat("yyyy"
				+ separator + "MM" + separator + "dd", locale);

		return dateFormatCompleto.format(fecha);
	}

	/**
	 * 
	 * @param fecha
	 * @param separator
	 * @return
	 */
	public static String formatFecha_dd_mm_yyyy(Date fecha, String separator) {

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat("dd"
				+ separator + "MM" + separator + "yyyy", locale);

		return dateFormatCompleto.format(fecha);
	}

	/**
	 * 
	 * @param separator
	 * @return
	 */
	public static String formatFecha_dd_mm_yyyy_Now(String separator) {

		Date fecha = Calendar.getInstance().getTime();

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat("dd"
				+ separator + "MM" + separator + "yyyy", locale);

		return dateFormatCompleto.format(fecha);
	}

	/**
	 * 
	 * @param fecha
	 * @return
	 */
	public static String formatFecha_dd(Date fecha) {

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat("dd", locale);

		return dateFormatCompleto.format(fecha);
	}

	/**
	 * 
	 * @param fecha
	 * @return
	 */
	public static String formatFecha_yyyy(Date fecha) {

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat("yyyy",
				locale);

		return dateFormatCompleto.format(fecha);
	}

	/**
	 * 
	 * @param fecha
	 * @return
	 */
	public static String formatFecha_MM(Date fecha) {

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat("MMMM",
				locale);

		return dateFormatCompleto.format(fecha);
	}

	/**
	 * 
	 * @param fecha
	 * @param separator
	 * @return
	 */
	public static String formatFecha_dd_de_mm(Date fecha, String separator) {

		return formatFecha_dd(fecha) + separator + formatFecha_MM(fecha);
	}

	/**
	 * 
	 * @param fecha
	 * @param separator
	 * @return
	 */
	public static String formatFecha_dd_de_MM_de_yyyy(Date fecha,
			String separator) {

		return formatFecha_dd(fecha) + separator + formatFecha_MM(fecha)
				+ separator + formatFecha_yyyy(fecha);
	}

	/**
	 * 
	 * @param fecha
	 * @param separator
	 * @return
	 */
	public static Date unFormatFecha_yyyy_mm_dd(String fecha, String separator) {

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat("yyyy"
				+ separator + "MM" + separator + "dd", locale);

		try {
			return dateFormatCompleto.parse(fecha);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param fecha
	 * @param separator
	 * @return
	 */
	public static Date unFormatFecha_dd_mm_yyyy(String fecha, String separator) {

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat("dd"
				+ separator + "MM" + separator + "yyyy", locale);

		try {
			return dateFormatCompleto.parse(fecha);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param fecha
	 * @param separator
	 * @return
	 */
	public static Date unFormatFecha_dd_mm_yyyy_hh_mm(String fecha,
			String separator) {

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat("dd"
				+ separator + "MM" + separator + "yyyy" + " '-' " + "HH:mm",
				locale);

		try {
			return dateFormatCompleto.parse(fecha);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param fecha
	 * @return
	 */
	public static Date unFormatFecha_long(long fecha) {

		Date date = new Date(fecha);

		return date;
	}

	/**
	 * 
	 * @param tiempo
	 * @return
	 */
	public static String formatTime(Date tiempo) {

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat("HH:mm:ss",
				locale);

		try {
			return dateFormatCompleto.format(tiempo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param tiempo
	 * @return
	 */
	public static String formatTimeNoSeconds(Date tiempo) {

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat("HH:mm",
				locale);

		try {
			return dateFormatCompleto.format(tiempo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param tiempo
	 * @return
	 */
	public static String formatTime_HH_mm(Date tiempo) {

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat("HH:mm",
				locale);

		try {
			return dateFormatCompleto.format(tiempo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param tiempo
	 * @return
	 */
	public static String formatTime_HH_mm_H(Date tiempo) {

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat("HH:mm'H'",
				locale);

		try {
			return dateFormatCompleto.format(tiempo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param fecha
	 * @return
	 */
	public static Date unFormatTime(String fecha) {

		Locale locale = Locale.getDefault();

		SimpleDateFormat dateFormatCompleto = new SimpleDateFormat("HH:mm:ss",
				locale);

		try {
			return dateFormatCompleto.parse(fecha);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static int compareFecha(String o1, String o2) {
		Date dateO1 = new Date(o1);
		Date dateO2 = new Date(o2);
		if (dateO1.before(dateO2)) {
			return -1;
		} else if (dateO1.after(dateO2)) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static Boolean equalsDates(Date date1, Date date2) {
		if (date1.equals(date2)) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int calculateDifference(Date a, Date b) {
		int tempDifference = 0;
		int difference = 0;
		Calendar earlier = Calendar.getInstance();
		Calendar later = Calendar.getInstance();

		if (a.compareTo(b) < 0) {
			earlier.setTime(a);
			later.setTime(b);
		} else {
			earlier.setTime(b);
			later.setTime(a);
		}

		while (earlier.get(Calendar.YEAR) != later.get(Calendar.YEAR)) {
			tempDifference = 365 * (later.get(Calendar.YEAR) - earlier
					.get(Calendar.YEAR));
			difference += tempDifference;

			earlier.add(Calendar.DAY_OF_YEAR, tempDifference);
		}

		if (earlier.get(Calendar.DAY_OF_YEAR) != later
				.get(Calendar.DAY_OF_YEAR)) {
			tempDifference = later.get(Calendar.DAY_OF_YEAR)
					- earlier.get(Calendar.DAY_OF_YEAR);
			difference += tempDifference;

			earlier.add(Calendar.DAY_OF_YEAR, tempDifference);
		}

		return difference;
	}

	/**
	 * 
	 * @param dateTxt
	 * @param time
	 * @return
	 */
	// Calcular si entre una fecha y otra ha pasado mas tiempo del indicado
	public static Boolean passTime(String dateTxt, long time) {
		long date = Long.parseLong(dateTxt);
		long currentDay = Calendar.getInstance().getTimeInMillis();
		if (currentDay - date > time) {
			return true;
		}

		return false;
	}

	// public static List<Mes> getMonths(Activity activity)
	// {
	// String[] listaMeses=
	// {activity.getString(R.string.mes_1),activity.getString(R.string.mes_2),activity.getString(R.string.mes_3),activity.getString(R.string.mes_4),activity.getString(R.string.mes_5),activity.getString(R.string.mes_6),activity.getString(R.string.mes_7),activity.getString(R.string.mes_8),activity.getString(R.string.mes_9),activity.getString(R.string.mes_10),activity.getString(R.string.mes_11),activity.getString(R.string.mes_12)};
	// List<Mes> meses = new ArrayList<Mes>();
	// Mes mes = null;
	// for (int i = 0; i < listaMeses.length; i++) {
	// mes = new Mes();
	// mes.setId(i);
	// mes.setNombre(listaMeses[i]);
	// meses.add(mes);
	// }
	//
	// return meses;
	// }

}
