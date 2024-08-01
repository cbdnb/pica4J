package de.dnb.basics.marc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import de.dnb.basics.Constants;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.filtering.RangeCheckUtils;

public final class DDCMarcUtils {

  /**
   * Nummern haben mindestens 3 Ziffern.
   */
  public static final int MINIMAL_DDC_NUMBER_LENGTH = 3;

  private DDCMarcUtils() {
  }

  /**
   *
   * Im Feld 008 steht 'b' an Position 12.
   *
   * @param record	nicht null
   * @return			true, wenn 765-Feld (synth. Nummer) vorhanden
   */
  public static boolean isSynthesizedNumber(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final String fixed = MarcUtils.getFixedLengthDataElements(record);
    return StringUtils.charAt(fixed, 12) == 'b';
    //        return MarcUtils.containsField(record, "765");
  }

  /**
   *
   * Wird die Nummer in einer Haupt- oder Hilfstafel angezeigt?
   * Kriterium ist, dass das letzte Zeichen (Position 13) im
   * Feld 008 ein 'a' ist (andernfalls == 'b').
   *
   * @param record    nicht null
   * @return          Ob die Nummer im Druckwerk auftaucht
   */
  public static boolean isDisplayedInStandardSchedulesOrTables(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final String fixed = MarcUtils.getFixedLengthDataElements(record);
    return StringUtils.charAt(fixed, 13) == 'a';
  }

  /**
   *
   * Gelöschte Notation.
   * Im Feld 008 steht 'h' an Position 13.
   *
   * @param record    nicht null
   * @return          Ob gelöscht
   */
  public static boolean isDeleted(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final String fixed = MarcUtils.getFixedLengthDataElements(record);
    return StringUtils.charAt(fixed, 13) == 'h';
  }

  /**
   *
   * Optionale Notation. Beispiel: (780.16).
   * Im Feld 008 steht 'b' an Position 9.
   *
   * @param record    nicht null
   * @return          Ob optional
   */
  public static boolean isOptional(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final String fixed = MarcUtils.getFixedLengthDataElements(record);
    return StringUtils.charAt(fixed, 9) == 'b';
  }

  /**
   *
   * Standard, nicht optionale Notation.
   * Im Feld 008 steht 'a' an Position 9.
   *
   * @param record    nicht null
   * @return          Ob Standard
   */
  public static boolean isStandard(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final String fixed = MarcUtils.getFixedLengthDataElements(record);
    return StringUtils.charAt(fixed, 9) == 'a';
  }

  /**
   *
   * Established.
   * Im Feld 008 steht 'a' an Position 11.
   * <br>
   * Extent to which the formulation of the the 1XX number or term
   * conforms to the classification scheme coded in field 084
   * (Classification Scheme and Edition).
   *
   * @param record    nicht null
   * @return          Ob Standard
   */
  public static boolean isEstablished(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final String fixed = MarcUtils.getFixedLengthDataElements(record);
    return StringUtils.charAt(fixed, 11) == 'a';
  }

  /**
   *
   * Obsolete Notation. Beispiel: [T1—0863]. Verlegt oder stillgelegt.
   * Im Feld 008 steht 'e' an Position 8.
   *
   * @param record    nicht null
   * @return          Ob Obsolet
   */
  public static boolean isObsolete(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final String fixed = MarcUtils.getFixedLengthDataElements(record);
    return StringUtils.charAt(fixed, 8) == 'e';
  }

  /**
   *
   * Valide Notation.
   * Im Feld 008 steht 'a' an Position 8.
   *
   * @param record    nicht null
   * @return          Ob valide
   */
  public static boolean isValid(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final String fixed = MarcUtils.getFixedLengthDataElements(record);
    return StringUtils.charAt(fixed, 8) == 'a';
  }

  /**
   * Datensatz deckt einen Bereich (z.B. 004-006) ab.
   * Im Feld 008 steht 'a' an Position 7
   *
   * @param record	nicht null
   * @return			kein Bereich
   */
  public static boolean isSpan(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final String fixed = MarcUtils.getFixedLengthDataElements(record);
    return StringUtils.charAt(fixed, 7) != 'a';
  }

