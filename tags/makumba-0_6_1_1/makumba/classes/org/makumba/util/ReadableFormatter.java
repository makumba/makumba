package org.makumba.util;

import java.util.TimeZone;

/**
 * Formats argument in a readable (meaningful) way
 * 
 * @author Stefan Baebler
 * @version $Id$
 */

public class ReadableFormatter {

	/**
	 * Formats byte size in nice format - a wrapper method
	 * 
	 * @param byteSize
	 *            <code>String</code> representing the byte size
	 * @see #readableBytes(long)
	 * @return Formatted number of bytes (eg: empty, 15 B, 12kB, 821 kB, 3
	 *         MB...) <br>
	 *         "N/A" if <code>byteSize</code> is not a number <br>"" if
	 *         <code>byteSize</code> is null
	 */
	public static String readableBytes(String byteSize) {
		try {
			return readableBytes((new Long(byteSize.toString())).longValue());
		} catch (NumberFormatException nfe) {
			return "N/A";
		} catch (NullPointerException npe) {
			return "";
		}
	}

	/**
	 * Formats byte size in nice format
	 * 
	 * @param byteSize
	 *            the size in bytes to format
	 * @see #readableBytes(String)
	 * @return Formatted number of bytes (eg: empty, 15 B, 12kB, 821 kB, 3
	 *         MB...)
	 */
	public static String readableBytes(long byteSize) {
		if (byteSize < 0l)
			return ("invalid");
		if (byteSize < 1l)
			return ("empty");

		float byteSizeF = (new java.lang.Float(byteSize)).floatValue();
		String unit = "bytes";
		float factor = 1f;
		String[] desc = { "B", "kB", "MB", "GB", "TB" };

		java.text.DecimalFormat nf = new java.text.DecimalFormat();
		nf.setMaximumFractionDigits(1);
		nf.setGroupingUsed(true);

		String value = nf.format(byteSizeF);

		int i = 0;
		while (i + 1 < desc.length
				&& (value.length() > 4 || (value.length() > 3 && value
						.indexOf('.') < 0))) {
			i++;
			factor = factor * 1024l;
			value = nf.format(byteSizeF / factor);
		}
		if (value.charAt(0) == '0' && i > 0) { //go one back if a too-big scale
											   // is used
			value = nf.format(java.lang.Math.round(1024 * byteSizeF / factor));
			i--;
		}

		if (value.length() > 3 && value.indexOf('.') > 0) // sut decimals on
														  // large numbers
			value = value.substring(0, value.indexOf('.'));

		unit = desc[i];
		return (value + " " + unit);
	}

	/**
	 * Formats lentgh of time periods in a nice format
	 * 
	 * @param milis
	 *            a time difference in milli-seconds to format
	 * @return formatted time (eg: "1 second", "3 hours", "2 weeks" "41
	 *         years"...)
	 */
	public static String readableAge(long milis) {
		//simplest implementation:
		//return((new Long(secs)).toString())+" seconds";
		long secs = milis / 1000l;

		if (secs < 2l)
			return ("1 second");
		if (secs == 2l)
			return ("2 seconds");

		//default:
		long value = secs; //new Long(secs);
		String unit = "seconds";

		//now try to give it a meaning:
		Long s = new Long(secs);

		long[] breaks = { 31536000, 2628000, 604800, 86400, 3600, 60, 1 };
		String[] desc = { "year", "month", "week", "day", "hour", "minute",
				"second" };

		int i = 0;
		while (i <= breaks.length && secs <= (2 * breaks[i])) {
			i++;
		}
		//i=i-1;
		//long break=breaks[i];
		value = secs / breaks[i];
		unit = desc[i];
		if (value >= 2)
			unit = unit + "s";

		String retval = value + " " + unit;

		//if...

		return (retval);
	}

	/**
	 * prints date in nice format - only relevant parts Skips year if current,
	 * shows time if today...
	 * 
	 * @todo: Make it actually work :)
	 */
	public static String readableDate(java.util.Date date) {
		java.text.DateFormat df = java.text.DateFormat.getInstance();
		df.setTimeZone(TimeZone.getTimeZone("CET"));

		return (df.format(date));
	}

}
