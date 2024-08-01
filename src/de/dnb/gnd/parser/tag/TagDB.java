package de.dnb.gnd.parser.tag;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.dnb.basics.applicationComponents.tuples.Triplett;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Pica3Comparator;
import de.dnb.gnd.parser.Repeatability;

public abstract class TagDB {

  public static final Repeatability R = Repeatability.REPEATABLE;
  public static final Repeatability NR = Repeatability.NON_REPEATABLE;
  public static final Repeatability U = Repeatability.UNKNOWN;
  protected HashMap<String, Tag> database = new HashMap<>();
  protected TreeMap<String, Tag> pica3Map = new TreeMap<>(new Pica3Comparator());
  protected TreeMap<String, Tag> picaPlusMap = new TreeMap<>();
  protected TreeMap<String, Tag> marcMap = new TreeMap<>();
  protected Collection<Tag> unmodifiables = null;
  /**
   *
   */
  public static final Indicator DOLLAR_2 = new Indicator("$2", "", '2', "Quelle des Terms", NR, "");

  /**
   * URI, wiederholbar.
   */
  public static final Indicator DOLLAR_U_SM_R =
    new Indicator("$u", "", 'u', "Uniform Resource Identifier", R, "");

  /**
   * Einziger Indikator für $8 (Expansion eines gefundenen Datensatzes).
   */
  public static final Indicator DOLLAR_8 =
    new Indicator("", "", '8', "Expansion eines gefundenen Datensatzes", NR, "");

  /**
   * Einziger Indikator für $9 (Verknüpfungsnummer).
   */
  public static final Indicator DOLLAR_9 =
    new Indicator("!", "!", '9', "Verknüpfungsnummer", NR, "");

  /**
   * Mindestens 3 Nicht-Whitespace gefolgt von einem Blank.
   */
  public static final Pattern TAG_PATTERN = Pattern.compile("^\\S{3,} ");

  protected TagDB() {
    super();
  }

  /**
   * Findet einen Tag zu einer Pica- oder Pica+ Feldkennzeichnung.
   *
   * @param tag 	Pica3- oder Pica+-Feldkennzeichnung nicht null, nicht leer.
   * 				Kann Leerzeichen vorne oder hinten enthalten.
   * @return		Gültiger Tag oder null.
   */
  public final Tag findTag(final String tag) {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("tag", tag);
    final String tagStr = tag.trim();
    return database.get(tagStr);
  }

  /**
   * Findet einen Tag zu einem Muster einer Pica- oder Pica+ Feldkennzeichnung.
   *
   * @param regExp   Muster einer Pica3- oder Pica+-Feldkennzeichnung nicht null,
   *                 nicht leer. Kann Leerzeichen vorne oder hinten enthalten.
   * @return    Tags, eventuell leer.
   */
  public final Set<Tag> findTagPattern(final String regExp) {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("tag", regExp);
    final Pattern searchPattern = Pattern.compile(regExp.trim());
    final Predicate<String> myPred = s -> searchPattern.matcher(s).matches();
    final Set<Tag> tags = new LinkedHashSet<>();
    final Set<String> keys = new TreeSet<>(database.keySet());
    keys.forEach(key ->
    {
      if (myPred.test(key))
        tags.add(database.get(key));
    });
    return tags;
  }

  /**
   * Findet einen Tag zu einer Pica- oder Pica+ Feldkennzeichnung.
   *
   * @param tagStr 	MARC-Feldkennzeichnung nicht null, nicht leer.
   * 					Kann Leerzeichen vorne oder hinten enthalten.
   * @param indicator1 marc-Indikator 1
   * @param indicator2 marc-Indikator 1
   * @return			Gültiger Tag oder null.
   */
  public final Tag findMARCTag(String tagStr, final char indicator1, final char indicator2) {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("tag", tagStr);
    tagStr = tagStr.trim();
    final Tag tag = marcMap.get(tagStr);
    if (tag != null)
      return tag;
    tagStr = (tagStr + indicator1) + indicator2;
    return marcMap.get(tagStr);
  }

  /**
   * Enthalten?
   *
   * @param tag	beliebig
   * @return		true, wenn enthalten.
   */
  public final boolean contains(final Tag tag) {
    return database.containsValue(tag);
  }

  /**
   * tag enthalten?
   * @param tag 	nicht null
   * @return		true, wenn enthalten.
   */
  public final Tag getPica3(final String tag) {
    RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
    return pica3Map.get(tag);
  }

