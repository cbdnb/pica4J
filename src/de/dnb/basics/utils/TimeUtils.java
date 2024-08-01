package de.dnb.basics.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.dnb.basics.filtering.Between;
import de.dnb.basics.filtering.FilterUtils;

public final class TimeUtils {

  /**
   * Datum im Format tt-mm-jj, wie
   * es z.B. als Datum der Ersterfassung
   * auftritt.
   */
  public static final SimpleDateFormat PICA_DATE_FORMAT = new SimpleDateFormat("dd-MM-yy");

  /**
   * Datum im Format jjjj-mm-tt, wie
   * es z.B. als Datum der Mailboxen
   * auftritt.
   */
  public static final SimpleDateFormat MX_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private TimeUtils() {
    super();
    // TODO Auto-generated constructor stub
  }

  public static final double MILLIS_PER_MINUTE = 60. * 1000.;

  public static final double MILLIS_PER_DAY = 24. * 60. * 60. * 1000.;

  private static long startMillis;

  private static long startNanos;

  /**
   * startet eine Stopuhr. Zeiten können über
   * {@link #delta_t_millis()} oder
   * {@link #delta_t_nanos()} genommen werden.
   */
  public static void startStopWatch() {
    startMillis = System.currentTimeMillis();
    startNanos = System.nanoTime();
  }

  /**
   * Zwischenzeit in ms. Uhr muss über {@link #startStopWatch()}
   * gestartet worden sein.
   *
   * @return  Zwischenzeit in ms, weitere Zwischenzeiten können genommen
   *          werden
   */
  public static long delta_t_millis() {
    final long stop = System.currentTimeMillis();
    return stop - startMillis;
  }

  /**
   * Zwischenzeit in ms. Uhr muss über {@link #startStopWatch()}
   * gestartet worden sein.
   *
   * @return  Zwischenzeit in ms, weitere Zwischenzeiten können genommen
   *          werden
   */
  public static long delta_t_nanos() {
    final long stop = System.nanoTime();
    return stop - startNanos;
  }

  /**
   * Deutsche Feiertage nach der Gauss-Formel für das Osterdatum.
   *
   * @param calendar
   *            nicht null
   * @return berücksichtigt auch Ostersonntag ...
   */
  public static boolean isGermanHoliday(final Calendar calendar) {
    final GregorianCalendar gregorianCalendar = cloneDate(calendar);
    final int year = calendar.get(Calendar.YEAR);

    final GregorianCalendar ostersonntag = getEasterDate(year);
    final int easterMonth = ostersonntag.get(Calendar.MONTH);
    final int easterDay = ostersonntag.get(Calendar.DATE);
    final GregorianCalendar ostermontag = new GregorianCalendar(year, easterMonth, (easterDay + 1));
    final GregorianCalendar karfreitag = new GregorianCalendar(year, easterMonth, (easterDay - 2));
    final GregorianCalendar rosenmontag =
      new GregorianCalendar(year, easterMonth, (easterDay - 48));
    final GregorianCalendar christihimmelfahrt =
      new GregorianCalendar(year, easterMonth, (easterDay + 39));
    final GregorianCalendar pfinstsonntag =
      new GregorianCalendar(year, easterMonth, (easterDay + 49));
    final GregorianCalendar pfinstmontag =
      new GregorianCalendar(year, easterMonth, (easterDay + 50));
    final GregorianCalendar frohnleichnahm =
      new GregorianCalendar(year, easterMonth, (easterDay + 60));
    final GregorianCalendar wiedervereinigung = new GregorianCalendar(year, Calendar.OCTOBER, 3);
    final GregorianCalendar weihnachten1 = new GregorianCalendar(year, Calendar.DECEMBER, 24);
    final GregorianCalendar weihnachten2 = new GregorianCalendar(year, Calendar.DECEMBER, 25);
    final GregorianCalendar weihnachten3 = new GregorianCalendar(year, Calendar.DECEMBER, 26);
    final GregorianCalendar silvester = new GregorianCalendar(year, Calendar.DECEMBER, 31);
    final GregorianCalendar neujahr = new GregorianCalendar(year, Calendar.JANUARY, 1);
    final GregorianCalendar tagDerArbeit = new GregorianCalendar(year, Calendar.MAY, 1);

    return ostermontag.equals(gregorianCalendar) || karfreitag.equals(gregorianCalendar)
    // || gc_rosenmontag.equals(gregorianCalendar)
      || ostersonntag.equals(gregorianCalendar) || christihimmelfahrt.equals(gregorianCalendar)
      || pfinstsonntag.equals(gregorianCalendar) || pfinstmontag.equals(gregorianCalendar)
      || frohnleichnahm.equals(gregorianCalendar) || weihnachten1.equals(gregorianCalendar)
      || weihnachten2.equals(gregorianCalendar) || weihnachten3.equals(gregorianCalendar)
      || silvester.equals(gregorianCalendar) || neujahr.equals(gregorianCalendar)
      || wiedervereinigung.equals(gregorianCalendar) || tagDerArbeit.equals(gregorianCalendar);
  }

