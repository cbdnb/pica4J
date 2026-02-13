/**
 *
 *
 */
package de.dnb.gnd.parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.collections.ListMultimap;
import de.dnb.basics.collections.Multimap;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.line.LineParser;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.TagDB;
import de.dnb.gnd.utils.RecordUtils;
import de.dnb.gnd.utils.StatusAndCodeFilter;

/**
 * Erstellt eine Abbildung <blockquote> Bibliothek -> (Exemplar1, Exemplar2,
 * ...) </blockquote> Die Bibliothek ist durch ihre ILN repräsentiert. Werden
 * Pica3-Daten geparst, die die ELN in der Form <blockquote>[0101 ] leipzig dnb
 * [101a]</blockquote> an erster Stelle enthalten so wird die ILN aufgrund einer
 * Konkordanz ermittelt. <br>
 * <br>
 * Die Exemplardaten hängen unten am Datensatz. Eingeleitet werden sie mit einem
 * Marker für die Bibliothek. Entweder in Pica3 <blockquote>[0101 ] leipzig dnb
 * [101a]</blockquote> oder in Pica+ <blockquote>101@ ƒa1</blockquote> Die
 * einzelnen Exemplardatensätze der Bibliothek werden eingeleiten von
 * <code>7001</code> in Pica3 oder von <code>208@/01</code> in Pica+ . <br>
 * <br>
 *
 * @author baumann
 *
 */
public class ItemParser {

	/**
	 * Für schnelle Überprüfungen.
	 */
	private static ItemParser parser = new ItemParser("");
	private static StatusAndCodeFilter imBestand = StatusAndCodeFilter.imBestand();

	private static final String PICA_PLUS_NEW_LIB = "101@ ";
	private static final TagDB THE_TAG_DB = BibTagDB.getDB();
	// [ILN: 12 ELN: 0003] Halle UuLB Sachsen-Anh. [3]
	private static final Pattern NEW_LIB_PAT = Pattern
			.compile("^\\[ILN: (\\d+) ELN: (\\d\\d\\d\\d)\\] " + "(.+)" + "(\\[\\d+\\])?$");
	private static final Pattern PAT_NEW_LINE = Pattern.compile("\n");

	private Record actualItem;
	private String actualLibrary;
	private String idnManifestation;

	private boolean isPicaPlus;
	private Matcher newLibMatcher;
	boolean isNewLib;

	/**
	 *
	 * @param record nicht null
	 * @return Bibliothek -> (Item1, Item2, ...), eventuell leer
	 */
	public Multimap<String, Record> parseItems(final Record record) {
		final String raw = record.getRawData();
		idnManifestation = record.getId();
		return parseItems(raw);
	}

	/**
	 *
	 * @param raw nicht null
	 * @return Bibliothek -> (Item1, Item2, ...), eventuell leer
	 */
	public Multimap<String, Record> parseItems(final String raw) {
		final List<String> lines = Arrays.asList(PAT_NEW_LINE.split(raw));
		return parseItems(lines);
	}

	/**
	 *
	 * @param zeilen nicht null
	 * @return Bibliothek -> (Item1, Item2, ...), eventuell leer
	 */
	public Multimap<String, Record> parseItems(final Collection<String> zeilen) {
		// Globale Variable initialisieren:
		final Multimap<String, Record> lib2Items = new ListMultimap<>();
		isNewLib = false;
		newLibMatcher = null;
		actualItem = null;
		actualLibrary = null;

		zeilen.forEach(zeile -> {
			if (zeile == null) {
				return;
			}
			zeile = zeile.trim();

			// Erkennen und verarbeiten:
			if (isNewLibrary(zeile)) {
				// noch sichern, wenn nötig
				if (actualItem != null) {
					lib2Items.add(actualLibrary, actualItem);
				} else { // Erste Bibliothek mit Exemplardaten entdeckt:
					// Erste Bibliothek mit Exemplardaten entdeckt:
					actualItem = new Record(idnManifestation, THE_TAG_DB);
				}
				setNewLibrary(zeile);
				isNewLib = true;
				return;
			}

			// Noch keine Bibliothek erkannt? Dann sparen wir uns die Mühe des Parsens:
			if (actualLibrary == null) {
				return;
			}

			try {
				final Line line = LineParser.parse(zeile, THE_TAG_DB, true);
				if (line == null) {
					return;
				}
				if (isNewItem(line)) {
					if (isNewLib) {
						isNewLib = false;
					} else {
						// bei neuer Bibliothek wurde schon abgespeichert
						lib2Items.add(actualLibrary, actualItem);
					}
					actualItem = new Record(idnManifestation, THE_TAG_DB);
				}
				actualItem.add(line);

			} catch (final IllFormattedLineException | OperationNotSupportedException | IllegalArgumentException e) {
				return;
			}
		});
		// Noch den letzten hinzufügen (SonarLint meldet hier Quatsch):
		if (actualItem != null) {
			lib2Items.add(actualLibrary, actualItem);
		}

		return lib2Items;
	}

