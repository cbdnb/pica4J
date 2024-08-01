package de.dnb.gnd.utils.mx;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.collections.ListUtils;
import de.dnb.basics.utils.TimeUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.line.LineFactory;
import de.dnb.gnd.parser.line.LineParser;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.utils.GNDUtils;
import de.dnb.gnd.utils.RecordUtils;
import de.dnb.gnd.utils.SubfieldUtils;
import de.dnb.gnd.utils.mx.MXAddress.Adressierung;

/**
 * Repräsentiert eine einzelne Mailbox. Sie kann mittels der statischen Funktionen
 *
 * <li>{@link Mailbox#parse(Line, String...)} aus
 * einem einzelnen Mailbox-Feld gewonnen werden.
 * <li>{@link Mailbox#parse(Record, String...)} gewinnt eine Liste aller Mailboxen des
 * Datensatzes.
 *
 * <br>
 *
 * Eine Mailbox hat folgende Felder:
 * <li>absender. Dieser kann mittels {@link this#setAbsender(MXAddress)} gesetzt und mittels
 * {@link this#getAbsender()} ausgelesen werden.
 *
 * @author baumann
 *
 */
public class Mailbox implements Comparable<Mailbox> {

  public final static GNDTagDB DB = GNDTagDB.getDB();

  public final static Tag TAG901 = DB.findTag("901");

  public final static Indicator IND_DATUM = TAG901.getIndicator('z');

  public final static Indicator IND_ADR = TAG901.getIndicator('b');

  public final static Indicator IND_TEXT = TAG901.getIndicator('a');

  public final static LineFactory FACTORY = TAG901.getLineFactory();

  /**
   * @param absender the absender to set
   */
  public void setAbsender(final MXAddress absender) {
    this.absender = absender;
  }

  /**
   * @param Text ohne Zeilenumbrüche und '$'
   */
  public void setText(String text) {
    text = text.replace("\n", "");
    text = text.replace("$", "");
    this.text = text;
  }

  /**
   * @param date the date to set
   */
  public void setDate(final Date date) {
    this.date = date;
  }

  /**
   * Absender.
   */
  private MXAddress absender;

  /**
   * Alle anderen, ob Empfänger oder stumm geschaltete.
   */
  private final List<MXAddress> beteiligte = new ArrayList<>();

  /**
   * Botschaft.
   */
  private String text;

  /**
   * Datum.
   */
  private Date date;

  /**
   * @return the absender
   */
  public MXAddress getAbsender() {
    return absender;
  }

  /**
   * @return die Beteiligten ohne Absender.
   */
  public List<MXAddress> getBeteiligte() {
    return beteiligte;
  }

  /**
   * Beteiligte ohne Absender.
   *
   * @return
   */
  public Set<MXAddress> getBeteiligteGeordnet() {
    return new TreeSet<>(beteiligte);
  }

  /**
   * nullsichere Methode
   *
   * @param i beliebig
   * @return  i-ten Beteiligten (0-basiert) oder null
   */
  public MXAddress getBeteiligten(final int i) {
    return ListUtils.getElement(beteiligte, i).orElse(null);
  }

  /**
   *
   * @return  Empfänger UND Absender.
   */
  public Set<MXAddress> getAllAdresses() {
    final TreeSet<MXAddress> adresses = new TreeSet<>();
    adresses.addAll(beteiligte);
    if (absender != null)
      adresses.add(absender);
    return adresses;
  }

  /**
   *
   * @param beteiligter wenn null oder schon vorhanden, keine Aktion. Kein Absender!
   */
  public void addBeteiligten(final MXAddress beteiligter) {
    if (beteiligter != null && !beteiligte.contains(beteiligter))
      beteiligte.add(beteiligter);
  }

  /**
  *
  * @param beteiligter  beliebig
  * @return             ob entfernt werden konnte
  */
  public boolean removeBeteiligten(final MXAddress beteiligter) {
    final boolean rem = beteiligte.remove(beteiligter);
    return rem;
  }

  /**
  *
  * @param nr  beliebig
  * @return    entfernten Beteiligten
  */
  public MXAddress removeBeteiligten(final int nr) {
    if (nr >= 0 && nr < beteiligte.size())
      return beteiligte.remove(nr);
    else
      return null;
  }

  /**
   * löscht alle Beteiligten
   */
  public void clearBeteiligte() {
    beteiligte.clear();
  }

  /**
   *
   * @param addresses Adressen der Beteiligten (Sender und Empfänger)
   */
  public void setBeteiligte(final Collection<MXAddress> addresses) {
    beteiligte.clear();
    addAllBeteiligte(addresses);
  }