  /**
   * @param year
   *            Jahr >1583
   * @return Osterdatum nach dem Gauss-Algorithmus
   */
  public static GregorianCalendar getEasterDate(final int year) {
    final int a = year % 19;
    final int b = year % 4;
    final int c = year % 7;
    int month = 0;

    int m = (8 * (year / 100) + 13) / 25 - 2;
    final int s = year / 100 - year / 400 - 2;
    m = (15 + s - m) % 30;
    final int n = (6 + s) % 7;

    int d = (m + 19 * a) % 30;

    if (d == 29)
      d = 28;
    else if (d == 28 && a >= 11)
      d = 27;

    final int e = (2 * b + 4 * c + 6 * d + n) % 7;

    int day = 21 + d + e + 1;

    if (day > 31) {
      day = day % 31;
      month = 3;
    } else
      month = 2;

    final GregorianCalendar easter = new GregorianCalendar(year, month, day);
    return easter;
  }

  /**
   *
   * @param calendar  auch null
   * @return          Quartal (1-4) oder -1, wenn calendar == null
   */
  public static int getQuartal(final Calendar calendar) {
    if (calendar == null)
      return -1;
    return 1 + calendar.get(Calendar.MONTH) / 3;
  }

  /**
   * Zeitunterschied zwischen Terminen in Tagen.
   *
   * @param from
   *            datum 1, auch null
   * @param to
   *            datum 2, auch null
   * @return from - to; 0, wenn from oder to == null
   */
  public static long getDayDifference(final Calendar from, final Calendar to) {
    if (from == null || to == null)
      return 0;
    // erst mal normieren, damit alle die gleiche Zeit haben:
    final Calendar date1 = cloneDate(from);
    final Calendar date2 = cloneDate(to);
    // Differenz in ms:
    final long time = date2.getTime().getTime() - date1.getTime().getTime();
    final long days = Math.round(time / MILLIS_PER_DAY);
    return days;
  }

  /**
   * Zeitunterschied zwischen Terminen in Minuten.
   *
   * @param from
   *            datum 1, auch null
   * @param to
   *            datum 2, auch null
   * @return from - to; 0, wenn from oder to == null
   */
  public static long getMinuteDifference(final Calendar from, final Calendar to) {
    if (from == null || to == null)
      return 0;
    // Differenz in ms:
    final long time = to.getTime().getTime() - from.getTime().getTime();
    final long days = Math.round(time / MILLIS_PER_MINUTE);
    return days;
  }

  /**
   * Zeitunterschied zwischen Terminen in Minuten.
   *
   * @param from
   *            datum 1, auch null
   * @param to
   *            datum 2, auch null
   * @return to - from; 0, wenn from oder to == null
   */
  public static long getMinuteDifference(final Date from, final Date to) {
    if (from == null || to == null)
      return 0;
    // Differenz in ms:
    final long time = to.getTime() - from.getTime();
    final long days = Math.round(time / MILLIS_PER_MINUTE);
    return days;
  }

