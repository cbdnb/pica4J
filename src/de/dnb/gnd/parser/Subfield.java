package de.dnb.gnd.parser;

import java.io.Serializable;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.Tag;

/**
 * Immutable, deshalb braucht man kein clone().
 *
 * @author Christian
 *
 */
public class Subfield implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2133053633841984938L;

	private final String content;

	private final Indicator indicator;

	/**
	 * Erzeugt ein Unterfeld. Der Inhalt des Unterfeldes wird getrimmt.
	 *
	 * @param anIndicator nicht null
	 * @param aContent    nicht null, kann auch leer sein
	 * @throws IllFormattedLineException Wenn Unterfeld null ist oder Indikator null
	 *                                   ist.
	 */
	public Subfield(final Indicator anIndicator, final String aContent) throws IllFormattedLineException {
		try {
			RangeCheckUtils.assertReferenceParamNotNull("anIndicator", anIndicator);
			RangeCheckUtils.assertReferenceParamNotNull("aContent", aContent);
		} catch (final IllegalArgumentException e) {
			throw new IllFormattedLineException("Übergebenes Unterfeld falsch: " + anIndicator + aContent);
		}
		// $$ wird im "k p"-Modus zur Eingabe eines einzelnen $ verwendet:
		String cont = aContent.replace("$$", "$");
		cont = cont.replace("\n", "");
		cont = cont.replace("\r", "");
		cont = StringUtils.unicodeComposition(cont);
		content = cont;
		indicator = anIndicator;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see parser.ISubfield#toString()
	 */
	@Override
	public final String toString() {
		return " |$" + indicator.indicatorChar + "|:" + content;
	}

	/**
	 * Erzeugt ein Unterfeld. Der Inhalt des Unterfeldes wird getrimmt.
	 *
	 * @param tag    nicht null.
	 * @param subStr in der Form "$<indikator><Inhalt>"
	 * @throws IllFormattedLineException Wenn substr oder Tag nicht korrekt sind.
	 *
	 */
	public Subfield(final Tag tag, final String subStr) throws IllFormattedLineException {
		this(tag.getIndicator(subStr.charAt(1)), subStr.substring(2));
	}

	/**
	 * Liefert den Inhalt des Unterfeldes.
	 *
	 * @return Inhalt.
	 */
	public final String getContent() {
		return content;
	}

	/**
	 * Liefert den Indikator des Unterfeldes.
	 *
	 * @return Indikator.
	 */
	public final Indicator getIndicator() {
		return indicator;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see parser.ISubfield#hashCode()
	 */
	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (content == null ? 0 : content.hashCode());
		result = prime * result + (indicator == null ? 0 : indicator.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see parser.ISubfield#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Subfield other = (Subfield) obj;
		if (content == null) {
			if (other.content != null) {
				return false;
			}
		} else if (!content.equals(other.content)) {
			return false;
		}
		if (indicator == null) {
			if (other.indicator != null) {
				return false;
			}
		} else if (!indicator.equals(other.indicator)) {
			return false;
		}
		return true;
	}

	/**
	 * @param args
	 * @throws IllFormattedLineException
	 */
	public static void main(final String[] args) throws IllFormattedLineException {
		final Tag tag = BibTagDB.getDB().findTag("4700");
		System.out.println(tag);
		final Subfield sub = new Subfield(tag, "$S");
		System.out.println(sub);

	}

}
