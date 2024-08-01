/**
 *
 */
package de.dnb.gnd.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.gnd.parser.Record;

/**
 * @author baumann
 *
 */
public class SystematikUtils {

  private static Map<String, String> sys2descr = new TreeMap<>(new SystematikComparator());

  static {
    sys2descr.put("00", "Unspezifische Allgemeinwörter");
    sys2descr.put("00m", "Maschinell eingespielte Datensätze (Platzhalter)");
    sys2descr.put("00p", "Sachlich nicht klassifizierbare Personen");
    sys2descr.put("1", "Allgemeines, Interdisziplinäre Allgemeinwörter");
    sys2descr.put("2.1", "Schrift, Handschriftenkunde");
    sys2descr.put("2.1p", "Personen zu Schrift, Handschriftenkunde");
    sys2descr.put("2.2", "Buchwissenschaft, Buchhandel");
    sys2descr.put("2.2p", "Personen zu Buchwissenschaft, Buchhandel");
    sys2descr.put("2.3", "Presse");
    sys2descr.put("2.3p", "Personen zu Presse");
    sys2descr.put("3.1",
      "Allgemeine und vergleichende Religionswissenschaft, Nichtchristliche Religionen");
    sys2descr.put("3.1p",
      "Personen zu allgemeiner und vergleichender Religionswissenschaft, Personen zu");
    sys2descr.put("3.2", "Bibel");
    sys2descr.put("3.2a", "Altes Testament");
    sys2descr.put("3.2aa", "Teile des Alten Testamentes");
    sys2descr.put("3.2b", "Neues Testament");
    sys2descr.put("3.2ba", "Teile des Neuen Testamentes");
    sys2descr.put("3.2p", "Personen der Bibel");
    sys2descr.put("3.3", "Kirchengeschichte");
    sys2descr.put("3.3a", "Kirchengeschichte: Antike");
    sys2descr.put("3.3b", "Kirchengeschichte: Mittelalter");
    sys2descr.put("3.3c", "Kirchengeschichte: Neuzeit");
    sys2descr.put("3.4a", "Systematische Theologie (Allgemeines), Fundamentaltheologie");
    sys2descr.put("3.4b", "Dogmatik");
    sys2descr.put("3.4c", "Theologische Anthropologie, Christliche Ethik");
    sys2descr.put("3.5", "Praktische Theologie");
    sys2descr.put("3.5a", "Liturgik, Frömmigkeit");
    sys2descr.put("3.5ba", "Homiletik");
    sys2descr.put("3.5bb", "Katechetik, Christliche Erziehung, Kirchliche Bildungsarbeit");
    sys2descr.put("3.5ca", "Seelsorge");
    sys2descr.put("3.5cb", "Mission, Kirchliche Sozialarbeit");
    sys2descr.put("3.6", "Kirche und Konfession");
    sys2descr.put("3.6a", "Katholische Kirche");
    sys2descr.put("3.6b", "Evangelische Kirchen");
    sys2descr.put("3.6c", "Ostkirchen und andere christliche Religionsgemeinschaften und Sekten");
    sys2descr.put("3.6p",
      "Personen zu Kirchengeschichte, Systematischer und Praktischer Theologie, Kirche und Konfession");
    sys2descr.put("4.1", "Philosophie (Allgemeines)");
    sys2descr.put("4.2", "Philosophiegeschichte");
    sys2descr.put("4.3", "Erkenntnistheorie, Logik");
    sys2descr.put("4.4", "Metaphysik");
    sys2descr.put("4.5", "Ethik, Philosophische Anthropologie, Sozialphilosophie");
    sys2descr.put("4.6", "Ästhetik");
    sys2descr.put("4.7", "Kulturphilosophie");
    sys2descr.put("4.7p", "Personen zu Philosophie");
    sys2descr.put("5.1a", "Psychologie (Allgemeines), Experimentelle Psychologie");
    sys2descr.put("5.1b", "Psychologische Diagnostik, Tests");
    sys2descr.put("5.2", "Entwicklungspsychologie, Vergleichende Psychologie");
    sys2descr.put("5.3", "Sozial-, Kultur- und Völkerpsychologie");
    sys2descr.put("5.4", "Tiefenpsychologie");
    sys2descr.put("5.5", "Angewandte Psychologie, Psychohygiene");
    sys2descr.put("5.5p", "Personen zu Psychologie");
    sys2descr.put("5.6", "Parapsychologie");
    sys2descr.put("5.7", "Esoterik");
    sys2descr.put("5.7p", "Personen zu Parapsychologie, Esoterik");
    sys2descr.put("6.1a", "Kultur, Künste allgemein");
    sys2descr.put("6.1b", "Geistes- und Kulturgeschichte");
    sys2descr.put("6.1p", "Personen zu Kultur und Künsten, Geistes- und Kulturgeschichte");
    sys2descr.put("6.2a", "Bildungswesen (Allgemeines)");
    sys2descr.put("6.2b", "Geschichte des Bildungswesens");
    sys2descr.put("6.3a", "Schule");
    sys2descr.put("6.3b", "Berufsausbildung");
    sys2descr.put("6.4", "Unterricht");
    sys2descr.put("6.4p", "Personen zu Bildungswesen");
    sys2descr.put("6.5", "Wissenschaft");
    sys2descr.put("6.6", "Hochschule");
    sys2descr.put("6.7", "Bibliothek, Information und Dokumentation");
    sys2descr.put("6.7p", "Personen zu Bibliothek, Information und Dokumentation");
    sys2descr.put("6.8", "Archiv, Museum");
    sys2descr.put("6.8p", "Personen zu Archiv, Museum");
    sys2descr.put("7.1a", "Recht allgemein, Rechtsphilosophie");
    sys2descr.put("7.1b", "Rechtsvergleich");
    sys2descr.put("7.2", "Rechtsgeschichte, Verfassungsgeschichte");
    sys2descr.put("7.2a", "Rechtsgeschichte, Verfassungsgeschichte: Altertum");
    sys2descr.put("7.2b", "Rechtsgeschichte, Verfassungsgeschichte: Mittelalter");
    sys2descr.put("7.2c", "Rechtsgeschichte, Verfassungsgeschichte: Neuzeit");
    sys2descr.put("7.3", "Staatsrecht, Verfassungsrecht");
    sys2descr.put("7.4", "Allgemeines Verwaltungsrecht");
    sys2descr.put("7.5a", "Dienstrecht (auch Richter)");
    sys2descr.put("7.5b", "Kommunalrecht");
    sys2descr.put("7.5c",
      "Baurecht, Raumordnung, Bodenrecht, Umweltrecht, Naturschutzrecht, Denkmalschutz");
    sys2descr.put("7.5d", "Kulturrecht, Presserecht, Rundfunkrecht, Bildungswesen und Forschung");
    sys2descr.put("7.5e", "Straßen- und Wegerecht, Verkehrsrecht, Telekommunikationsrecht");
    sys2descr.put("7.5f", "Wehrrecht");
    sys2descr.put("7.6a", "Polizeirecht, Ordnungsrecht");
    sys2descr.put("7.6b", "Gesundheitsrecht");
    sys2descr.put("7.6c", "Kriminologie");
    sys2descr.put("7.7a", "Strafrecht");
    sys2descr.put("7.7b", "Strafvollzug");
    sys2descr.put("7.8a", "Rechtspflege, Prozessrecht");
    sys2descr.put("7.8b", "Rechtsmedizin");
    sys2descr.put("7.9a", "Finanzrecht");
    sys2descr.put("7.9b", "Steuerrecht, Zollrecht");
    sys2descr.put("7.10a", "Wirtschaftsrecht, Wirtschaftsaufsicht");
    sys2descr.put("7.10b", "Gewerblicher Rechtsschutz, Urheberrecht");
    sys2descr.put("7.11a", "Arbeitsrecht, Sozialrecht, Recht der Tarifverträge");
    sys2descr.put("7.11b", "Berufsrecht");
    sys2descr.put("7.11c", "Gebührenrecht");
    sys2descr.put("7.12", "Bürgerliches Recht");
    sys2descr.put("7.12a", "Allgemeiner Teil des Bürgerlichen Rechts");
    sys2descr.put("7.12b", "Schuldrecht");
    sys2descr.put("7.12c", "Sachenrecht");
    sys2descr.put("7.12d", "Familienrecht");
    sys2descr.put("7.12e", "Erbrecht");
    sys2descr.put("7.13", "Religionsrecht, Kirchenrecht, Staatskirchenrecht");
    sys2descr.put("7.14",
      "Internationales Recht (einschließlich Völkerrecht und Recht der Europäischen Gemeinschaften, Europäischen Union), Kollisionsrecht");
    sys2descr.put("7.14p", "Personen zu Recht");
    sys2descr.put("7.15a", "Öffentliche Verwaltung, Öffentlicher Dienst");
    sys2descr.put("7.15b", "Geschichte der Öffentlichen Verwaltung");
    sys2descr.put("7.15p", "Personen zu öffentlicher Verwaltung");
    sys2descr.put("8.1", "Politik (Allgemeines), Politische Theorie");
    sys2descr.put("8.1p", "Personen (Politologen, Staatstheoretiker)");
    sys2descr.put("8.2a", "Innenpolitik");
    sys2descr.put("8.2b", "Parteien, Politische Organisationen");
    sys2descr.put("8.3", "Außenpolitik");
    sys2descr.put("8.4", "Militär");
    sys2descr.put("8.4p", "Personen zu politischer Theorie, Militär");
    sys2descr.put("9.1", "Sozialgeschichte");
    sys2descr.put("9.1a", "Sozialgeschichte: Altertum");
    sys2descr.put("9.1b", "Sozialgeschichte: Mittelalter");
    sys2descr.put("9.1c", "Sozialgeschichte: Neuzeit");
    sys2descr.put("9.2",
      "Sozialwissenschaften allgemein, Soziologische Theorien, Statistik in den Sozialwissenschaften");
    sys2descr.put("9.2a", "Sozialwissenschaften allgemein, Soziologische Theorien");
    sys2descr.put("9.2b",
      "Methoden und Techniken der empirischen Sozialforschung, Statistik in den");
    sys2descr.put("9.3a", "Soziales Leben, Bevölkerung (Allgemeines), Gesellschaft allgemein");
    sys2descr.put("9.3b", "Bevölkerung, Sozialstruktur, Soziale Situation, Soziale Bewegungen");
    sys2descr.put("9.3c", "Gruppe, Organisationssoziologie, Interaktion");
    sys2descr.put("9.3d", "Sozialisation, Sozialverhalten");
    sys2descr.put("9.3e", "Kommunikation, Meinungsbildung");
    sys2descr.put("9.4a", "Arbeit");
    sys2descr.put("9.4ab",
      "Einzelne Berufe, Tätigkeiten, Funktionen; Religionszugehörigkeit, Weltanschauung");
    sys2descr.put("9.4b", "Mitbestimmung, Gewerkschaften");
    sys2descr.put("9.5a", "Sozialpolitik, Entwicklungshilfe");
    sys2descr.put("9.5b", "Sozialversicherung und Gesetzliche Krankenversicherung");
    sys2descr.put("9.5c", "Sozialarbeit, Sozialhilfe");
    sys2descr.put("9.5p", "Personen zu Soziologie, Gesellschaft, Arbeit, Sozialgeschichte");
    sys2descr.put("10.1", "Wirtschaftsgeschichte");
    sys2descr.put("10.1a", "Wirtschaftsgeschichte: Altertum");
    sys2descr.put("10.1b", "Wirtschaftsgeschichte: Mittelalter");
    sys2descr.put("10.1c", "Wirtschaftsgeschichte: Neuzeit");
    sys2descr.put("10.1p", "Personen zu Wirtschaftsgeschichte (bis ca. 1900)");
    sys2descr.put("10.2a", "Wirtschaft, Volkswirtschaft (Allgemeines)");
    sys2descr.put("10.2aa", "Volkswirtschaft");
    sys2descr.put("10.2ab", "Wirtschaftssystem");
    sys2descr.put("10.2ac", "Mathematische Methoden, Information, Entscheidung");
    sys2descr.put("10.2b", "Haushalt, Verbraucher");
    sys2descr.put("10.2c", "Mikroökonomie, Wettbewerb");
    sys2descr.put("10.2d", "Konjunktur, Verteilung, Wirtschaftsstruktur");
    sys2descr.put("10.2da", "Wirtschaftskreislauf, Konjunktur");
    sys2descr.put("10.2db", "Verteilung");
    sys2descr.put("10.2dc", "Wirtschaftsstruktur");
    sys2descr.put("10.2dp", "Personen zu Wirtschaftswissenschaften");
    sys2descr.put("10.2e", "Außenwirtschaft, Außenhandel");
    sys2descr.put("10.2ea", "Außenwirtschaft, Außenhandel gesamtwirtschaftlich");
    sys2descr.put("10.2eb", "Außenwirtschaft, Außenhandel betrieblich");
    sys2descr.put("10.2ep", "Personen zu Außenwirtschaft, Außenhandel");
    sys2descr.put("10.3a", "Öffentliche Aufgaben");
    sys2descr.put("10.3b", "Öffentliche Wirtschaft, Abfallwirtschaft, Gesundheitsökonomie");
    sys2descr.put("10.3c", "Energie- und Wasserwirtschaft");
    sys2descr.put("10.3d", "Rohstoffwirtschaft");
    sys2descr.put("10.3p", "Personen zu öffentlichen Aufgaben, "
      + "Öffentlicher Wirtschaft, Energie- und Rohstoffwirtschaft");
    sys2descr.put("10.4", "Wirtschaftspolitik");
    sys2descr.put("10.4p", "Personen zu Wirtschaftspolitik");
    sys2descr.put("10.5", "Finanzwirtschaft, Finanzpolitik");
    sys2descr.put("10.5p", "Personen zu Finanzwirtschaft, Finanzpolitik");
    sys2descr.put("10.6a", "Telekommunikation und Verkehr");
    sys2descr.put("10.6b", "Fremdenverkehr, Hotel- und Gaststättengewerbe");
    sys2descr.put("10.6p", "Personen zu Telekommunikation und Verkehr, Fremdenverkehr");
    sys2descr.put("10.7a", "Umweltschutz, Umweltbelastung");
    sys2descr.put("10.7b", "Raumordnung, Stadtplanung, Landschaftsgestaltung");
    sys2descr.put("10.7p", "Personen zu Umweltschutz, Raumordnung, Landschaftsgestaltung");
    sys2descr.put("10.8a", "Öffentliche und private Bautätigkeit, Bau- und Bodenpolitik");
    sys2descr.put("10.8b", "Bauwirtschaft, Baubetrieb und Bodenmarkt");
    sys2descr.put("10.8p", "Personen zu Bau, Boden");
    sys2descr.put("10.9a", "Geldtheorie, Geldpolitik, Währung");
    sys2descr.put("10.9b", "Bank");
    sys2descr.put("10.9c", "Kapitalmarkt, Börse, Kapitalanlage");
    sys2descr.put("10.9p", "Personen zu Geld, Bank, Börse");
    sys2descr.put("10.10", "Genossenschaft, Gemeinwirtschaft, Alternative Wirtschaft");
    sys2descr.put("10.10p",
      "Personen zu Genossenschaft, Gemeinwirtschaft, Alternativer Wirtschaft");
    sys2descr.put("10.11a", "Betriebswirtschaftslehre (Allgemeines), Unternehmen, Management");
    sys2descr.put("10.11b", "Mathematische Methoden, Information, Entscheidung");
    sys2descr.put("10.11c", "Beschaffung, Produktion");
    sys2descr.put("10.11d", "Kosten");
    sys2descr.put("10.11e", "Marketing, Wettbewerb");
    sys2descr.put("10.11f", "Rechnungswesen, Steuer, Revision");
    sys2descr.put("10.11g", "Investition, Finanzierung");
    sys2descr.put("10.11h", "Personalpolitik, Arbeitsgestaltung");
    sys2descr.put("10.11i", "Büro, Bürokommunikation");
    sys2descr.put("10.11m", "Spezielle Informationssysteme, Programme");
    sys2descr.put("10.11p", "Personen zu Betriebswirtschaftslehre");
    sys2descr.put("10.12", "Gewerbe allgemein, Industrie, Handwerk");
    sys2descr.put("10.12a", "Industrie, Industriebetrieb, Handwerk");
    sys2descr.put("10.12b", "Einzelne Branchen der Industrie und des Handwerks");
    sys2descr.put("10.12p", "Personen zu Gewerbe allgemein, Industrie, Handwerk");
    sys2descr.put("10.13a", "Handel allgemein, Dienstleistung allgemein");
    sys2descr.put("10.13b", "Handel einzelner Branchen, Dienstleistung einzelner Branchen");
    sys2descr.put("10.13p", "Personen zu Handel, Dienstleistung");
    sys2descr.put("10.14", "Versicherung");
    sys2descr.put("10.14p", "Personen zu Versicherung");
    sys2descr.put("10.15", "Werbewirtschaft, Öffentlichkeitsarbeit");
    sys2descr.put("10.15p", "Personen zu Werbewirtschaft, Öffentlichkeitsarbeit");
    sys2descr.put("11.1a", "Sprache (Allgemeines)");
    sys2descr.put("11.1b", "Historische Sprachwissenschaft");
    sys2descr.put("11.1c", "Sprachliche Technik");
    sys2descr.put("11.2a", "Allgemeine Sprachtheorie");
    sys2descr.put("11.2b", "Grammatik");
    sys2descr.put("11.2c", "Phonetik, Phonologie");
    sys2descr.put("11.2p", "Personen zu Sprache");
    sys2descr.put("11.3a", "Lexikologie");
    sys2descr.put("11.3b", "Namenkunde");
    sys2descr.put("11.3c", "Fachsprache");
    sys2descr.put("11.3d", "Anonymes Werk als Sprachdenkmal");
    sys2descr.put("12.1a", "Allgemeine Literaturwissenschaft");
    sys2descr.put("12.1b", "Literarisches Leben");
    sys2descr.put("12.1p", "Personen zu Literaturwissenschaft (Literaturwissenschaftler)");
    sys2descr.put("12.2a", "Literaturgeschichte");
    sys2descr.put("12.2b", "Anonyme literarische Werke");
    sys2descr.put("12.2p", "Personen zu Literaturgeschichte (Schriftsteller)");
    sys2descr.put("12.3", "Literaturgattung");
    sys2descr.put("12.4", "Literarische Motive, Stoffe, Gestalten");
    sys2descr.put("12.4p", "Personen als literarisches Motiv");
    sys2descr.put("12.4y", "Geografische Namen als literarisches Motiv");
    sys2descr.put("13.1a", "Bildende Kunst");
    sys2descr.put("13.1b", "Kunstgeschichte");
    sys2descr.put("13.1c", "Sachliche Motive in der Kunst");
    sys2descr.put("13.1cp", "Personen als künstlerisches Motiv");
    sys2descr.put("13.1cy", "Geografische Namen als künstlerisches Motiv");
    sys2descr.put("13.2", "Plastik");
    sys2descr.put("13.2p", "Personen zu Plastik");
    sys2descr.put("13.3", "Malerei");
    sys2descr.put("13.4", "Zeichnung, Grafik");
    sys2descr.put("13.4p", "Personen zu Malerei, Zeichnung, Grafik");
    sys2descr.put("13.5", "Fotografie");
    sys2descr.put("13.5p", "Personen zu Fotografie");
    sys2descr.put("13.6", "Kunsthandwerk");
    sys2descr.put("13.6p", "Personen zu Kunsthandwerk");
    sys2descr.put("13.7", "Neue Formen der Kunst");
    sys2descr.put("13.7p", "Personen zu neuen Formen der Kunst");
    sys2descr.put("14.1", "Musik (Allgemeines), Musikgeschichte");
    sys2descr.put("14.2", "Musikalische Form, Musikgattung");
    sys2descr.put("14.3", "Musikinstrumentenkunde, Musikinstrumentenbau");
    sys2descr.put("14.4", "Systematische und Angewandte Musikwissenschaft");
    sys2descr.put("14.4p", "Personen zu Musik");
    sys2descr.put("15.1", "Theater, Tanz");
    sys2descr.put("15.1p", "Personen zu Theater, Tanz");
    sys2descr.put("15.2", "Kabarett, Zirkus, Varieté");
    sys2descr.put("15.2p", "Personen zu Kabarett, Zirkus, Varieté");
    sys2descr.put("15.3", "Film");
    sys2descr.put("15.3p", "Personen zu Film");
    sys2descr.put("15.4", "Rundfunk, Neue Medien");
    sys2descr.put("15.4p", "Personen zu Rundfunk, Neuen Medien");
    sys2descr.put("16.1", "Geschichte (Allgemeines)");
    sys2descr.put("16.1p", "Personen der Geschichtswissenschaft (Historiker, Archäologen)");
    sys2descr.put("16.2", "Quellen und Historische Hilfswissenschaften");
    sys2descr.put("16.3", "Archäologie, Vor- und Frühgeschichte");
    sys2descr.put("16.4", "Geschichte überregionaler Gebiete");
    sys2descr.put("16.4a", "Geschichte überregionaler Gebiete: Altertum");
    sys2descr.put("16.4b", "Geschichte überregionaler Gebiete: Mittelalter");
    sys2descr.put("16.4c", "Geschichte überregionaler Gebiete: Neuzeit");
    sys2descr.put("16.4d", "Geschichte überregionaler Gebiete: Zeitgeschichte");
    sys2descr.put("16.5", "Geschichte einzelner Länder und Völker");
    sys2descr.put("16.5p", "Personen der Geschichte (Politiker und historische Persönlichkeiten)");
    sys2descr.put("17.1", "Volkskunde, Völkerkunde (Allgemeines)");
    sys2descr.put("17.2", "Brauchtum, Volksglaube");
    sys2descr.put("17.3", "Sachkultur, Volkskunst");
    sys2descr.put("17.4", "Volksliteratur, Volksmusik");
    sys2descr.put("17.4p", "Personen zu Volkskunde, Völkerkunde");
    sys2descr.put("18", "Natur, Naturwissenschaften allgemein");
    sys2descr.put("18p", "Personen zu Natur, Naturwissenschaften allgemein");
    sys2descr.put("19", "Geowissenschaften");
    sys2descr.put("19.1a", "Geografie, Heimat- und Länderkunde (Allgemeines)");
    sys2descr.put("19.1b", "Physische Geografie");
    sys2descr.put("19.1c", "Anthropogeografie");
    sys2descr.put("19.1d", "Reise");
    sys2descr.put("19.1dp", "Personen zu Geografie, Heimat- und Länderkunde");
    sys2descr.put("19.2", "Geodäsie, Kartografie");
    sys2descr.put("19.2p", "Personen zu Geodäsie, Kartografie");
    sys2descr.put("19.3", "Hydrologie, Meereskunde");
    sys2descr.put("19.3p", "Personen zu Hydrologie, Meereskunde");
    sys2descr.put("19.4a", "Allgemeine Geologie, Geophysik");
    sys2descr.put("19.4b", "Historische Geologie");
    sys2descr.put("19.4c", "Mineralogie, Boden-, Gesteins- und Lagerstättenkunde");
    sys2descr.put("19.4d", "Paläontologie");
    sys2descr.put("19.4p",
      "Personen zu Geologie, Mineralogie, Historischer Geologie, Boden-, Gesteins- und Lagerstättenkunde, Paläontologie");
    sys2descr.put("19.5", "Meteorologie, Klimatologie, Hochatmosphäre, Magnetosphäre");
    sys2descr.put("19.5p", "Personen zu Meteorologie, Klimatologie, Hochatmosphäre, Magnetosphäre");
    sys2descr.put("20", "Astronomie, Weltraumforschung");
    sys2descr.put("20p", "Personen zu Astronomie, Weltraumforschung");
    sys2descr.put("21.1", "Physik (Allgemeines), Mathematische Physik");
    sys2descr.put("21.2", "Mechanik, Wärme, Akustik");
    sys2descr.put("21.3", "Elektrizität, Magnetismus, Optik");
    sys2descr.put("21.4", "Elementarteilchen, Kern-, Atom-, Molekularphysik");
    sys2descr.put("21.5", "Plasma, Gas, Flüssigkeit, Festkörper");
    sys2descr.put("21.5p", "Personen zu Physik");
    sys2descr.put("22.1", "Chemie (Allgemeines)");
    sys2descr.put("22.2", "Theoretische und Physikalische Chemie");
    sys2descr.put("22.3", "Analytische Chemie und Untersuchungsmethoden");
    sys2descr.put("22.4", "Anorganische Chemie");
    sys2descr.put("22.5", "Organische Chemie");
    sys2descr.put("22.5p", "Personen zu Chemie");
    sys2descr.put("23.1a", "Biologie allgemein");
    sys2descr.put("23.1b", "Genetik, Evolution");
    sys2descr.put("23.2", "Biochemie, Biophysik, Zytologie");
    sys2descr.put("23.3", "Mikrobiologie");
    sys2descr.put("23.4", "Untersuchungsmethoden (Biologie)");
    sys2descr.put("23.4p", "Personen zu allgemeiner Biologie, Mikrobiologie");
    sys2descr.put("24.1", "Botanik (Allgemeines)");
    sys2descr.put("24.2", "Allgemeine Botanik");
    sys2descr.put("24.2a", "Pflanzenanatomie, Pflanzenphysiologie");
    sys2descr.put("24.2b", "Pflanzensoziologie, Pflanzenökologie, Pflanzengeografie");
    sys2descr.put("24.3", "Spezielle Botanik");
    sys2descr.put("24.3p", "Personen zu Botanik");
    sys2descr.put("25.1", "Zoologie (Allgemeines)");
    sys2descr.put("25.2", "Allgemeine Zoologie");
    sys2descr.put("25.2a", "Anatomie, Tierphysiologie");
    sys2descr.put("25.2b", "Tiersoziologie, Tierökologie, Tiergeografie, Verhaltensforschung");
    sys2descr.put("25.3", "Spezielle Zoologie");
    sys2descr.put("25.3p", "Personen zu Zoologie");
    sys2descr.put("26", "Anthropologie");
    sys2descr.put("26p", "Personen zu Anthropologie");
    sys2descr.put("27.1a", "Medizin (Allgemeines)");
    sys2descr.put("27.1b", "Medizingeschichte");
    sys2descr.put("27.2", "Anatomie");
    sys2descr.put("27.3a", "Physiologie (Allgemeines), Physiologische Chemie");
    sys2descr.put("27.3b", "Blut, Kardiovaskuläres System, Atmungsorgan");
    sys2descr.put("27.3c", "Ernährung, Stoffwechsel, Inkretion");
    sys2descr.put("27.3d", "Haut, Knochen, Nerven, Muskeln, Sinnesorgane");
    sys2descr.put("27.3e", "Harn- und Geschlechtsorgane");
    sys2descr.put("27.4", "Allgemeine Pathologie, Onkologie, Experimentelle Medizin");
    sys2descr.put("27.5", "Allgemeine Diagnostik");
    sys2descr.put("27.6", "Medizinische Radiologie, Nuklearmedizin");
    sys2descr.put("27.7", "Allgemeine Therapie");
    sys2descr.put("27.8a", "Pharmazie, Pharmakologie, Toxikologie");
    sys2descr.put("27.8b", "Immunologie");
    sys2descr.put("27.9", "Innere Medizin");
    sys2descr.put("27.9a", "Hämatologie, Kardiologie");
    sys2descr.put("27.9b", "Pulmonologie");
    sys2descr.put("27.9c", "Gastroenterologie, Endokrinopathie");
    sys2descr.put("27.9d", "Urologie, Nephrologie, Andrologie");
    sys2descr.put("27.9e", "Knochen, Gelenke, Muskeln");
    sys2descr.put("27.9f", "Infektionen");
    sys2descr.put("27.10", "Chirurgie, Orthopädie");
    sys2descr.put("27.11", "Gynäkologie, Geburtshilfe");
    sys2descr.put("27.12", "Kinderheilkunde");
    sys2descr.put("27.13", "Neurologie, Psychiatrie");
    sys2descr.put("27.14", "Dermatologie, Venerologie");
    sys2descr.put("27.15", "Hals-Nasen-Ohren-Heilkunde");
    sys2descr.put("27.16", "Augenheilkunde");
    sys2descr.put("27.17", "Zahnmedizin");
    sys2descr.put("27.18", "Sexualmedizin");
    sys2descr.put("27.19", "Sondergebiete der Medizin");
    sys2descr.put("27.20", "Hygiene, Gesundheitswesen");
    sys2descr.put("27.20p", "Personen zu Medizin, Tiermedizin");
    sys2descr.put("27.21", "Tiermedizin");
    sys2descr.put("28", "Mathematik");
    sys2descr.put("28p", "Personen zu Mathematik");
    sys2descr.put("29", "Stochastik, Operations Research");
    sys2descr.put("29p", "Personen zu Stochastik, Operations Research");
    sys2descr.put("30", "Informatik, Datenverarbeitung");
    sys2descr.put("30m", "Informatikprodukte (Hardware- und Softwareprodukte)");
    sys2descr.put("30p", "Personen zu Informatik, Datenverarbeitung");
    sys2descr.put("31.1a", "Technik (Allgemeines)");
    sys2descr.put("31.1b", "Technische Physik, Technische Mathematik");
    sys2descr.put("31.1c", "Mess-, Steuerungs- und Regelungstechnik");
    sys2descr.put("31.1d", "Werkstoffkunde, Werkstoffprüfung");
    sys2descr.put("31.1e", "Technikgeschichte");
    sys2descr.put("31.2", "Sanitärtechnik, Umwelttechnik");
    sys2descr.put("31.3a", "Architektur");
    sys2descr.put("31.3ab", "Ortsgebundene Bauwerke");
    sys2descr.put("31.3b", "Bautechnik");
    sys2descr.put("31.3p", "Personen zu Architektur, Bautechnik");
    sys2descr.put("31.4", "Bergbau, Hüttentechnik");
    sys2descr.put("31.5", "Energietechnik, Kerntechnik");
    sys2descr.put("31.6", "Maschinenbau");
    sys2descr.put("31.7", "Fahrzeugbau, Fördertechnik, Raumfahrttechnik");
    sys2descr.put("31.8a", "Fertigungstechnik");
    sys2descr.put("31.8b", "Feinwerktechnik");
    sys2descr.put("31.9a", "Elektrotechnik, Elektrische Energietechnik");
    sys2descr.put("31.9b", "Elektronik, Nachrichtentechnik");
    sys2descr.put("31.10", "Verfahrenstechnik, Technische Chemie");
    sys2descr.put("31.11", "Lebensmitteltechnologie");
    sys2descr.put("31.12", "Textiltechnik, Gummi- und Lederverarbeitung");
    sys2descr.put("31.13", "Holzbearbeitung");
    sys2descr.put("31.14", "Papierherstellung, Grafische Technik");
    sys2descr.put("31.15", "Glas, Keramik, Steine und Erden");
    sys2descr.put("31.16", "Militärtechnik");
    sys2descr.put("31.16p", "Personen zu Technik (ohne Architektur, Bautechnik)");
    sys2descr.put("32.1a", "Landwirtschaft allgemein");
    sys2descr.put("32.1b", "Landwirtschaftsgeschichte");
    sys2descr.put("32.2", "Agrarpolitik, Agrarmarkt, Landwirtschaftliche Betriebslehre");
    sys2descr.put("32.3", "Ackerbau");
    sys2descr.put("32.4", "Gartenbau, Obstbau");
    sys2descr.put("32.5", "Phytomedizin");
    sys2descr.put("32.6", "Tierzucht, Tierhaltung");
    sys2descr.put("32.7", "Milchwirtschaft");
    sys2descr.put("32.8", "Forstwirtschaft");
    sys2descr.put("32.9", "Jagd");
    sys2descr.put("32.10", "Fischerei, Fischzucht");
    sys2descr.put("32.10p", "Personen zu Landwirtschaft, Garten");
    sys2descr.put("33.1", "Hauswirtschaft, Körperpflege");
    sys2descr.put("33.2", "Kochen, Backen, Lebens- und Genussmittel, Küchengerät");
    sys2descr.put("33.3", "Mode, Kleidung");
    sys2descr.put("33.3p", "Personen zu Hauswirtschaft, Körperpflege, Mode, Kleidung");
    sys2descr.put("34.1", "Sport (Allgemeines)");
    sys2descr.put("34.2", "Geschichte des Sports");
    sys2descr.put("34.3", "Einzelne Sportarten");
    sys2descr.put("34.3p", "Personen zu Sport");
    sys2descr.put("35", "Spiel, Unterhaltung");
    sys2descr.put("35p", "Personen zu Spiel, Unterhaltung");
    sys2descr.put("36", "Basteln, Handarbeiten, Heimwerken");
    sys2descr.put("36p", "Personen zu Basteln, Handarbeiten, Heimwerken");

  }

  /**
   *
   * @param sys Systematiknummer
   * @return  Verbale Beschreibung oder null, wenn nicht vorhanden
   */
  public static String getBeschreibung(final String sys) {
    return sys2descr.get(sys);
  }

  public static Collection<String> getAlleSystemtiknummern() {
    final Set<String> keySet = sys2descr.keySet();
    return new LinkedHashSet<>(keySet);
  }

  /**
   *
   * @param sys Systematiknummer
   * @return    Systematiknummer vorhanden
   */
  public static boolean systematikVorhanden(final String sys) {
    return sys2descr.containsKey(sys);
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    getAlleSystemtiknummern().forEach(nr ->
    {
      System.out.println(StringUtils.concatenate("\t", nr, getBeschreibung(nr)));
    });

  }

  /**
   * Vergleicht nach der ersten Systematiknummer (die auch null sein kann).
   */
  public final static Comparator<Record> recordSysComparator = new Comparator<Record>() {

    SystematikComparator sysComparator = new SystematikComparator();

    @Override
    public int compare(final Record r1, final Record r2) {
      return sysComparator.compare(GNDUtils.getFirstGNDClassification(r1),
        GNDUtils.getFirstGNDClassification(r2));
    }
  };

}