  /**
   * Zeitunterschied zwischen Terminen in Minuten.
   *
   * @param from
   *            datum 1, auch null
   * @param to
   *            datum 2, auch null
   * @return to - from; 0, wenn from oder to == null
   */
  public static long getDayDifference(final Date from, final Date to) {
    if (from == null || to == null)
      return 0;
    // Differenz in ms:
    final long time = to.getTime() - from.getTime();
    final long days = Math.round(time / MILLIS_PER_DAY);
    return days;
  }

  /**
   * gibt den früheren von zwei Terminen.
   *
   * @param calendar1
   *            nicht null
   * @param calendar2
   *            nicht null
   * @return früheren
   */
  public static Calendar getLowerDate(final Calendar calendar1, final Calendar calendar2) {
    final long diff = getDayDifference(calendar1, calendar2);
    if (diff > 0)
      return calendar1;
    else
      return calendar2;
  }

  /**
   *
   * @param from
   *            nicht null
   * @param to
   *            nicht null
   * @return aufsteigende Liste der Termine von from bis to
   *         (einschließlich)
   */
  public static List<Calendar> getDaysBetween(final Calendar from, final Calendar to) {
    final List<Calendar> list = new ArrayList<>();
    final long diff = Math.abs(getDayDifference(to, from));
    final Calendar lower = getLowerDate(from, to);
    for (int i = 0; i <= diff; i++) {
      final GregorianCalendar cloned = cloneDate(lower);
      cloned.add(Calendar.DAY_OF_YEAR, i);
      list.add(cloned);
    }
    return list;
  }

  public static Function<Calendar, String> debugDateFunction = new Function<Calendar, String>() {
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd. MM. yy");

    @Override
    public String apply(final Calendar x) {
      return formatter.format(x.getTime());
    }
  };

  public static Function<Calendar, String> picaTTMMJJ = new Function<Calendar, String>() {
    @Override
    public String apply(final Calendar x) {
      return PICA_DATE_FORMAT.format(x.getTime());
    }
  };

  public static Function<Calendar, String> mxJJJJMMTT = new Function<Calendar, String>() {
    @Override
    public String apply(final Calendar x) {
      return MX_DATE_FORMAT.format(x.getTime());
    }
  };

  public static Function<Date, String> mxhhMM = new Function<Date, String>() {
    private final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

    @Override
    public String apply(final Date x) {
      return formatter.format(x.getTime());
    }
  };

  /**
   *
   * @return  aktuelle Zeit im Format HH:mm
   */
  public static String getActualTimehhMM() {
    return mxhhMM.apply(new Date());
  }

  /**
   *
   * @param calendar1
   *            nicht null
   * @param calendar2
   *            nicht null
   * @return aufsteigende Liste der Termine von calendar1 bis calendar2
   * (einschließlich)
   */
  public static
    List<Calendar>
    getWorkDaysBetween(final Calendar calendar1, final Calendar calendar2) {
    final List<Calendar> days = getDaysBetween(calendar1, calendar2);
    FilterUtils.filter(days, TimeUtils::isWorkday);
    return days;
  }

  /**
  *
  * @param calendar
  *            nicht null
  *
  * @return aufsteigende Liste der Arbeitstage einer Woche zuvor
  * (einschließlich)
  */
  public static List<Calendar> getWorkDaysWeekBefore(final Calendar calendar) {
    final Calendar calendarPast = (Calendar) calendar.clone();
    calendarPast.add(Calendar.DAY_OF_MONTH, -7);
    return getWorkDaysBetween(calendarPast, calendar);
  }

  /**
   *
   * @return  Arbeitstage eine Woche vor heute (inklusiv)
   */
  public static List<Calendar> getWorkDaysWeekBefore() {
    return getWorkDaysWeekBefore(Calendar.getInstance());
  }

  /**
   *
   * @return  Tage eine Woche vor heute (inklusiv)
   */
  public static List<Calendar> getDaysWeekBefore() {
    return getDaysWeekBefore(Calendar.getInstance());
  }