	/**
	 * @param line
	 * @return
	 */
	private boolean isNewItem(final Line line) {
		return line.getTag().pica3.startsWith("E");
	}

	/**
	 * Erkennt, ob eine neue Bibliothek vorliegt und setzt die Flags.
	 *
	 * @param zeile nicht null
	 * @return
	 */
	private boolean isNewLibrary(final String zeile) {
		if (zeile.startsWith(PICA_PLUS_NEW_LIB)) {
			isPicaPlus = true;
			return true;
		}
		newLibMatcher = NEW_LIB_PAT.matcher(zeile);
		if (newLibMatcher.matches()) {
			isPicaPlus = false;
			return true;
		}
		return false;
	}

	/**
	 * @param zeile
	 */
	private void setNewLibrary(final String zeile) {
		if (isPicaPlus) {
			actualLibrary = StringUtils.substring(zeile, PICA_PLUS_NEW_LIB.length() + 2);
			actualLibrary = StringUtils.leftPadding(actualLibrary, 4, '0');
		} else {
			final String eln = newLibMatcher.group(2).trim();
			final String name = newLibMatcher.group(3).trim();
			final String iln = newLibMatcher.group(1).trim();
			// ELN2ILN.get(eln);
			actualLibrary = iln == null ? name : iln;
		}
	}

	public static void main(final String[] args) {
		final Record record = RecordUtils.readFromClip();
		final ItemParser parser = new ItemParser("1234");
		System.out.println(parser.parseItems(record.getRawData()));
	}

