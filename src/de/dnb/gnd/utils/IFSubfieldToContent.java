package de.dnb.gnd.utils;

import java.util.function.Function;

import de.dnb.gnd.parser.Subfield;

public class IFSubfieldToContent implements Function<Subfield, String> {
    @Override
    public String apply(final Subfield x) {
        return x.getContent();
    }

    public static final Function<Subfield, String> FUNCTION =
        new IFSubfieldToContent();
}