  /**
  *
  * @param calendar
  *            nicht null
  *
  * @return   aufsteigende Liste der Tage einer Woche zuvor
  *           (einschließlich)
  */
  public static List<Calendar> getDaysWeekBefore(final Calendar calendar) {
    final Calendar calendarPast = (Calendar) calendar.clone();
    calendarPast.add(Calendar.DAY_OF_MONTH, -7);
    return getDaysBetween(calendarPast, calendar);
  }

  /**
   *
   * @param calendar
   *            Termin nicht null
   * @return letzten Arbeitstag vor calendar
   */
  public static GregorianCalendar getWorkdayBefore(final Calendar calendar) {
    final GregorianCalendar gregorianCalendar = cloneDate(calendar);

    decrementDate(gregorianCalendar);
    while (!isWorkday(gregorianCalendar)) {
      decrementDate(gregorianCalendar);
    }
    return gregorianCalendar;
  }

  /**
   *
   * @param calendar
   *            Termin nicht null
   * @param days Anzahl Tage in der Vergangenheit
   * @return letzten Arbeitstag vor calendar
   */
  public static GregorianCalendar getWorkdayBefore(final Calendar calendar, final int days) {
    if (days <= 0)
      throw new IllegalArgumentException("nur positive Zahlen");
    GregorianCalendar before = getWorkdayBefore(calendar);
    for (int i = 1; i < days; i++) {
      before = getWorkdayBefore(before);
    }
    return before;
  }

