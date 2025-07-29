package de.dnb.gnd.parser.line;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.tag.BibliographicTag;
import de.dnb.gnd.parser.tag.Tag;

public class BibliographicLine extends Line implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1751423685030079014L;

	public BibliographicLine(Tag aTag) {
		super(aTag);
		subfields = new LinkedList<Subfield>();
	}

	protected BibliographicLine(
		final BibliographicTag aTag,
		final Collection<Subfield> subfieldColl) {
		this(aTag);
		RangeCheckUtils.assertCollectionParamNotNullOrEmpty("subfieldColl",
			subfieldColl);
		for (Subfield subfield : subfieldColl) {
			add(subfield);
		}
	}

	@Override
	public Line add(Line otherLine) throws OperationNotSupportedException {
		throw new OperationNotSupportedException(
			"Kann einer Zeile eines bibliographischen Datensatzes"
				+ " keine andere hinzufügen");
	}

}
