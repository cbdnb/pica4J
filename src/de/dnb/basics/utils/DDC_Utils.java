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

    public static final String DDC_PAT_S = "\\d\\d\\d(\\.\\d+)?";

    public static final Pattern DDC_PAT = Pattern.compile(DDC_PAT_S);

    public static final List<String> mainClasses = Arrays.asList("000", "100",
        "200", "300", "400", "500", "600", "700", "800", "900");

    public static List<String> getMainClasses() {
        return mainClasses;
    }

    /**
     * 
     * @param ddc   auch null
     * @return      ist eine Haupttafelnotation (3 Ziffern, gefolgt
     *              optional von einem Punkt + weitere Ziffern)
     */
    public static boolean isMainTableDDC(final String ddc) {
        if (ddc == null)
            return false;
        final Matcher matcher = DDC_PAT.matcher(ddc);
        return matcher.matches();
    }

    /**
     * 
     * @param ddc   auch null
     * @return      der zur Notation gehörige Zehner der Ersten Ebene
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
    public static void main(String[] args) {
        System.out.println(getDDCMainClass("004"));

    }

}