  /**
   * Macht einen neuen Termin, wobei alle Felder außer Jahr, Monat und Tag
   * genullt werden.
   *
   * @param calendar
   *            nicht null
   * @return neues Datum aus Jahr, Monat und Tag
   */
  public static GregorianCalendar cloneDate(final Calendar calendar) {
    final GregorianCalendar gregorianCalendar = new GregorianCalendar(calendar.get(Calendar.YEAR),
      calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    return gregorianCalendar;
  }

  /**
   * @param calendar
   *            nicht null
   *
   */
  public static void decrementDate(final Calendar calendar) {
    calendar.add(Calendar.DAY_OF_MONTH, -1);
  }

  /**
   * @param calendar
   *            nicht null
   *
   */
  public static void decrementDate(final Calendar calendar, final int amount) {
    calendar.add(Calendar.DAY_OF_MONTH, -amount);
  }

  /**
   * @param calendar
   *            nicht null
   */
  public static void incrementDate(final Calendar calendar) {
    calendar.add(Calendar.DAY_OF_MONTH, 1);
  }

  /**
   * @param calendar
   *            nicht null
   *
   */
  public static void incrementDate(final Calendar calendar, final int amount) {
    calendar.add(Calendar.DAY_OF_MONTH, amount);
  }

  /**
   *
   * @param calendar
   *            nicht null
   * @return ist Arbeitstag?
   */
  public static boolean isWorkday(final Calendar calendar) {
    final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    if (dayOfWeek == Calendar.SUNDAY)
      return false;
    else
      return !isGermanHoliday(calendar);
  }

  public static int getActualYear() {
    return ZonedDateTime.now().getYear();
  }

  public static int getActualMonth() {
    return ZonedDateTime.now().getMonthValue();
  }

  /**
   *
   * @param twoDigitYear zweistellig, zwischen 0 und 99
   * @return  vierstelliges Datum in Abhängigkeit vom aktuellen Datum.
   *          Das zurückgegebene
   *          Datum liegt immer vor dem aktuellen.
   */
  public static int make4DigitYear(final int twoDigitYear) {
    if (twoDigitYear < 0 || twoDigitYear >= 100)
      throw new IllegalArgumentException("Zahl nicht zweistellig: " + twoDigitYear);
    final int actual = getActualYear();
    final int hundred = actual / 100;
    final int shortY = actual % 100;
    if (shortY >= twoDigitYear)
      return hundred * 100 + twoDigitYear;
    else
      return (hundred - 1) * 100 + twoDigitYear;
  }

  /**
  *
  * @param twoDigitYear zweistellig, zwischen 0 und 99
  * @return  vierstelliges Datum in Abhängigkeit vom aktuellen Datum.
  *          Das zurückgegebene
  *          Datum liegt immer vor dem aktuellen.
  * @throws #{@link NumberFormatException}, wenn keine Zahl
  */
  public static int make4DigitYear(final String twoDigitYear) {
    return make4DigitYear(Integer.parseInt(twoDigitYear));
  }

  /**
   * Wie nach Handbuch für das Feld 548 gebraucht: tt.mm.jjjj oder jjjj.
   * Vor Christus wird vor die Jahreszahl ein 'v' eingeschoben.
   */
  public static final DateTimeFormatter FORMATTER_548 = DateTimeFormatter.ofPattern("[d.M.][G]y");

  /**
   * Gibt den {@link TemporalAccessor} für Formate wie 01.01.1910 oder 1910 oder 12.03.v23 .
   * Auch Titan-Besonderheiten wie "00" oder "v00" werden erkannt.
   * Der TA kann etwa mittels ta.get(ChronoField.YEAR) ausgelesen werden.
   *
   * @param s auch null
   * @return  gültiges 548-Datum oder null
   */
  public static TemporalAccessor temporalAccesorFrom548(String s, final boolean endOfPeriod) {
    if (s == null || s.isEmpty())
      return null;
    if (s.equals("00") || s.equals("v00"))
      s = "1";
    s = s.replace("v", "v. Chr.");
    s = s.replace("XXXX", "dummy");

    final boolean vorChr = s.contains("v. Chr.");

    if (endOfPeriod) {
      // Monate
      s = s.replace(".XX.", ".12.");
      s = s.replace(".0X.", ".09.");
      s = s.replace(".1X.", ".12.");
      // Tage
      s = s.replace("XX.", "31.");
      s = s.replaceAll("([012])X\\.", "$19.");
      s = s.replace("3X.", "31.");
    } else {
      // Monate
      s = s.replace(".XX.", ".01.");
      s = s.replace(".0X.", ".01.");
      s = s.replace(".1X.", ".10.");
      // Tage
      s = s.replace("XX.", "01.");
      s = s.replaceAll("([123])X\\.", "$10.");
      s = s.replace("0X.", "01.");
    }
    // Jahre
    if (vorChr && endOfPeriod || !vorChr && !endOfPeriod) {
      // untere Grenze
      s = s.replaceAll("\\.0*X+$", ".1");
      s = s.replaceAll("^0*X+$", "1");
      s = s.replace('X', '0');
    } else {
      // obere Grenze
      s = s.replace('X', '9');
    }

    try {
      return FORMATTER_548.parse(s);
    } catch (final Exception e) {
      return null;
    }
  }

  /**
   * Zum Vergleich, da etliche Methoden keine null zurückgeben, sonder bei
   * Misserfolg den maximalen möglichen Zeitraum.
   */
  public static final Between<LocalDate> MAX_INTERVAL =
    new Between<LocalDate>(LocalDate.MIN, LocalDate.MAX);

  /**
   * Daten, die zum Teil unbekannt sind (durch 'X' gekennzeichnet), werden so
   * behandelt, dass das X durch 1 ersetzt wird. XXXX steht für ein unbekanntes
   * (Sterbe-)Datum, dann wird null zurückgegeben.
   *
   * @param s auch null
   * @param endOfPeriod
   *                  <br>true: Für reine Jahreszahlen ('1234') wird
   *                  der 31.12.1234 zurückgegeben. Für 12XX wird
   *                  der 31.12.1299 zurückgegeben
   *                  <br>false: Der 1.1.1234, bzw der 1.1.1200
   * @return  Das Datum oder null.
   */
  public static LocalDate localDateFrom548(final String s, final boolean endOfPeriod) {
    final TemporalAccessor accessor = temporalAccesorFrom548(s, endOfPeriod);
    if (accessor == null)
      return null;

    return makeLocalDate(accessor, endOfPeriod);
  }

  /**
   *
   * @param accessor      auch null
   * @param endOfPeriod   Wenn nur Jahre: 1.1. oder 31.12.?
   * @return              Datum mit Tagen und Monaten oder null
   */
  public static
    LocalDate
    makeLocalDate(final TemporalAccessor accessor, final boolean endOfPeriod) {
    if (accessor == null)
      return null;
    if (accessor.isSupported(ChronoField.DAY_OF_MONTH))
      return LocalDate.from(accessor);

    if (accessor.isSupported(ChronoField.YEAR)) {
      final Year year = Year.from(accessor);
      if (endOfPeriod)
        return year.atDay(365 + (year.isLeap() ? 1 : 0));
      return year.atDay(1);
    } else
      return null;
  }

  /**
   * Liefert zu einem String in der 548 das zugehörige Intervall, in der Regel
   * eines, das aus einem Zeitpunkt besteht. Werden unbekannte Informationen
   * durch eine 'X' ausgedrückt, so wird das zugehörige Intervall zurückgegeben:
   * <br>11X -> [110 .. 119]
   * <br>v11X -> [v119 .. v110]
   * <br> Enthält datString 'XXXX', wird das maximale Intervall zurückgegeben.
   *
   * @param datString auch null
   * @return          Intervall oder null
   */
  public static Between<LocalDate> get548Interval(final String datString) {
    if (datString == null || datString.isEmpty())
      return null;
    if (datString.contains("XXXX"))
      return new Between<LocalDate>(LocalDate.MIN, LocalDate.MAX);
    final LocalDate min = localDateFrom548(datString, false);
    if (min == null)
      return null;
    final LocalDate max = localDateFrom548(datString, true);
    return new Between<LocalDate>(min, max);

  }

  /**
   *
   * @param nr  >0
   * @param bc  vor Christus?
   * @return    Jahrhundert
   */
  public static Between<LocalDate> makeCentury(final int nr, final boolean bc) {
    if (nr <= 0)
      throw new IllegalArgumentException(nr + " ist nicht positiv");
    final int year1 = bc ? -nr * 100 + 1 : nr * 100;
    if (bc)
      return new Between<LocalDate>(LocalDate.of(year1, 1, 1), LocalDate.of(year1 + 99, 12, 31));
    else
      return new Between<LocalDate>(LocalDate.of(year1 - 99, 1, 1), LocalDate.of(year1, 12, 31));
  }

  static final DateTimeFormatter formatter548d = DateTimeFormatter.ofPattern("[d.M.]y[ G]");

  /**
   * Parst alle Daten, die möglicherweise im Feld $d (ungefähre Zeit) enthalten
   * sind. Das können Zeitpunkte, Zeitabschnnitte, aber auch Angaben wie 4. Jh.,
   * 2. Jahrhundert v. Chr. sein. Das zurückgegebene Intervall ist das aus dem
   * frühesten und dem spätesten Zeitpunkt.
   *
   * @param datString auch null
   * @return          Intervall oder null
   */
  public static Between<LocalDate> get548dInterval(final String datString) {

    if (datString == null || datString.isEmpty())
      return null;
    final TreeSet<LocalDate> dates = new TreeSet<>();

    if (!datString.contains("Jh.") && !datString.contains("Jahrhundert")) {
      // (\d{1,2}\.\d{1,2}\.)?\d+(?!\.)( v\. Chr\.| n\. Chr\.)?
      // (?!\\.) bedeutet: hinten kein '.'
      final Pattern datumPattern = Pattern
        .compile("(\\d{1,2}\\.\\d{1,2}\\.)?" + "\\d+(?!\\.)" + "( v\\. Chr\\.| n\\. Chr\\.)?");
      final Matcher datumMatcher = datumPattern.matcher(datString);
      int i = 0;
      while (datumMatcher.find()) {
        final String m = datumMatcher.group();
        try {
          final TemporalAccessor ac = formatter548d.parse(m);
          // das erstgenannte Datum ist das frühere:
          final LocalDate ld = makeLocalDate(ac, i == 0 ? false : true);
          if (ld != null) {
            dates.add(ld);
            i++;
          }

        } catch (final Exception e) {
          // nix
        }

      }
    } else { // Jh. oder Jahrhundert enthalten

      final Pattern jhPattern =
        Pattern.compile("(\\d+)\\." + " (Jh\\.|Jahrhundert)" + "( v\\. Chr\\.| n\\. Chr\\.)?");
      final Matcher jhMatcher = jhPattern.matcher(datString);
      while (jhMatcher.find()) {
        final String phrase = jhMatcher.group();
        final String zahl = jhMatcher.group(1);
        final int jh = Integer.parseInt(zahl);
        final boolean bc = phrase.contains("v. Chr.");
        final Between<LocalDate> inter = makeCentury(jh, bc);
        dates.add(inter.higherBound);
        dates.add(inter.lowerBound);
      }
    }

    if (!dates.isEmpty()) {
      // Sonderbehandlung für '-1468'
      if (dates.size() == 1) {
        if (datString.startsWith("-"))
          dates.add(LocalDate.MIN);
        if (datString.endsWith("-"))
          dates.add(LocalDate.MAX);
      }
      final LocalDate first = makeLocalDate(dates.first(), false);
      final LocalDate last = makeLocalDate(dates.last(), true);
      return new Between<>(first, last);
    } else {
      return null;
    }

  }

  public static void main(final String[] args) throws InterruptedException, ParseException {
    System.out.println(getActualMonth());

  }

  /**
   * @param source            Datum im Format tt-mm-jj, wie
   *                          es z.B. als Datum der Ersterfassung
   *                          auftritt
   * @return                  Datum oder null
   *
   */
  public static Date parsePicaDate(final String source) {
    Date picaDate;
    try {
      picaDate = PICA_DATE_FORMAT.parse(source);
    } catch (final ParseException e) {
      return null;
    }
    return picaDate;
  }

  /**
   * @param source            Datum im Format jjjj-mm-tt, wie
   *                          es z.B. als Datum der Ersterfassung
   *                          auftritt, auch null
   * @return                  Datum oder null
   *
   */
  public static Date parseMxDate(final String source) {
    if (source == null)
      return null;
    Date mxDate;
    try {
      mxDate = MX_DATE_FORMAT.parse(source);
    } catch (final ParseException e) {
      return null;
    }
    return mxDate;
  }

  /**
   * Gibt das heutige Datum in der Form yyyy-MM-dd aus.
   *
   * @return  heute
   */
  public static String getToday() {
    final Date currentTime = new Date();
    final String dateStr = toYYYYMMDD(currentTime);
    return dateStr;
  }

  /**
   * @param date  nicht null
   * @return      Datum in der Form yyyy-MM-dd
   */
  public static String toYYYYMMDD(final Date date) {
    final String dateStr = MX_DATE_FORMAT.format(date);
    return dateStr;
  }

  /**
   * @param date  nicht null
   * @return      Datum in der Form yy-MM-dd (Status)
   */
  public static String toYYMMDD(final Calendar date) {
    final SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd");
    final String dateStr = formatter.format(date.getTime());
    return dateStr;
  }

  /**
   * @param date  nicht null
   * @return      Datum in der Form yyyy-MM
   */
  public static String toYYYYMM(final Date date) {
    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
    final String dateStr = formatter.format(date);
    return dateStr;
  }

  /**
   * @param date  nicht null
   * @return      Datum in der Form tt.mm.jjjj
   */
  public static String toDDMMYYYY(final Date date) {
    final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
    final String dateStr = formatter.format(date);
    return dateStr;
  }

  /**
   *
   * @param s nicht null, muss mit 6 aufeinander folgende Zahlen beginnen.
   * @return  das Datum aus dem Format yymmdd
   * @throws  NullPointerException  wenn s nicht dem Format entspricht
   */
  public static Date parseMARC(final String s) {
    final SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
    try {
      final Date d = formatter.parse(s);
      return d;
    } catch (final ParseException e) {
      // TODO Auto-generated catch block
      return null;
    }
  }

  /**
   * @param date  auch null
   * @return      Calendar-Objekt (aktueller als Date) oder null
   */
  public static Calendar getCalendar(final Date date) {
    if (date == null)
      return null;
    final Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    return calendar;
  }

}