  /**
   *
   * @param addresses kann auch null enthalten, die nullen werden aber
   *                  herausgefiltert
   */
  public void addAllBeteiligte(final Collection<MXAddress> addresses) {
    addresses.forEach(bet -> addBeteiligten(bet));
  }

  /**
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
  *
  * @param mxLine    nicht null
  * @param ausnahmen Strings wie "FLLB", die keine gültige Adressierung darstellen
  * @return          alle Mailboxen
  */
  public static Mailbox parse(final Line mxLine, final String... ausnahmen) {
    final Mailbox mailbox = new Mailbox();
    mailbox.date = getDate(mxLine);
    mailbox.text = getMessage(mxLine);
    final String addStr = getAddressField(mxLine);
    final List<String> rawAddresses = getRawAddresses(addStr);
    boolean found = false;
    for (final String rawAdd : rawAddresses) {
      if (Arrays.asList(ausnahmen).contains(rawAdd)) {
        continue;
      }
      final MXAddress parsed = MXAddress.parse(rawAdd);
      if (parsed == null)
        continue;
      if (!found && parsed.getAdressierung() == Adressierung.ABSENDER) {
        found = true;
        mailbox.setAbsender(parsed);
      } else {
        mailbox.addBeteiligten(parsed);
      }
    }
    return mailbox;
  }

  @Override
  public String toString() {
    String mx2string = "Mailbox: ";
    final String datumStr = TimeUtils.toDDMMYYYY(getDate());
    mx2string += "\n\tDatum: " + datumStr;
    mx2string += "\n\tAbsender:\n";
    mx2string += absender.toString(2);
    mx2string += "\n\tWeitere Beteiligte:";
    String beteiligteStr = "";
    for (final MXAddress bet : beteiligte) {
      beteiligteStr += "\n" + bet.toString(2);
    }
    mx2string += beteiligteStr;
    mx2string += "\n\tText: " + text;

    return mx2string;
  }

  /**
   *
   * @return  Das Unterfeld $z mit dem Datum.
   *
   * @throws IllFormattedLineException
   */
  public Subfield getSubDatum() throws IllFormattedLineException {
    return new Subfield(IND_DATUM, TimeUtils.toYYYYMMDD(getDate()));
  }

  /**
   *
   * @return  Das Unterfeld $a mit der Mitteilung.
   * @throws IllFormattedLineException
   */
  public Subfield getSubMessage() throws IllFormattedLineException {
    if (getText() == null || getText().isEmpty())
      return null;
    return new Subfield(IND_TEXT, getText());
  }

  /**
   *
   * @return  Das Unterfeld $b mit den Adressen. Den Absender zuerst.
   * @throws IllFormattedLineException
   */
  public Subfield getSubAdressen() throws IllFormattedLineException {
    final List<String> adrStrList = new ArrayList<>();

    final MXAddress absender = getAbsender();
    if (absender != null)
      adrStrList.add(absender.asMxString());

    getBeteiligteGeordnet().stream().map(MXAddress::asMxString).forEach(s -> adrStrList.add(s));
    final String adrStr = StringUtils.concatenate(" ", adrStrList);
    return new Subfield(IND_ADR, adrStr);
  }

  /**
   *
   * @return  Als Zeile oder null. Wenn kein Textvorhanden, wird dieser
   *          ignoriert
   */
  public Line toLine() {
    Subfield subText;
    try {
      subText = getSubMessage();
    } catch (final IllFormattedLineException e1) {
      subText = null;
    }
    try {
      if (subText != null)
        FACTORY.load(getSubDatum(), getSubAdressen(), subText);
      else
        FACTORY.load(getSubDatum(), getSubAdressen());
      return FACTORY.createLine();
    } catch (final IllFormattedLineException e) {
      return null;
    }
  }

  public String asMxString() {
    final Line line = toLine();
    return RecordUtils.toPica(line, Format.PICA3, true, '$');
  }

  /**
   *
   * @param mxLine nicht null
   * @return       Datum der Mailbox oder null
   */
  public static Date getDate(final Line mxLine) {
    final String dateString = getRawDate(mxLine);
    if (dateString == null)
      return null;
    final Date date = TimeUtils.parseMxDate(dateString);
    return date;
  }

  /**
   * @param mxLine  nicht null
   * @return        Datum in der Form jjjj-mm-tt oder null
   */
  public static String getRawDate(final Line mxLine) {
    return SubfieldUtils.getContentOfFirstSubfield(mxLine, 'z');
  }

