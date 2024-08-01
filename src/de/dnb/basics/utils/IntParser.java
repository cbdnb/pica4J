/**
 * 
 */
package de.dnb.basics.utils;

import java.util.function.Function;

/**
 * Parst einen String und gibt den Default-Wert, wenn nicht
 * geparst werden kann.
 * 
 * @author baumann
 *
 */
public class IntParser implements Function<String, Integer> {

    /**
     * Default-Wert.
     */
    private final Integer def;

    /**
     * @param def   Default-Wert
     */
    public IntParser(final Integer def) {
        this.def = def;
    }

    /**
     * Der Default-Wert ist 0.
     */
    public IntParser() {
        this.def = 0;
    }

    /* (non-Javadoc)
     * @see java.util.function.Function#apply(java.lang.Object)
     */
    @Override
    public Integer apply(String t) {
        try {
            int i = Integer.parseInt(t);
            return i;
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        IntParser intParser = new IntParser(21);
        System.out.println(intParser.apply(null));

    }

}