  /**
   * Hilfstafel.
   * Im Feld 008 steht 'b' an Position 6.
   *
   * @param record    nicht null
   * @return          Hilfstafel
   */
  public static boolean isTable(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final String fixed = MarcUtils.getFixedLengthDataElements(record);
    return StringUtils.charAt(fixed, 6) == 'b';
  }

  /**
   * Haupttafel.
   * Im Feld 008 steht 'a' an Position 6.
   *
   * @param record    nicht null
   * @return          Haupttafel
   */
  public static boolean isSchedule(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final String fixed = MarcUtils.getFixedLengthDataElements(record);
    return StringUtils.charAt(fixed, 6) == 'a';
  }

  /**
   * Ist Überblicks-Datensatz (Am Anfang eines Abschnittes)?
   *
   * @param record	nicht null
   * @return			true, wenn Nummer weniger als 2 Ziffern hat (false
   * 					sonst, auch wenn kein 153-Feld vorhanden)
   */
  public static boolean isOverview(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final String number = getFullClassificationNumber(record);
    if (number == null)
      return false;
    return number.length() < MINIMAL_DDC_NUMBER_LENGTH;
  }

  /**
   * Ist der Datensatz eine Anhängeanweisung (erkennbar an 2 $a)?
   *
   * @param record nicht null
   * @return		Ist der Datensatz eine Anhängeanweisung?
   */
  public static boolean isAddInstructionOld(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final DataField nameField = getNumberField(record);
    if (nameField == null)
      return false;
    final List<Subfield> aSubfields = nameField.getSubfields('a');
    return aSubfields.size() == 2;
  }

  /**
   * Ist der Datensatz eine interne Anhängeanweisung (erkennbar $y 1)?
   *
   * @param record nicht null
   * @return      Ist der Datensatz eine interne Anhängeanweisung?
   */
  public static boolean isInternalAddTable(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final DataField nameField = getNumberField(record);
    if (nameField == null)
      return false;
    final Subfield subfield = nameField.getSubfield('y');
    if (subfield == null)
      return false;
    if (subfield.getData().equals("1"))
      return true;
    else
      return false;
  }

  /**
   * Ist der Datensatz eine Anhängeanweisung (erkennbar an $c oder $y)?
   * <li>in $c steht die Endnummer eines Spans
   * <li>in $y steht eine interne Anhängetafel
   * <br>nicht ganz klar!
   * @param record nicht null
   * @return      Ist der Datensatz eine Anhängeanweisung?
   */
  public static boolean isAddInstruction(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final DataField nameField = getNumberField(record);
    if (nameField == null)
      return true;
    Subfield subfield = nameField.getSubfield('c');
    if (subfield != null)
      return true;
    subfield = nameField.getSubfield('y');
    if (subfield != null)
      return true;

    return false;
  }

  /**
   * Gibt an, ob 673 $a mit "\" endet. Wenn record
   * nicht im Feld 5401 vorkommen darf, dann wird false
   * zurückgegeben.
   *
   * @param record  nicht null.
   * @return        ist DNB-Kurznotation (endet mit \).
   */
  public static boolean isKurznotation(final Record record) {
    if (!isUsedInWinIBW(record)) {
      return false;
    }
    final String kurz = getAbridgedNumber(record);
    if (kurz == null)
      return false;
    else
      return kurz.endsWith("\\");
  }

  /**
   *
   * @param record    auch null
   * @return          Datensatz darf in 5401 vorkommen
   */
  public static boolean isUsedInWinIBW(final Record record) {
    if (record == null) {
      return false;
    }
    if (!DDCMarcUtils.isDDCRecord(record)) {
      return false;
    }
    if (!isValid(record)) {
      return false;
    }
    if (DDCMarcUtils.isOverview(record)) {
      return false;
    }
    if (DDCMarcUtils.isSpan(record)) {
      return false;
    }

    if (!DDCMarcUtils.isDisplayedInStandardSchedulesOrTables(record)) {
      return false;
    }
    if (DDCMarcUtils.isAddInstruction(record)) {
      return false;
    }
    final String name = DDCMarcUtils.getCaption(record);
    if (name == null) {
      return false;
    }
    return true;
  }