  public static Date getFirstDate(final Record record) {
    final ArrayList<Line> mxx = GNDUtils.getMXLines(record);
    Date first = null;
    for (final Line mxLine : mxx) {
      final Date current = getDate(mxLine);
      if (current == null)
        continue;
      else if (first == null)
        first = current;
      else if (first.compareTo(current) > 0)
        first = current;

    }
    return first;
  }

  /**
   *
   * @param mxLine nicht null
   * @return       Nachrichten(unter)feld in $a
   */
  public static String getMessage(final Line mxLine) {
    final String message = SubfieldUtils.getContentOfFirstSubfield(mxLine, 'a');
    return message;
  }

  /**
   *
   * @param mxLine nicht null
   * @return       Adress(unter)feld in $b
   */
  public static String getAddressField(final Line mxLine) {
    final String add = SubfieldUtils.getContentOfFirstSubfield(mxLine, 'b');
    return add;
  }

  /**
   *
   * @param adressField nicht null
   * @return            die duch " " gertrennten Teilstrings
   */
  public static List<String> getRawAddresses(final String adressField) {
    if (adressField == null)
      return Collections.emptyList();
    // im Pica+-Format werden zwei aufeinanderfolgende Leerzeichen in
    // Leerzeichen + geschütztes Leerzeichen (x00A0 = \h) umgewandelt:
    final String[] addArray = adressField.split("(\\s|\\h)+");
    return Arrays.asList(addArray);
  }

  public static List<String> splitRawAddress(final String rawAdress) {
    final String[] addArray = rawAdress.split("\\-");
    return Arrays.asList(addArray);
  }

  /**
   * @param mxL nicht null
   * @return    Empfänger ohne e-
   */
  public static List<String> getRawSenders(final Line mxL) {
    final List<String> rawAdresses = getRawAddresses(getAddressField(mxL));
    final List<String> rawRecipients = getRawSenders(rawAdresses);
    return rawRecipients;
  }

  public static List<String> getRawSenders(final List<String> rawAdresses) {
    final ArrayList<String> senders = new ArrayList<>();
    rawAdresses.forEach(raw ->
    {
      final String prefix = "a-";
      if (raw.startsWith(prefix))
        senders.add(raw.substring(prefix.length()));
    });
    return senders;
  }

  /**
   * @param mxL nicht null
   * @return    Empfänger ohne e-
   */
  public static List<String> getRawRecipients(final Line mxL) {
    final List<String> rawAdresses = getRawAddresses(getAddressField(mxL));
    final List<String> rawRecipients = getRawRecipients(rawAdresses);
    return rawRecipients;
  }

  /**
   *
   * @param rawAdresses nicht null
   * @return            Empfänger ohne e-
   */
  public static List<String> getRawRecipients(final List<String> rawAdresses) {
    final ArrayList<String> recipients = new ArrayList<>();
    rawAdresses.forEach(raw ->
    {
      final String prefix = "e-";
      if (raw.startsWith(prefix))
        recipients.add(raw.substring(prefix.length()));
    });
    return recipients;
  }

  /**
   *
   * @param record    nicht null
   * @param ausnahmen Strings wie "FLLB", die keine gültige Adressierung darstellen
   * @return          alle Mailboxen
   */
  public static Collection<Mailbox> parse(final Record record, final String... ausnahmen) {
    final List<Mailbox> mailboxes = new ArrayList<>();
    final ArrayList<Line> mxx = GNDUtils.getMXLines(record);
    mxx.forEach(mxL ->
    {
      final Mailbox mailbox = parse(mxL, ausnahmen);
      mailboxes.add(mailbox);
    });
    return mailboxes;
  }

  /**
   *
   * @param record  nicht null
   * @param ausnahmen sollen nicht berücksichtigt werden
   * @return        Alle Empfänger und Absender, nicht null
   */
  public static Set<MXAddress> getAllAdresses(final Record record, final String... ausnahmen) {
    final TreeSet<MXAddress> adresses = new TreeSet<>();
    parse(record, ausnahmen).forEach(mx ->
    {
      final Set<MXAddress> mxads = mx.getAllAdresses();
      adresses.addAll(mxads);
    });
    return adresses;

  }

  public static void main(final String[] args) throws ParseException, IllFormattedLineException {

    final String aContentStr = StringUtils.readClipboard();
    final Line line = LineParser.parseGND(aContentStr);
    System.out.println(line);
    final Mailbox mx = parse(line);
    System.out.println(mx);

  }

  @Override
  public int compareTo(final Mailbox o) {
    return date.compareTo(o.date);
  }

}
