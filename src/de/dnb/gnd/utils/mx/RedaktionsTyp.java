/**
 *
 */
package de.dnb.gnd.utils.mx;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.tries.TST;
import de.dnb.basics.tries.Trie;

/**
 * enum. Mögliche Typen sind:
 *
 * <li>DEFAULT("")
 * <li>FE("FE")
 * <li>FE_P("FE-P") - Personen
 * <li>FE_K("FE-K") - Körperschaften
 * <li>FE_VD_17("FE-VD-17")
 * <li>MUSIK("MUSIK")
 * <li>SE("SE")
 * <li>SE_P("SE-P") - Personen
 * <li>SE_K("SE-K") - Körperschaften
 * <li>SPRACH("SPRACH")
 * <li>GKD("GKD")
 * <li>SWD("SWD")
 * <li>DMA("DMA")
 * <li>DEA("DEA") <br>
 * <br>
 * Das ist auch die Sortierreihenfolge.
 *
 * @author baumann
 *
 */
public enum RedaktionsTyp {

	DEFAULT(""), FE("FE"), FE_P("FE-P"), FE_K("FE-K"), FE_VD_17("FE-VD-17"), MUSIK("MUSIK"), SE("SE"), SE_P("SE-P"),
	SE_K("SE-K"), SPRACH("SPRACH"), GKD("GKD"), SWD("SWD"), DMA("DMA"), DEA("DEA");

	/**
	 * Etwas wie "SE-P".
	 */
	public final String asText;

	private RedaktionsTyp(final String text) {
		asText = text;
	}

	@Override
	public String toString() {
		return asText;
	}

	private static final Map<String, RedaktionsTyp> text2Typ = new LinkedHashMap<>();

	private static final Trie<RedaktionsTyp> typTrie = new TST<>();

	static {
		for (final RedaktionsTyp typ : RedaktionsTyp.values()) {
			text2Typ.put(typ.asText, typ);
			typTrie.put(typ.asText + "-", typ);
		}
	}

	/**
	 *
	 * @param s nicht null, auch Kleinbuchstaben werden akzeptiert
	 * @return Typ oder null
	 */
	public static RedaktionsTyp getTyp(final String s) {
		Objects.requireNonNull(s);
		return text2Typ.get(s.toUpperCase());
	}

	/**
	 *
	 * @param s nicht null, auch Kleinbuchstaben werden akzeptiert
	 * @return Typ oder null
	 */
	public static RedaktionsTyp getTypeOfLongestPrefix(final String s) {
		Objects.requireNonNull(s);
		return typTrie.getValueOfLongestPrefix(s.toUpperCase() + "-");
	}

	/**
	 * Parst den Teil einer Mailboxadresse nach der ISIL. Der String muss aus dem
	 * Redaktionstyp allein oder aus Redaktionstyp + "-" + Rest bestehen. Das
	 * irrtümliche Fehlen des Rests wird akzeptiert. <br>
	 * Kann kein Typ erkannt werden, wird DEFAULT angenommen und alles in den Rest
	 * verschoben. <br>
	 *
	 *
	 * @param s nicht <code>null</code>, auch Kleinbuchstaben werden akzeptiert
	 * @return (typ, Rest), nicht null
	 */
	public static Pair<RedaktionsTyp, String> parse(String s) {
		Objects.requireNonNull(s);
		RedaktionsTyp typ = getTypeOfLongestPrefix(s);
		String rest;
		if (typ == null) {
			typ = DEFAULT;
			rest = s;
		} else {
			rest = s.substring(typ.asText.length());
			if (!rest.isEmpty())
				rest = rest.substring(1);
		}
		return new Pair<RedaktionsTyp, String>(typ, rest);
	}

	public static void main(final String... args) {
		System.out.println(parse("lö-FA"));
		System.out.println(getTyp(""));
		System.out.println(getTypeOfLongestPrefix("lö"));
	}

}
