package de.dnb.gnd.utils.mx;

import java.util.HashMap;
import java.util.Map;

/**
 * Eine Bibliothek. Sie hat die Felder:
 * <li>{@link #nameKurz}, DNB für die DNB
 * <li>{@link #nameLang}, "Deutsche Nationalbibliothek" für die DNB
 * <li>{@link #isil}, "DE-101" für die DNB
 * <li>{@link #urheberkennung} ???, etwa 1923 für die DNB <br>
 * <br>
 * Sie kann mehrere {@link RedaktionsTyp}en bearbeiten, für jeden dieser Typen
 * kann eine andere Verbundzentrale zuständig sein.
 *
 *
 * @author baumann
 *
 */
public class Library implements Comparable<Library> {

	public static final Library NULL_LIBRARY;

	public final static Library SPIO;

	public final static Library PSEU;

	static {
		NULL_LIBRARY = new Library("9999", "null", "9999", "la");
		SPIO = new Library("XXXX", "spio", "spio", "Spitzen, Exekutiv- und Informationsorgane");
		SPIO.addRedaktion(RedaktionsTyp.DEFAULT, "spio");
		PSEU = new Library("XXXX", "pseu", "pseu", "Aufspaltung von Pseudonymen");
		PSEU.addRedaktion(RedaktionsTyp.DEFAULT, "pseu");
		
	}

	public static void main(final String... strings) {
		System.out.println(SPIO.toStringLang());
		System.out.println(PSEU.toStringLang());
		System.out.println(NULL_LIBRARY);
		Library bibliothek;
		bibliothek = new Library("XXX", "Stadtarchiv Attnang-Puchheim", "AT-41703AR", "Stadtarchiv Attnang-Puchheim");
		System.out.println(bibliothek);

	}

	/**
	 *
	 * @return Library mit von null verschiedenen Feldern
	 */
	public static Library getNullLibrary() {
		return NULL_LIBRARY;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((isil == null) ? 0 : isil.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Library other = (Library) obj;
		if (isil == null) {
			if (other.isil != null)
				return false;
		} else if (!isil.equals(other.isil))
			return false;
		return true;
	}

	public final String urheberkennung;

	public final String nameKurz;

	public final String nameLang;

	public final String isil;

	public String getKurzPlusIsil() {
		return "" + nameKurz + " (" + isil + ")";
	}

	/**
	 *
	 * @param urheberkennung beliebig
	 * @param nameKurz       beliebig
	 * @param isil           nicht null
	 * @param nameLang       beliebig
	 */
	public Library(final String urheberkennung, final String nameKurz, final String isil, final String nameLang) {
		if (isil == null)
			throw new IllegalArgumentException("ISIL ist null");
		this.isil = isil;
		this.urheberkennung = urheberkennung;
		this.nameKurz = nameKurz;
		this.nameLang = nameLang;
	}

	public String serialize() {
		String s = "bibliothek = new Library(\"" + urheberkennung + "\", \"" + nameKurz.replace("\"", "\\\"") + "\", \""
				+ isil + "\", \"" + nameLang.replace("\"", "\\\"") + "\");";

		for (final Map.Entry<RedaktionsTyp, String> el : redaktion2verbundISIL.entrySet()) {
			final RedaktionsTyp typ = el.getKey();
			s += "\nbibliothek.addRedaktion(RedaktionsTyp." + typ.name() + ", \"" + el.getValue() + "\");";
		}
		return s;
	}

	private final Map<RedaktionsTyp, String> redaktion2verbundISIL = new HashMap<>();

	/**
	 *
	 * @param typ     auch null
	 * @param isilRed Korrekte ISIL
	 */
	void addRedaktion(final RedaktionsTyp typ, final String isilRed) {
		final String oldIsil = redaktion2verbundISIL.put(typ, isilRed);
		if (oldIsil != null)
			throw new IllegalArgumentException(isil + ": " + typ + " war schon mal da");
	}

	public String toStringLang() {
		return "Library [" + "Urheberkennung=" + urheberkennung + ", " + "Name kurz=" + nameKurz + ", " + "Name lang="
				+ nameLang + ", " + "ISIL=" + isil + ", " + "Redaktionen und Verbünde=" + redaktion2verbundISIL + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return nameKurz + " (" + isil + ")";
	}

	/**
	 *
	 * @param typ auch null
	 * @return zugehörige Verbundredaktion (eventuell DEFAULT). Wenn keine
	 *         vorhanden, wird angenommen, dass die Bibliothek selbst Redaktion ist.
	 */
	public String getVerbundISIL(final RedaktionsTyp typ) {
		String verbund = redaktion2verbundISIL.get(typ);
		if (verbund == null) {
			verbund = redaktion2verbundISIL.get(RedaktionsTyp.DEFAULT);
			if (verbund == null)
				verbund = isil;
		}
		return verbund;
	}

	/**
	 *
	 * @param typ nicht null, auch Kleinbuchstaben möglich
	 * @return zugehörige Verbundredaktion (eventuell DEFAULT). Wenn keine
	 *         vorhanden, wird angenommen, dass die Bibliothek selbst Redaktion ist.
	 */
	public String getVerbundISIL(final String typS) {
		final RedaktionsTyp redaktionsTyp = RedaktionsTyp.getTyp(typS);
		return getVerbundISIL(redaktionsTyp);
	}

	/**
	 * Vergleicht nach ISIL. Im Falle von null wird ans Ende gestellt.
	 */
	@Override
	public int compareTo(final Library o) {
		final String thisIsil = isil == null ? "zzz" : isil;
		final String otherIsil = o.isil == null ? "zzz" : o.isil;
		return thisIsil.compareTo(otherIsil);
	}

}
