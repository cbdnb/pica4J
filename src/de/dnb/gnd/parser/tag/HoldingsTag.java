package de.dnb.gnd.parser.tag;

import de.dnb.gnd.parser.Repeatability;
import de.dnb.gnd.parser.line.BibLineFactory;
import de.dnb.gnd.parser.line.HoldingsFactory;

/**
 * Für Bestandsdaten.
 * 
 * @author baumann
 *
 */
public class HoldingsTag extends BibliographicTag {

	private String prefix;

	/**
	 * 
	 * @param pica3
	 * @param picaPlus
	 * @param german
	 * @param repeatability
	 * @param marc
	 * @param english
	 * @param pica3prefix		pica3-Präfix, an das der Inhalt des Unterfeldes
	 * 							$x angehängt werden muss
	 */
	HoldingsTag(
		String pica3,
		String picaPlus,
		String german,
		Repeatability repeatability,
		String marc,
		String english,
		String pica3prefix) {
		super(pica3, picaPlus, german, repeatability, marc, english);
		this.prefix = pica3prefix;
	}

	@Override
	public BibLineFactory getLineFactory() {
		return new HoldingsFactory(this);
	}

	public String getPrefix() {
		return prefix;
	}

}