  /**
   * Liefert eine String-Repäsentation des Feldes ohne Unterfeld-Indikatoren.
   * @param dataField nicht null
   * @return          String
   */
  public static String flatten(final DataField dataField) {
    final List<Subfield> list = dataField.getSubfields();
    return FilterUtils.foldLeft(list, (x, y) -> x + " " + y.getData(), "");
  }

  /**
   * Gibt die Felder, die Registereinträge enthalten.
   *
   * @param record	nicht null
   * @return			nicht null. Indexfelder
   *                  (700, 710, 711, 720, 730, 748, 750, 751,
   * 					753, 754)
   */
  public static List<DataField> getIndexTermFields(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final List<DataField> variableFields = MarcUtils.getDataFields(record, "700", "710", "711",
      "720", "730", "748", "750", "751", "753", "754");
    return variableFields;
  }

  /**
   * Gibt die Registereinträge (Einrückungen durch '--' gekennzeichnet).
   *
   * @param record	nicht null
   * @return			Liste der Registereinträge eines DDC-Marc-Datensatzes
   */
  public static List<String> getFullIndexTerms(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final List<DataField> indexFields = getIndexTermFields(record);
    final List<String> indexTerms = new ArrayList<>();
    for (final DataField dataField : indexFields) {
      String s = dataField.getSubfield('a').getData();
      final List<Subfield> subsx = dataField.getSubfields('x');
      for (final Subfield subfield : subsx) {
        s += "--" + subfield.getData();
      }
      indexTerms.add(s);
    }
    return indexTerms;
  }

  /**
   * Gibt den Namen der DDC-NUmmer.
   *
   * @param record	nicht null
   * @return			Namen oder null, wenn nicht vorhanden
   */
  public static String getCaption(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final DataField dataField = getNumberField(record);
    if (dataField == null)
      return null;
    final Subfield captionSubfield = dataField.getSubfield('j');
    if (captionSubfield == null)
      return null;
    return captionSubfield.getData();
  }

  /**
   * Gibt das 153-Feld, das den Eintrag in der Tafel (Nummer + Name) enthält.
   *
   * @param record	nicht null
   * @return			Feld 153 oder null, wenn nicht vorhanden
   */
  public static DataField getNumberField(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final DataField dataField = (DataField) record.getVariableField("153");
    return dataField;
  }

  /**
   * Gibt das 673-Feld, das die Kurznotation (abridged number) enthält.
   *
   * @param record    nicht null
   * @return          Feld 673 oder null, wenn nicht vorhanden
   */
  public static DataField getSegmentedNumberField(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final DataField dataField = (DataField) record.getVariableField("673");
    return dataField;
  }

  /**
   *
   * @param record	nicht null
   * @return			true, wenn 153-Feld enthalten
   */
  public static boolean isDDCRecord(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    return getNumberField(record) != null;
  }

  /**
   * Gibt die Klassifikationsnummer (mit 'TX--' vorneweg).
   *
   *
   * @param record	nicht null
   * @return			Nummer oder null
   */
  public static String getFullClassificationNumber(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);