  /**
   * Gibt eine Teilmenge der Tags, die zwischen from (einschließlich) und
   * to (einschließlich) liegen.
   * @param from	nicht null, nicht leer.
   * @param to	nicht null, nicht leer.
   * @return		nicht null, nicht modifizierbar.
   */
  public final Collection<Tag> getTagsBetween(final String from, final String to) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("from", from);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("to", to);
    final Map<String, Tag> subMap = pica3Map.subMap(from, true, to, true);
    return Collections.unmodifiableCollection(subMap.values());
  }

  /**
   * Gibt alle Tags, sofern sie eine Pica3-Bezeichnung haben.

   * @return		nicht null.
   */
  public final List<Tag> getAllTags() {
    return new LinkedList<>(pica3Map.values());
  }

  /**
   * Liefert die Tags, die nicht vom Benutzer an der IBW geändert
   * werden dürfen.
   *
   * @return	nicht null
   */
  public Collection<Tag> getUnmodifiables() {
    return Collections.emptyList();
  }

  /**
   * Liefert zu einem GNDTag und einem Indikator das {@link Indicator}-Objekt.
   * Es werden nicht alle Tags durchsucht.
   * @param tagStr    nicht null, nicht leer
   * @param indChar   nicht 0
   * @return          null, wenn nicht gefunden.
   */
  public final Indicator findIndicator(final String tagStr, final char indChar) {
    if (tagStr == null || tagStr.trim().length() == 0 || indChar == 0)
      //@formatter:off
	        throw new IllegalArgumentException(
	                "tag == null oder Indikator == 0");
	    //@formatter:on
    final Tag aTag = findTag(tagStr);
    if (aTag == null)
      return null;
    return aTag.getIndicator(indChar);
  }

  /**
   * Meldet den Tag bei {@link #database} (als Pica3 und Pica+),
   * bei {@link #pica3Map}, bei {@link #picaPlusMap}und bei {@link #marcMap}
   * mit angehängten {@link Tag#marcIndicator1} und
   * {@link Tag#marcIndicator2} an.
   *
   * @param tag	nicht null; wenn schon in der Datenbank so wird eine
   * 				{@link IllegalStateException} geworfen
   */
  protected final void addTag(final Tag tag) {
    if (database.containsKey(tag.pica3) || database.containsKey(tag.picaPlus))
      throw new IllegalStateException("tag schon enthalten: " + tag);

    database.put(tag.pica3, tag);
    database.put(tag.picaPlus, tag);
    pica3Map.put(tag.pica3, tag);
    picaPlusMap.put(tag.picaPlus, tag);
    if (tag.marcIndicator1 == 0)
      marcMap.put(tag.marc, tag);
    else
      marcMap.put((tag.marc + tag.marcIndicator1) + tag.marcIndicator2, tag);
  }

  /**
   * Liefert zu einer Zeile ein Tripel aus Tag am Anfang, dem Match
   * (getrimmt) und dem Rest. Der Rest kann mit einem Blank beginnen.
   * Das ist gefährlich, da die WinIBW überzählige Blanks zwischen Tag
   * und Inhalt in der Vollanzeige ("s d") verschluckt. Im Korrekturmodus
   * sind sie dagegen wieder sichtbar.
   *
   *
   * @param 	line != null.
   * @return	gefundenes Tripel oder null sonst.
   */
  public final Triplett<Tag, String, String> parseTag(final String line) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    final Matcher m = TAG_PATTERN.matcher(line);

    if (m.find()) {
      final String match = m.group().trim();
      final Tag tag = findTag(match);
      if (tag == null) {
        return null;
      }
      // also was gefunden:
      final int posRest = m.end();
      final String rest = line.substring(posRest);

      return new Triplett<>(tag, match, rest);
    }
    return null;

  }

  /**
   * Liefert alle Tags der Liste.
   * @param taglist	nicht null
   * @return			nicht null
   */
  public final Collection<Tag> getTags(final List<String> taglist) {
    RangeCheckUtils.assertReferenceParamNotNull("taglist", taglist);
    final LinkedList<Tag> tags = new LinkedList<>();
    for (final String string : taglist) {
      final Tag tag = findTag(string);
      tags.add(tag);
    }
    return tags;
  }

  /**
   * Liefert alle Tags der Liste.
   * @param taglist	nicht null
   * @return			nicht null
   */
  public final Collection<Tag> getTags(final String... taglist) {
    RangeCheckUtils.assertReferenceParamNotNull("taglist", taglist);
    return getTags(Arrays.asList(taglist));
  }

}