	public static void main2(final String[] args) {
		System.out.println(dbString);
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main1(final String[] args) throws IOException {
		final ItemParser parser = new ItemParser("1234");
		final RecordReader reader = RecordReader
				.getMatchingReader("U:/pica-0.15.0-x86_64-pc-windows-gnu/samples.dat.gz");
		reader.forEach(record -> {
			System.out.println("++++++++++++++++++++++++++");
			System.out.println("Datensatz: " + record.getId());

			final Multimap<String, Record> bib2Items = parser.parseItems(record);
			log(bib2Items);
		});

	}

	/**
	 * Gibt die Datenstruktur aus.
	 *
	 * @param bib2Items nicht null
	 */
	public static void log(final Multimap<String, Record> bib2Items) {
		new TreeSet<>(bib2Items.getKeySet()).forEach(bib -> {
			System.out.println("Bibliothek: " + bib);
			final Collection<Record> items = bib2Items.get(bib);
			int i = 1;
			for (final Record item : items) {
				System.out.println("Item " + i);
				System.out.println(item);
				System.out.println("------");
				i++;
			}
		});
	}

	/**
	 * Hilfsfunktion für DNB-Exemplare. Ob die Item-Datensätze überhaupt Daten
	 * enthalten, wird nicht überprüft.
	 *
	 * @param bib2Items nicht null.
	 * @return
	 */
	public static int countDNB(final Multimap<String, Record> bib2Items) {
		int count = bib2Items.getNullSafe("0001").size();
		count += bib2Items.getNullSafe("0002").size();
		return count;
	}

	/**
	 * Hilfsfunktion für DNB-Exemplare. Ob die Item-Datensätze überhaupt Daten
	 * enthalten, wird nicht überprüft.
	 *
	 * @param record nicht null
	 * @return Zahl der DNB-Exemplare
	 */
	public static int countDNB(final Record record) {
		if (!imBestand.test(record)) {
			return 0;
		}
		return countDNB(parser.parseItems(record));
	}

	/**
	 * @param idnManifestation Die idn des bibliographischen Datensatzes, der die
	 *                         Manifestation repräsentiert.
	 */
	public ItemParser(final String idnManifestation) {
		super();
		this.idnManifestation = idnManifestation;
	}

	private static final Map<String, String> ELN2ILN = new HashMap<>();
	private static final Map<String, String> ILN2NAME = new HashMap<>();

	static String dbString = "ELN (Pica3 Anfang)\tILN (Pica+)\tKurzname (lowercase)\tISIL\tSigel (Pica3 Ende)\n"
			+ "0001\t0011\tBerlin SBB\tDE-1\t1+1a+1w\n" + "0003\t0012\tHalle UuLB Sachsen-Anh.\tDE-3\t3\n"
			+ "0004\t0013\tMarburg UB\tDE-4\t4\n" + "0005\t0014\tBonn UuLB\tDE-5\t5\n"
			+ "0006\t0015\tMünster UuLB\tDE-6\t6\n" + "0007\t0016\tGöttingen SuUB\tDE-7\t7\n"
			+ "0008\t0017\tKiel UB\tDE-8\t8\n" + "0009\t0018\tGreifswald UB\tDE-9\t9\n"
			+ "0011\t0019\tBerlin UB HU\tDE-11\t11\n" + "0012\t0020\tMünchen BSB\tDE-12\t12\n"
			+ "0014\t0021\tDresden SLUB\tDE-14\t14\n" + "0015\t0022\tLeipzig UB\tDE-15\t15\n"
			+ "0016\t0023\tHeidelberg UB\tDE-16\t16\n" + "0017\t0024\tDarmstadt LuHSB\tDE-17\t17\n"
			+ "0018\t0025\tHamburg SUB\tDE-18\t18\n" + "0019\t0026\tMünchen UB\tDE-19\t19\n"
			+ "0020\t0027\tWürzburg UB\tDE-20\t20\n" + "0021\t0028\tTübingen UB\tDE-21\t21\n"
			+ "0023\t0029\tWolfenbüttel HAugB\tDE-23\t23\n" + "0024\t0030\tStuttgart Württ. LB\tDE-24\t24\n"
			+ "0025\t0031\tFreiburg UB\tDE-25\t25\n" + "0026\t0032\tGiessen UB\tDE-26\t26\n"
			+ "0027\t0033\tJena UuLB\tDE-27\t27\n" + "0028\t0034\tRostock UB\tDE-28\t28\n"
			+ "0029\t0035\tErlangen UB\tDE-29\t29\n" + "0030\t0036\tFrankfurt/M StuUB\tDE-30\t30\n"
			+ "0031\t0037\tKarlsruhe LB\tDE-31\t31\n" + "0032\t0038\tWeimar HAAB\tDE-32\t32\n"
			+ "0033\t0039\tSchwerin LBMV\tDE-33\t33\n" + "0034\t0040\tKassel GHB\tDE-34\t34\n"
			+ "0035\t0041\tHannover LB\tDE-35\t35\n" + "0036\t0042\tMainz StBi\tDE-36\t36\n"
			+ "0038\t0043\tKöln UuStB\tDE-38\t38\n" + "0043\t0044\tWiesbaden LB\tDE-43\t43\n"
			+ "0045\t0045\tOldenburg LB\tDE-45\t45\n" + "0046\t0046\tBremen StUB\tDE-46\t46\n"
			+ "0051\t0152\tDetmold Lippische LB\tDE-51\t51\n" + "0061\t0047\tDüsseldorf UuLB\tDE-61\t61\n"
			+ "0066\t0048\tFulda HLB\tDE-66\t66\n" + "0068\t0145\tKiel LB\tDE-68\t68\n"
			+ "0077\t0049\tMainz UB\tDE-77\t77\n" + "0082\t0050\tAachen BTH\tDE-82\t82\n"
			+ "0083\t0051\tBerlin UB TU\tDE-83\t83\n" + "0084\t0052\tBraunschweig UB TU\tDE-84\t84\n"
			+ "0089\t0054\tHannover UB/TIB\tDE-89\t89\n" + "0090\t0055\tKarlsruhe KIT-Bibliothek Süd\tDE-90\t90\n"
			+ "0091\t0056\tMünchen TU\tDE-91\t91\n" + "0093\t0057\tStuttgart UB\tDE-93\t93\n"
			+ "0095\t0058\tHannover TierHS\tDE-95\t95\n" + "0100\t0059\tStuttgart UB-Hohenh.\tDE-100\t100\n"
			+ "0101\t0001\tLeipzig DNB\tDE-101a\t101a\n" + "0104\t0060\tClausthal-Z. UB\tDE-104\t104\n"
			+ "0105\t0061\tFreiberg TU BA\tDE-105\t105\n" + "0107\t0062\tSpeyer LB\tDE-107\t107\n"
			+ "0109\t0063\tBerlin ZLB\tDE-109\t109\n" + "0180\t0064\tMannheim UB\tDE-180\t180\n"
			+ "0188\t0065\tBerlin UB FU\tDE-188\t188\n" + "0204\t0066\tBerlin IAI\tDE-204\t204\n"
			+ "0206\t0067\tKiel ZBW\tDE-206\t206\n" + "0212\t0068\tStuttgart Inst. Auslandsbez.\tDE-212\t212\n"
			+ "0282\t0069\tWiesbaden StaBA\tDE-282\t282\n" + "0289\t0070\tUlm UB\tDE-289\t289\n"
			+ "0290\t0071\tDortmund UB\tDE-290\t290\n" + "0291\t0072\tSaarbrücken UuLB\tDE-291\t291\n"
			+ "0292\t0002\tFrankfurt/M DNB\tDE-101b\t101b\n" + "0294\t0073\tBochum UB\tDE-294\t294\n"
			+ "0352\t0074\tKonstanz UB\tDE-352\t352\n" + "0354\t0146\tHannover Med. HS\tDE-354\t354\n"
			+ "0355\t0075\tRegensburg UB\tDE-355\t355\n" + "0361\t0076\tBielefeld UB\tDE-361\t361\n"
			+ "0384\t0077\tAugsburg UB\tDE-384\t384\n" + "0385\t0078\tTrier UB\tDE-385\t385\n"
			+ "0386\t0079\tKaiserslautern UB\tDE-386\t386\n" + "0464\t0080\tDuisburg UB\tDE-464\t464\n"
			+ "0465\t0081\tEssen UB\tDE-465\t465\n" + "0466\t0082\tPaderborn UB\tDE-466\t466\n"
			+ "0467\t0083\tSiegen UB\tDE-467\t467\n" + "0468\t0084\tWuppertal UB\tDE-468\t468\n"
			+ "0473\t0085\tBamberg UB\tDE-473\t473\n" + "0517\t0086\tPotsdam UB\tDE-517\t517\n"
			+ "0521\t0087\tFrankfurt/O UB\tDE-521\t521\n" + "0547\t0088\tErfurt UB\tDE-547\t547\n"
			+ "0700\t0089\tOsnabrück UB\tDE-700\t700\n" + "0703\t0090\tBayreuth UB\tDE-703\t703\n"
			+ "0705\t0091\tHamburg UniBW\tDE-705\t705\n" + "0706\t0162\tMünchen UniBW\tDE-706\t706\n"
			+ "0708\t0092\tHagen FernUB\tDE-708\t708\n" + "0715\t0093\tOldenburg UB\tDE-715\t715\n"
			+ "0739\t0094\tPassau UB\tDE-739\t739\n" + "0743\t0142\tLemgo FH Lippe\tDE-743\t743\n"
			+ "0824\t0095\tEichstätt UB\tDE-824\t824\n" + "0829\t0140\tMönchengladb. FH Niederrhein\tDE-829\t829\n"
			+ "0830\t0096\tHamburg TU\tDE-830\t830\n" + "0832\t0097\tKöln FHB\tDE-832\t832\n"
			+ "0836\t0141\tMünster FH\tDE-836\t836\n" + "0929\t0098\tKoblenz LB\tDE-929\t929\n"
			+ "0969\t0160\tWiesbaden FHB\tDE-969\t969\n" + "0974\t0130\tGiessen-Friedberg FH\tDE-974\t974\n"
			+ "1010\t0139\tGelsenkirchen FH\tDE-1010\t1010\n"
			+ "1044\t0143\tSankt Augustin FH Bonn-Rhein-Sieg\tDE-1044\t1044\n"
			+ "1140\t0001\tLeipzig DNB Formalerschließung Monografien\tDE-101\ta 101a\n"
			+ "1141\t0010\tZR GKD DNB Leipzig\t\t\n" + "1210\t0010\tZR GKD DNB Frankfurt\t\t\n"
			+ "1373\t0174\tHamburg HafenCity\tDE-1373\t1373\n" + "1922\t0150\tZ39.50 Titeldaten\t\t\n"
			+ "1994\t0099\tWeitere Bibliotheken Ausland\t\t\n" + "1995\t0100\tWeitere Bibliotheken I\t\t\n"
			+ "1996\t0101\tWeitere Bibliotheken II\t\t\n" + "1997\t0149\tWeitere Bibliotheken III\t\t\n"
			+ "1998\t0173\tWeitere Bibliotheken IV\t\t\n" + "1999\t0102\tBerlin BGK\t\t\n"
			+ "2001\t0301\tBremen Kuhnke-Archiv Web\tDE-Bre18\tBre 18\n" + "2002\t0302\tWeitere Bibliotheken VI\t\t\n"
			+ "2003\t0303\tMainz Martinus-Bibliothek Web\tDE-Mz2\tMz 2\n" + "2004\t0304\tNürnberg HS Web\tDE-92\t92\n"
			+ "2005\t0305\tBerlin UdK Web\tDE-B170\tB 170\n" + "2006\t0306\tGörlitz Oberlaus. Bibl. Web\tDE-Gl2\tGl 2\n"
			+ "2007\t0307\tMünchen ZI Kunstgeschichte Web\tDE-255\t255\n" + "2008\t0308\tBonn FIZ Bundeswehr\t\t\n"
			+ "2999\t0103\tHamburg Zentrale\t\t\n" + "3000\t0003\tBerlin DNB\tDE-101c\t101c\n"
			+ "3090\t0104\tHildesheim UB\tDE-Hil2\tHil 2\n" + "3100\t0105\tMagdeburg UB\tDE-Ma9\tMa 9\n"
			+ "3110\t0106\tLüneburg UB\tDE-Luen4\tLün 4\n" + "3238\t0107\tBonn F.-Ebert-Stiftung\tDE-Bo133\tBo 133\n"
			+ "3349\t0108\tChemnitz UB\tDE-Ch1\tCh 1\n" + "3400\t0109\tIlmenau UB\tDE-Ilm1\tIlm 1\n"
			+ "3501\t0110\tHamburg ZBW\tDE-206\tH 206 H\n" + "3999\t0111\tGöttingen Zentrale\t\t\n"
			+ "4002\t0112\tKöln ZB Medizin\tDE-38M\t38 M\n" + "4011\t0113\tBerlin SMPK KunstB\tDE-B11\tB 11\n"
			+ "4012\t0114\tBerlin Biol. Bundesanst.\tDE-B85\tB 85\n"
			+ "4013\t0115\tBerlin SenatsB\tDE-109-720\t109/720\n" + "4014\t0116\tKöln SportHS ZB\tDE-Kn41\tKn 41\n"
			+ "4015\t0117\tKöln Erzbischöfl. B.\tDE-Kn28\tKn 28\n" + "4017\t0119\tStuttgart PH Bibl. BAW\t\t\n"
			+ "4018\t0120\tTübingen UB/Neuphil.\tDE-21-108\t21/108\n"
			+ "4019\t0121\tKarlsruhe ForschungsZ\tDE-Ka85\tKa 85\n" + "4020\t0122\tBerlin SAPMO\tDE-B479\tB 479\n"
			+ "4022\t0124\tCottbus TU\tDE-Co1\tCo 1\n"
			+ "4023\t0132\tBochum Stiftung Bibl. d. Ruhrgebiets\tDE-Bm3\tBm 3\n"
			+ "4024\t0133\tBerlin Charite Med. Bibl.\t\t578/1 bis 3\n"
			+ "4025\t0134\tKöln HBZ Elektron. Zeitschr. NRW\tDE-WWW2\tWWW 2\n"
			+ "4026\t0135\tBochum FH\tDE-Bm40\tBm 40\n" + "4027\t0136\tBielefeld FH\tDE-Bi10\tBi 10\n"
			+ "4028\t0137\tAachen FH\tDE-A96\tA 96\n" + "4029\t0138\tDüsseldorf FH\tDE-Due62\tDü 62\n"
			+ "4030\t0144\tDortmund Inst. f. Zeitungsforschung\tDE-Dm11\tDm 11\n"
			+ "4031\t0153\tBerlin DIPF\tDE-B478\tB 478\n" + "4032\t0163\tHeidelberg MPI Recht\tDE-B208\tB 208\n"
			+ "4033\t0165\tZeitungen SAA\t\t\n" + "4034\t0166\tBerlin Diakonisches Werk\tDE-B232\tB 232\n"
			+ "4035\t0169\tHamburg MPI Privatrecht\tDE-B212\tB 212\n"
			+ "4036\t0170\tMünchen MPI Geistiges Eigentum\tDE-M382\tM 382\n"
			+ "4037\t0175\tMarbach Dt. Litraturarchiv\tDE-Mar1\tMar 1\n" + "4038\t0179\tHeidelberg PH\tDE-He76\tHe 76\n"
			+ "4039\t0180\tKunstbibliotheken - Fachverbund\t\t255 Y2 u. Y3 Y7\n"
			+ "4040\t0182\tHerne MOB\tDE-365\t364\n" + "4041\t0184\tWeitere Bibliotheken V\t\t\n"
			+ "4042\t0185\tBerlin ZMO\tDE-B2138\tB 2138\n" + "4050\t0131\tMarburg Herder-Inst.\tDE-Mb50\tMb 50\n"
			+ "4060\t0158\tWien ÖNB\t\tAT-ÖNB\n" + "5998\t0125\tKöln NRW FHs\t\t\n"
			+ "5999\t0126\tKöln NRW-Zentrale\t\t\n" + "6998\t0178\tSenckenberg-Bibliotheken\t\t\n"
			+ "6999\t0127\tFrankfurt HEBIS-Zentrale\t\t\n" + "7998\t0154\tKarlsruhe BWZ\t\t\n"
			+ "7999\t0128\tStuttgart BWZ-Zentrale\t\t\n" + "8001\t0147\tEBSCO-Datenbanken\t\t\n"
			+ "8002\t0148\tWeitere MPI\t\t\n" + "8003\t0151\tProQuest-Datenbanken\t\t\n" + "8004\t0155\tIDA\t\t\n"
			+ "8005\t0157\tÖsterreichische Bibliotheken\t\t\n" + "8006\t0164\tArchiv 3\t\t\n"
			+ "8007\t0168\tNationallizenzen (Paketpaten)\t\t\n"
			+ "8008\t0171\tKonstanz BSZ Elektronische Zeitschr. SWB\t\tWWW 5\n" + "8009\t0172\tOnlinepakete\t\t\n"
			+ "8010\t0176\tSchulungskennung\t\t\n" + "8011\t0177\tGale - Cengage Learning\t\t\n"
			+ "8012\t0181\tEv. Landeskirche Wuerttemberg\t\t\n" + "8333\t0333\tWebprobe\t\t\n"
			+ "8897\t0161\tBVB-Bibl. München u. Oberbayern\t\t\n"
			+ "4062\t0216\tOffenbach/Main, Deutscher Wetterdienst\t\t\n" + "8898\t0167\tBSB Zeitungsprojekt\t\t\n"
			+ "8999\t0129\tMünchen BVB-Zentrale\t\t\n" + "9001\t0010\tZR Titel\t\t\n" + "9002\t0010\tZR GKD SBB\t\t\n"
			+ "9003\t0010\tZR GKD DNB Frankfurt\t\t\n" + "9004\t0010\tZR GKD BSB\t\t\n"
			+ "9005\t0010\tZR GKD Österreich\t\t\n" + "9006\t0010\tRedaktion Bibliotheksdatei\t\t\n"
			+ "9007\t0010\tZDB SBB\t\t\n" + "9008\t0010\tOnline GKD KOBV\t\t\n"
			+ "9009\t0010\tOnline GKD BSZ bis Febr. 06\tOffline\t\n" + "9010\t0010\tOffline GKD BVB\t\t\n"
			+ "9011\t0010\tOffline GKD DNB\t\t\n" + "9012\t0010\tOffline GKD GBV\t\t\n"
			+ "9013\t0010\tOffline GKD HBZ\t\t\n" + "9014\t0010\tOffline GKD HEBIS\t\t\n"
			+ "9015\t0010\tOffline GKD Wien\t\t\n" + "9016\t0010\tOffline Bestände\t\t\n"
			+ "9017\t0010\tZRT Titel Sonderkorrekturen\t\t\n" + "9018\t0010\tLesekennung\t\t\n"
			+ "9019\t0010\tZR GKD DNB Leipzig\t\t\n" + "9020\t0010\tZR GKD DMA Berlin\t\t\n"
			+ "9021\t0010\tRedaktion Österreich\t\t\n" + "9022\t0010\tZRT Titel Sonderkorrekturen\t\t\n"
			+ "9023\t0010\tOrgKat\t\t\n" + "9024\t0010\tVD18\t\t\n"
			+ "2032\t0335\tOberwolfach, Mathematisches Forschungsinstitut\t\t\n"
			+ "4999\t0686\tstuttgart bwz-zentrale i-z\t\t\n"
			+ "2054\t0360\tBerlin, Staatliches Institut für Musikforschung\t\t\n"
			+ "2010\t0310\tHochschulbibliothek Karlsruhe\t\t\n" + "2012\t0312\tberlin bgk web 1\t\t\n"
			+ "2028\t0329\tJülich, Forschungszentrum\t\t\n"
			+ "2022\t0322\tMax-Planck-Institut für Rechtsgeschichte\t\t\n" + "2082\t0384\tPotsdam StuLB\t\t\n"
			+ "9025\t0010\tBerlin Systembetreuung\t\t\n" + "2034\t0337\tMittweida HS\t\t\n"
			+ "2053\t0359\tStadtbibliothek Chemnitz\t\t\n" + "2014\t0314\tKoblenz-Landau UB\t\t\n"
			+ "2043\t0349\tSächsisches Sandesamt ULG\t\t\n" + "2042\t0348\tUmweltbundesamt\t\t\n"
			+ "9026\t0010\tKonstanz BSZ, Sacherschl. SWB\t\t";

	static {
		final String[] lines = dbString.split("\n");
		for (int i = 0; i < lines.length; i++) {
			final String[] fracs = lines[i].split("\t");
			final String eln = fracs[0].trim();
			final String iln = fracs[1].trim();
			final String name = fracs[2].trim();
			ELN2ILN.put(eln, iln);
			ILN2NAME.put(iln, name);
		}
	}

	/**
	 * ILN zu Externer Bibliothsnummer (ELN). Die ELN wird im Pica3-Format, die ILN
	 * im Pica+-Format verwendet.
	 *
	 * @param eln vierstellig mit führenden Nullen
	 * @return Name
	 */
	public static String getILN(final String eln) {
		return ELN2ILN.get(eln);
	}

	/**
	 * ILN zu Externer Bibliothsnummer (ELN). Die ELN wird im Pica3-Format, die ILN
	 * im Pica+-Format verwendet.
	 *
	 * @param eln maximal vierstellig
	 * @return Name
	 */
	public static String getILN(final int eln) {
		final String s = StringUtils.leftPadding(Integer.toString(eln), 4, '0');
		return ELN2ILN.get(s);
	}

	/**
	 * Name zu Interner Bibliothsnummer (ILN). Diese wird im Pica+-Format verwendet.
	 *
	 * @param iln vierstellig mit führenden Nullen
	 * @return Name
	 */
	public static String getName(final String iln) {
		return ILN2NAME.get(iln);
	}

	/**
	 * Name zu Interner Bibliothsnummer (ILN). Diese wird im Pica+-Format verwendet.
	 *
	 * @param iln maximal vierstellig
	 * @return Name
	 */
	public static String getName(final int iln) {
		final String s = StringUtils.leftPadding(Integer.toString(iln), 4, '0');
		return ILN2NAME.get(s);
	}

}
