package de.dnb.gnd.parser.tag;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Repeatability;
import de.dnb.gnd.parser.line.BibLineFactory;

public class BibliographicTag extends Tag implements Serializable{

  /**
	 * 
	 */
	private static final long serialVersionUID = -1937195096403029965L;

  //@formatter:off
	/**
	 *
	 * @param pica3
	 * @param picaPlus
	 * @param german
	 * @param repeatability
	 * @param marc
	 * @param english
	 */
	BibliographicTag(final String pica3,
			final String picaPlus,
			final String german,
			final Repeatability repeatability,
			final String marc,
			final String english) {
		super(pica3, picaPlus, german, repeatability, marc, english);
	}


	/**
	 *
	 * @param pica3
	 * @param picaPlus
	 * @param german
	 * @param repeatability
	 * @param marc
	 * @param english
	 */
	BibliographicTag(final String pica3,
			final String picaPlus,
			final String german,
			final Repeatability repeatability,
			final String marc,
			final char marcIndicator1,
			final char marcIindicator2,
			final String english) {
		super(pica3, picaPlus, german, repeatability, marc, marcIndicator1,
			marcIindicator2,english);
	}

	//@formatter:on

  /**
   * Grundlegende Datenstruktur.
   */
  protected Map<Character, Indicator> alternativeIndicatorMap = new LinkedHashMap<>();

  @SuppressWarnings("boxing")
  final void addAlternative(final Indicator indicator) {
    RangeCheckUtils.assertReferenceParamNotNull("indicator", indicator);
    alternativeIndicatorMap.put(indicator.indicatorChar, indicator);
  }

  @Override
  public Set<Indicator> getOwnIndicators() {
    if (ownIndicators == null) {
      ownIndicators = new LinkedHashSet<Indicator>(get1stIndicators());
      ownIndicators.addAll(get2ndIndicators());
    }
    return Collections.unmodifiableSet(ownIndicators);
  }

  /**
   *
   * @return nicht null, nicht leer.
   */
  public final Map<Character, Indicator> get1stIndicatorMap() {
    final Map<Character, Indicator> map = new LinkedHashMap<>(indicatorMap);
    for (final Tag tag : getInherited()) {
      map.putAll(((BibliographicTag) tag).get1stIndicatorMap());
    }
    return map;
  }

  /**
   *
   * @return nicht null, nicht leer.
   */
  public final Map<Character, Indicator> get2ndIndicatorMap() {
    final Map<Character, Indicator> map = new LinkedHashMap<>(alternativeIndicatorMap);
    for (final Tag tag : getInherited()) {
      map.putAll(((BibliographicTag) tag).get2ndIndicatorMap());
    }
    return map;
  }

  /**
   * Gibt eine Map aller Indikatoren.
   * @return	nicht null, nicht leer.
   */
  public final Map<Character, Indicator> getIndicatorMap() {
    final Map<Character, Indicator> map = get1stIndicatorMap();
    map.putAll(get2ndIndicatorMap());
    return map;
  }

  @Override
  public Map<Character, Indicator> getIndicatorMap(final boolean optionalsIncluded) {
    return getIndicatorMap();
  }

  /**
   * Liefert eine beliebig manipulierbare Menge.
   *
   * @return nicht null, nicht leer.
   */
  public Set<Indicator> get1stIndicators() {
    final Map<Character, Indicator> map = get1stIndicatorMap();
    return new LinkedHashSet<>(map.values());
  }

  /**
   * Liefert eine beliebig manipulierbare Menge.
   * @return nicht null, evenuell leer.
   */
  public Set<Indicator> get2ndIndicators() {
    final Map<Character, Indicator> map = get2ndIndicatorMap();
    return new LinkedHashSet<>(map.values());
  }

  @Override
  public BibLineFactory getLineFactory() {
    return new BibLineFactory(this);
  }

  @Override
  public Indicator getIndicator(final char ind) {
    return getIndicatorMap().get(ind);
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    // TODO Auto-generated method stub

  }

}
