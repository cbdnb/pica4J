/**
 * 
 */
package de.dnb.basics.utils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author baumann
 *
 */
public class DDC_Utils {

	private static final String T1 = "T1--";
	public static final String DDC_MAIN_PAT_S = "\\d\\d\\d(\\.\\d*[123456789])?";
	public static final String DDC_AUX_PAT_S = "T([12456]|3[ABC])\\-\\-(\\d)*[123456789]";

	public static final Pattern DDC_MAIN_PAT = Pattern.compile(DDC_MAIN_PAT_S);
	public static final Pattern DDC_AUX_PAT = Pattern.compile(DDC_AUX_PAT_S);

	public static final List<String> mainClasses = Arrays.asList("000", "100", "200", "300", "400", "500", "600", "700",
			"800", "900");

	public static List<String> getMainClasses() {
		return mainClasses;
	}

	/**
	 * 
	 * @param ddc auch null
	 * @return ist eine Haupttafelnotation (3 Ziffern, gefolgt optional von einem
	 *         Punkt + weitere Ziffern)
	 */
	public static boolean isMainTableDDC(final String ddc) {
		if (ddc == null)
			return false;
		final Matcher matcher = DDC_MAIN_PAT.matcher(ddc);
		return matcher.matches();

	}

	/**
	 * 
	 * @param ddc auch null
	 * @return ist eine Hilfstafelnotation (T1--0.. / T2--... / T3A--...)
	 */
	public static boolean isAuxTableDDC(final String ddc) {
		if (ddc == null)
			return false;
		final Matcher matcher = DDC_AUX_PAT.matcher(ddc);
		boolean isMatch = matcher.matches();
		if (!isMatch)
			return false;
		// T1-- genauer untersuchen:
		if (ddc.startsWith(T1)) {
			char firstCipher = ddc.charAt(T1.length());
			if (firstCipher != '0')
				return false;
		}
		return true;
	}

	/**
	 * 
	 * @param ddc auch null
	 * @return ist eine Haupt- oder Hilfstafelnotation (111.23 / T1--0.. / T2--... /
	 *         T3A--...)
	 */
	public static boolean isDDC(final String ddc) {
		return isAuxTableDDC(ddc) || isMainTableDDC(ddc);
	}

	/**
	 * 
	 * @param ddc auch null
	 * @return der zur Notation gehörige Zehner der Ersten Ebene
	 */
	public static String getDDCMainClass(final String ddc) {
		if (!isMainTableDDC(ddc))
			return null;
		char first = ddc.charAt(0);
		return first + "00";
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		System.out.println(isDDC("T1--020"));

	}

}
