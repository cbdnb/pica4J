/**
 *
 */
package de.dnb.gnd.utils.formatter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import de.dnb.basics.applicationComponents.MyFileUtils;
import de.dnb.basics.applicationComponents.Streams;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.collections.CollectionUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.parser.Field;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.RecordReader;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.parser.tag.TagDB;
import de.dnb.gnd.utils.RecordUtils;

/**
 * Formatiert Datensätze so, dass man sie in eine Exceltabelle
 * einfügen kann. Die Felder der Datensätze werden vorgegeben oder
 * vom Programm erraten.
 *
 * @author baumann
 *
 */
public class ExcelFormatter {

  /**
   * Extrahiert aus einem Datensatz alle Tags, die im Konstruktor 
   * {@link ExcelFormatter#ExcelFormatter(TagDB, Collection)} oder 
   * {@link ExcelFormatter#ExcelFormatter(TagDB, Tag...)} festgelegt wurden. 
   * <br><br>
   * Die Inhalte eines Tags werden, wenn wiederholbar, im Pica3-Format untereinander in eine 
   * Excel-Zelle geschrieben.
   *
   * @param record  nicht null, beliebig.
   * @return  Einen String, den man in eine Excel-Tabelle kopieren kann.
   */
  public String formatRecord(final Record record) {
    Objects.requireNonNull(record);
    return record.getId() + "\t" + tags.stream().map(tag ->
    {
      final Field field = record.getField(tag);
      String zelle = "";
      if (field != null) {
        zelle = Streams.getStreamFromIterable(field).map(line -> RecordUtils.toPicaWithoutTag(line))
          .collect(Collectors.joining("\n"));
      }
      return StringUtils.makeExcelCell(zelle);
    }).collect(Collectors.joining("\t"));
  }

  /**
   * Ermittel die Überschrift der Tabelle aus dem übergebenen oder errechneten
   * Felder.
   *
   * @param verbose wenn true, dann verbale Auflösung der Felder, z.B.
   *                Person – Bevorzugter Name für 100, sonst die Nummer
   * @return        Überschrift
   *
   */
  public String makeHeadline(final boolean verbose) {
    return "IDN" + "\t" + tags.stream().map(tag -> verbose ? tag.german : tag.pica3)
      .collect(Collectors.joining("\t"));
  }

  /**
   * Eine nichtleere Liste der extrahierten Tags. Wird im Konstruktor festgelegt.
   */
  private final Collection<Tag> tags;

  /**
   * @param tagDB Datenbank
   * @param tags  berücksichtigte Tags; wenn null oder leer, werden alle Tags der Datenbank
   *              berücksichtigt.
   */
  public ExcelFormatter(final TagDB tagDB, Collection<Tag> tags) {
    super();
    if (CollectionUtils.isNullOrEmpty(tags)) {
      tags = tagDB.getAllTags();
    }
    this.tags = tags;
  }

  /**
   * @param tagDB Datenbank
   * @param tagsS Tags als String, wenn null oder leer, werden alle Tags der Datenbank
   *              berücksichtigt.
   */
  public static ExcelFormatter getFormatter(final TagDB tagDB, final List<String> tagsS) {
    List<Tag> tags = null;
    if (tagsS != null)
      tags = tagsS.stream().map(s -> tagDB.findTag(s)).collect(Collectors.toList());
    return new ExcelFormatter(tagDB, tags);
  }

  /**
   * @param tagDB Datenbank
   * @param tags  Tags als String, wenn leer, werden alle Tags der Datenbank
   *              berücksichtigt.
   */
  public static ExcelFormatter getFormatter(final TagDB tagDB, final String... tags) {
    return getFormatter(tagDB, Arrays.asList(tags));
  }

  /**
   * @param tagDB Datenbank
   * @param tags  für die Extraktion berücksichtigte Tags; wenn leer, werden alle Tags der 
   * 			Datenbank berücksichtigt.
   */
  public ExcelFormatter(final TagDB tagDB, final Tag... tags) {
    super();
    this.tags = Arrays.asList(tags);
  }

  /**
   * Hilfsfunktion. Berücksichtigt in der Liste <code>records</code> nur die Tags, die auch 
   * wirklich in mindestens einem Datensatz vorkommen - außer: 001., 042@ und 003@
   * @param records nicht null, nicht leer
   * @param verbose Tags in der Überschrift werden verbal aufgelöst, wenn true
   * @return        Eine vollständige Exceltabelle als String
   */
  @SuppressWarnings("null")
  public static String format(final Collection<Record> records, final boolean verbose) {
    RangeCheckUtils.assertCollectionParamNotNullOrEmpty("records", records);

    // Wird mit dem ersten Datensatz aus records gesetzt:
    TagDB db = null;

    // Erst mal die relevanten Tags suchen:
    final Set<Tag> tags = new TreeSet<>();
    for (final Record record : records) {
      final LinkedHashSet<Tag> recordsTags = record.getTags();
      if (db == null)
        db = record.tagDB;
      tags.addAll(recordsTags);
    }
    final Set<Tag> unerwuenschte = db.findTagPattern("001.|042@|003@");
    tags.removeAll(unerwuenschte);

    
    final ExcelFormatter formatter = new ExcelFormatter(db, tags);
    final String ret = formatter.makeHeadline(verbose);

    return ret + "\n" + records.stream().map(record -> formatter.formatRecord(record))
      .collect(Collectors.joining("\n"));
  }

  /**
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) throws IOException {
    final RecordReader reader = RecordReader.getMatchingReader("D:/Analysen/jahns/professoren.dow");
    final List<Record> records = reader.stream().collect(Collectors.toList());
    final PrintWriter out = MyFileUtils.oeffneAusgabeDatei("D:/Analysen/jahns/professoren.tab", false);
    final String format = format(records, true);
    //    System.err.println(format);
    out.println(format);
  }

}