    final String number = getClassificationNumber(record);
    if (number == null)
      return null;
    final String s = getTablePrefix(record);
    return s + number;
  }

  /**
   * Gibt die Kurznummer (mit 'TX--' vorneweg).
   *
   *
   * @param record    nicht null
   * @return          Nummer oder null
   */
  public static String getAbridgedClassificationNumber(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);

    String number = getAbridgedNumber(record);
    if (number == null) {
      number = getClassificationNumber(record);
      if (number == null)
        return null;
    } else {
      final String[] parts = number.split("/");
      number = parts[0];
    }
    final String s = getTablePrefix(record);
    return s + number;
  }

  /**
   * Gibt die Klassifikationsnummer, also den Inhalt von Unterfeld a
   * (ohne 'TX--' für Hilfstafeln vorneweg).
   *
   *
   * @param record    nicht null
   * @return          Nummer oder null
   */
  public static String getClassificationNumber(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final DataField numberField = getNumberField(record);
    if (numberField == null)
      return null;
    return numberField.getSubfield('a').getData();
  }

  /**
   * Gibt die Kurznotation, also den Inhalt von 673 $a
   * (ohne 'TX--' für Hilfstafeln vorneweg).
   *
   *
   * @param record    nicht null
   * @return          Nummer oder null
   */
  public static String getAbridgedNumber(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final DataField numberField = getSegmentedNumberField(record);
    if (numberField == null)
      return null;
    final Subfield subfieldA = numberField.getSubfield('a');
    if (subfieldA == null)
      return null;
    return subfieldA.getData();
  }

  /**
   * Gibt die übergeordnete Klassifikationsnummer, also den
   * Inhalt von Unterfeldern $z und $e
   * (mit 'TX--' für Hilfstafeln vorneweg).
   *
   *
   * @param record    nicht null
   * @return          Nummer oder null
   */
  public static String getFullClassificationNumberHierarchy(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);

    final DataField numberField = getNumberField(record);
    if (numberField == null)
      return null;
    final Subfield subfieldE = numberField.getSubfield('e');
    if (subfieldE == null)
      return null;
    final String number = subfieldE.getData();
    if (number == null)
      return null;
    final String s = getTablePrefix(record);
    return s + number;
  }

  /**
   * @param record    nicht null
   * @return          TX-- oder leeren String
   */
  public static String getTablePrefix(final Record record) {
    final String table = getTableNumber(record);
    String s;
    if (table != null)
      s = "T" + table + "--";
    else
      s = "";
    return s;
  }

  /**
   * Gibt die Nummer der Hilfstafel.
   *
   *
   * @param record    nicht null
   * @return          Nummer oder null
   */
  public static String getTableNumber(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final DataField numberField = getNumberField(record);
    if (numberField == null)
      return null;
    // Tabellen-Nummer in $z:
    final Subfield table = numberField.getSubfield('z');
    if (table == null)
      return null;
    return table.getData();
  }

  /**
   * Gibt die Basisnummmer, an die angehängt wurde.
   *
   * @param field765  nicht null, tag = 765
   * @return          Nummer oder null, wenn nicht vorhanden
   */
  public static String getBaseNumber(final DataField field765) {
    RangeCheckUtils.assertReferenceParamNotNull("field765", field765);
    final String tag = field765.getTag();
    if (!tag.equals("765"))
      throw new IllegalArgumentException("Feld " + field765 + "hat nicht tag 765");
    final ArrayList<Subfield> subs = new ArrayList<>(field765.getSubfields());
    for (int i = 0; i < subs.size(); i++) {
      final Subfield sub = subs.get(i);
      if (sub.getCode() == 'b') {
        // z zuvor? Dann Hilfstafel
        if (i > 0) {
          final Subfield subZ = subs.get(i - 1);
          if (subZ.getCode() == 'z') {
            final String table = subZ.getData();
            return "T" + table + "--" + sub.getData();
          }
        }
        // keine Hilfstafel
        final String raw = sub.getData();
        return StringUtils.rightPadding(raw, 3, '0');
      }
    }

    return null;
  }

  /**
   * DIE Basis-Nummer, an die angehängt wurde. Sie steht im letzten
   * 765-Feld. Wenn nicht angehängt wurde, einfach die DDC-Nummer.
   *
   * @param record    nicht null
   * @return          Nummer oder null, wenn nicht vorhanden
   */
  public static String getBaseNumber(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final List<DataField> fields765 = MarcUtils.getDataFields(record, "765");
    final int size = fields765.size();
    if (size == 0)
      return getFullClassificationNumber(record);
    final DataField field765 = fields765.get(size - 1);
    return getBaseNumber(field765);
  }

  public static String getPicaFields(final Record record, final int number) {
    String s = "";
    final String synt = getFullClassificationNumber(record);
    if (!synt.startsWith("T"))
      s += "54" + number + "0 [DDC22ger]" + synt + "\n";
    final String base = getBaseNumber(record);
    if (base.startsWith("T"))
      s += "54" + number + "3 -" + base + "\n";
    else
      s += "54" + number + "1 " + base + "\n";
    final ArrayList<String> adds = getAddNumbers(record);
    //@formatter:off
        for (final ListIterator<String> iterator
                = adds.listIterator(adds.size());
                iterator.hasPrevious();) {
            final String add = iterator.previous();
            //@formatter:on
      if (add.startsWith("T"))
        s += "54" + number + "3 -" + add + "\n";
      else
        s += "54" + number + "2 " + add + "\n";
    }
    return s;
  }

  /**
   * Nummer, die angehängt wurde. Diese steht im Feld 765 in den
   * Unterfeldern $r (Root) und $s (angehängt). Steht vor diesen
   * direkt ein $z, so ist es eine Hilfstafelnotation.
   *
   * @param field765  nicht null
   * @return          Nummer oder null, wenn nicht gefunden
   */
  public static String getAddNumber(final DataField field765) {
    RangeCheckUtils.assertReferenceParamNotNull("field765", field765);
    final String tag = field765.getTag();
    if (!tag.equals("765"))
      throw new IllegalArgumentException("Feld " + field765 + "hat nicht tag 765");

    final ArrayList<Subfield> subs = new ArrayList<>(field765.getSubfields());
    for (int i = 0; i < subs.size(); i++) {
      final Subfield sub = subs.get(i);
      if (sub.getCode() == 'r' || sub.getCode() == 's') {
        String dataZ = "";
        final String dataSub = sub.getData();
        String dataNext = "";
        // z zuvor?
        if (i > 0) {
          final Subfield subZ = subs.get(i - 1);
          if (subZ.getCode() == 'z') {
            dataZ = "T" + subZ.getData() + "--";
          }
        }
        if (i < subs.size() - 1) {
          final Subfield subNext = subs.get(i + 1);
          if (subNext.getCode() == 'r' || subNext.getCode() == 's') {
            dataNext = subNext.getData();
          }
        }
        if (dataZ.equals(""))
          return concatenate(dataSub, dataNext);
        else
          return dataZ + dataSub + dataNext;

      }
    }
    return null;

  }

  /**
   * Gibt die Nummern, die an die Grundnotation angehängt werden,
   * um die synthetisierte Nummer zu erhalten. Es werden alle
   * 765-Felder ausgewertet.
   *
   *
   * @param record    nicht null, auf DDC wird nicht geprüft.
   * @return          nicht null, ohne null in der Collection
   */
  public static ArrayList<String> getAddNumbers(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final List<DataField> fields765 = MarcUtils.getDataFields(record, "765");
    final Function<DataField, String> function = new Function<DataField, String>() {
      @Override
      public String apply(final DataField x) {
        return getAddNumber(x);
      }
    };
    return FilterUtils.mapNullFiltered(fields765, function);
  }

  /**
   * Baut die Nummer und fügt, wenn nötig, an dritter Stelle einen Punkt ein.
   * @param root      nicht null
   * @param added     nicht null
   * @return          DDC-Nummer
   */
  public static String concatenate(final String root, final String added) {
    final StringBuffer buffer = new StringBuffer(root);
    buffer.append(added);
    final int len = buffer.length();
    if (len < MINIMAL_DDC_NUMBER_LENGTH)
      throw new IllegalArgumentException("weniger als 3 Ziffern");
    if (len > MINIMAL_DDC_NUMBER_LENGTH) {
      if (!root.contains("."))
        buffer.insert(MINIMAL_DDC_NUMBER_LENGTH, '.');
    }
    return buffer.toString();
  }

  public static void main(final String[] args) throws IOException {

    final MarcIterator it = MarcIterator.getFromFile(Constants.DDC_XML);
    final AtomicInteger i = new AtomicInteger();
    it.forEach(record ->
    {
      final DataField nameField = getNumberField(record);
      if (nameField == null)
        return;
      final Subfield subfield = nameField.getSubfield('y');
      if (subfield == null)
        return;
      if (subfield.getData().equals("1"))
        return;
      else {
        i.incrementAndGet();
        System.out.println(MarcUtils.readableFormat(record));
      }
    });
    System.out.println(i);

  }

  /**
   * @param record    nicht null
   * @return
   */
  public static boolean isAbridged(final Record record) {
    final String number = DDCMarcUtils.getFullClassificationNumber(record);
    final String abridged = DDCMarcUtils.getAbridgedClassificationNumber(record);
    final boolean isAbridged = number.equals(abridged);
    return isAbridged;
  }

}
