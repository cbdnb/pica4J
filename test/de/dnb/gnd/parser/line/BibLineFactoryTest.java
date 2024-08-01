package de.dnb.gnd.parser.line;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.TagDB;
import de.dnb.gnd.utils.RecordUtils;

public class BibLineFactoryTest {

  private static final TagDB TAG_DB = BibTagDB.getDB();

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public final void testBestand() throws IllFormattedLineException {
    Line line = LineParser.parse("7133 a", TAG_DB, false);
    assertEquals("7133  |$a|:a", line.toString());

    line = LineParser.parse("8032 #kkk#dddd$xxx", TAG_DB, false);
    final Line line2 = LineParser.parse("8032 #kkk#dddd", TAG_DB, false);
    assertEquals(line, line2);
  }

  @Test
  public final void testFE() throws IllFormattedLineException {
    Line line = LineParser.parse("3010 !185126499!Lottmann, Kathrin", TAG_DB, false);
    assertEquals("3010  |$9|:185126499 |$8|:Lottmann, Kathrin", line.toString());

    line = LineParser.parse(
      "3100 Kernforschungszentrum <Karlsruhe>" + " / Projekt Nukleare Sicherheit", TAG_DB, false);
    assertEquals(
      "3100  |$a|:Kernforschungszentrum |$c|:Karlsruhe" + " |$b|:Projekt Nukleare Sicherheit",
      line.toString());
    line = LineParser.parse("3100 Laboratorium für Aerosolphysik und Filtertechnik"
      + " <Karlsruhe> / Meßstation <Rheinhausen, Bruchsal>", TAG_DB, false);
    assertEquals("3100  |$a|:Laboratorium für Aerosolphysik und Filtertechnik"
      + " |$c|:Karlsruhe |$b|:Meßstation |$x|:Rheinhausen, Bruchsal", line.toString());

    line = LineParser.parse("5050 %21&63+355$Ep$D2011-11-03", TAG_DB, false);
    assertEquals("5050  |$a|:21 |$d|:63 |$m|:355 |$E|:p |$D|:2011-11-03", line.toString());

    line = LineParser.parse("4000 Von der Physik zur Ph$ilosophie / Jörg Fidorra", TAG_DB, false);
    assertEquals("4000  |$a|:Von der Physik zur Ph$ilosophie |$h|:Jörg Fidorra", line.toString());
    assertEquals("021A ƒaVon der Physik zur Ph$ilosophieƒhJörg Fidorra",
      RecordUtils.toPica(line, Format.PICA_PLUS, true, 'ƒ'));
    assertEquals("021A $aVon der Physik zur Ph$$ilosophie$hJörg Fidorra",
      RecordUtils.toPica(line, Format.PICA_PLUS, true, '$'));

    line = LineParser.parse("021A $aVon der Physik zur Ph$$ilosophie$hJörg Fidorra", TAG_DB, false);
    assertEquals("4000  |$a|:Von der Physik zur Ph$ilosophie |$h|:Jörg Fidorra", line.toString());
    assertEquals("021A ƒaVon der Physik zur Ph$ilosophieƒhJörg Fidorra",
      RecordUtils.toPica(line, Format.PICA_PLUS, true, 'ƒ'));
    assertEquals("021A $aVon der Physik zur Ph$$ilosophie$hJörg Fidorra",
      RecordUtils.toPica(line, Format.PICA_PLUS, true, '$'));

    line = LineParser.parse("021A ƒaVon der Physik zur Ph$ilosophieƒhJörg Fidorra", TAG_DB, false);
    assertEquals("4000  |$a|:Von der Physik zur Ph$ilosophie |$h|:Jörg Fidorra", line.toString());
    assertEquals("021A ƒaVon der Physik zur Ph$ilosophieƒhJörg Fidorra",
      RecordUtils.toPica(line, Format.PICA_PLUS, true, 'ƒ'));
    assertEquals("021A $aVon der Physik zur Ph$$ilosophie$hJörg Fidorra",
      RecordUtils.toPica(line, Format.PICA_PLUS, true, '$'));

    line = LineParser.parse("3260 Three lambda", TAG_DB, false);
    assertEquals("3260  |$a|:Three lambda", line.toString());
    assertEquals("027A ƒaThree lambda", RecordUtils.toPica(line, Format.PICA_PLUS, true, 'ƒ'));

    line = LineParser.parse("3260 |b|Zehn kleine Negerlein", TAG_DB, false);
    assertEquals("3260  |$S|:b |$a|:Zehn kleine Negerlein", line.toString());
    assertEquals("027A ƒSbƒaZehn kleine Negerlein",
      RecordUtils.toPica(line, Format.PICA_PLUS, true, 'ƒ'));

    line = LineParser.parse("7120 $d25$j2012/13", TAG_DB, false);
    assertEquals("7120  |$d|:25 |$j|:2012/13", line.toString());

    line = LineParser.parse("3010 !121182584!Tadday, Ulrich$BHrsg.", TAG_DB, false);
    assertEquals("3010  |$9|:121182584 |$8|:Tadday, Ulrich |$B|:Hrsg.", line.toString());
    line = LineParser.parse("3011 !121182584!Tadday, Ulrich$BHrsg.", TAG_DB, false);
    assertEquals("3011  |$9|:121182584 |$8|:Tadday, Ulrich |$B|:Hrsg.", line.toString());
    line = LineParser.parse("3019 !121182584!Tadday, Ulrich$BHrsg.", TAG_DB, false);
    assertEquals("3019  |$9|:121182584 |$8|:Tadday, Ulrich |$B|:Hrsg.", line.toString());
    line = LineParser.parse("3029 !121182584!Tadday, Ulrich$BHrsg.", TAG_DB, false);
    assertEquals("3029  |$9|:121182584 |$8|:Tadday, Ulrich |$B|:Hrsg.", line.toString());
    line = LineParser.parse("3039 !121182584!Tadday, Ulrich$BHrsg.", TAG_DB, false);
    assertEquals("3039  |$9|:121182584 |$8|:Tadday, Ulrich |$B|:Hrsg.", line.toString());
    line = LineParser.parse("3072 !121182584!Tadday, Ulrich$BHrsg.", TAG_DB, false);
    assertEquals("3072  |$9|:121182584 |$8|:Tadday, Ulrich |$B|:Hrsg.", line.toString());
    line = LineParser.parse("3073 !121182584!Tadday, Ulrich$BHrsg.", TAG_DB, false);
    assertNull(line);

    // ohne Link-Nummer
    line = LineParser.parse("3000 Goethe, Johann Wolfgang", TAG_DB, false);
    assertEquals("3000  |$a|:Goethe |$d|:Johann Wolfgang", line.toString());
    line = LineParser.parse("028A ƒdJohann WolfgangƒaGoethe", TAG_DB, false);
    assertEquals("3000  |$a|:Goethe |$d|:Johann Wolfgang", line.toString());
    assertEquals("3000 Goethe, Johann Wolfgang", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    assertEquals("028A $dJohann Wolfgang$aGoethe",
      RecordUtils.toPica(line, Format.PICA_PLUS, true, '$'));
    line = LineParser.parse("028B/01 $dJohann Wolfgang$aGoethe", TAG_DB, false);
    assertEquals("3001  |$a|:Goethe |$d|:Johann Wolfgang", line.toString());

    line = LineParser.parse("3010 Dietze, Anita$BHrsg.", TAG_DB, false);
    assertEquals("3010  |$a|:Dietze |$d|:Anita |$B|:Hrsg.", line.toString());
    line = LineParser.parse("028C ƒdAnitaƒaDietzeƒBHrsg.", TAG_DB, false);
    assertEquals("3010  |$a|:Dietze |$d|:Anita |$B|:Hrsg.", line.toString());
    assertEquals("3010 Dietze, Anita$BHrsg.", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    assertEquals("028C $dAnita$aDietze$BHrsg.",
      RecordUtils.toPica(line, Format.PICA_PLUS, true, '$'));
    line = LineParser.parse("3072 Dietze, Anita$BHrsg.", TAG_DB, false);
    assertEquals("3072  |$a|:Dietze |$d|:Anita |$B|:Hrsg.", line.toString());

    line = LineParser.parse("4021 #ndr#Neudr. [der] 1. Aufl. Berlin, Bard, 1906", TAG_DB, false);
    assertEquals("4021  |$g|:ndr |$a|:Neudr. [der] 1. Aufl. Berlin, Bard, 1906", line.toString());
    line = LineParser.parse("032B ƒgndrƒaNeudr. [der] 1. Aufl. Berlin, Bard, 1906", TAG_DB, false);
    assertEquals("4021  |$g|:ndr |$a|:Neudr. [der] 1. Aufl. Berlin, Bard, 1906", line.toString());

    line = LineParser.parse("3200 S# <mittelhdt.>", TAG_DB, false);
    assertEquals("3200  |$b|:S |$r|:mittelhdt.", line.toString());
    line = LineParser.parse("022S ƒbSƒrmittelhdt.", TAG_DB, false);
    assertEquals("3200  |$b|:S |$r|:mittelhdt.", line.toString());

    line = LineParser.parse("3200 Vertrag <1992.02.07>", TAG_DB, false);
    assertEquals("3200  |$a|:Vertrag |$r|:1992.02.07", line.toString());
    line = LineParser.parse("022S ƒaVertragƒr1992.02.07", TAG_DB, false);
    assertEquals("3200  |$a|:Vertrag |$r|:1992.02.07", line.toString());

    line = LineParser.parse("2000 978-3-8252-8286-8*(UTB) kart.", TAG_DB, false);
    assertEquals("2000  |$0|:978-3-8252-8286-8 |$c|:UTB |$f|: kart.", line.toString());
    line = LineParser.parse("004A ƒ0978-3-8252-8286-8ƒcUTBƒf kart.", TAG_DB, false);
    assertEquals("2000  |$0|:978-3-8252-8286-8 |$c|:UTB |$f|: kart.", line.toString());

    line = LineParser.parse("2000 978-3-219-11464-5*Pp. : EUR 19.95 (DE)", TAG_DB, false);
    assertEquals("2000  |$0|:978-3-219-11464-5 |$f|:Pp. : EUR 19.95 (DE)", line.toString());
    line = LineParser.parse("004A ƒ0978-3-219-11464-5ƒfPp. : EUR 19.95 (DE)", TAG_DB, false);
    assertEquals("2000  |$0|:978-3-219-11464-5 |$f|:Pp. : EUR 19.95 (DE)", line.toString());

    line = LineParser.parse("2000 978-3-8274-2426-6*", TAG_DB, false);
    assertEquals("2000  |$0|:978-3-8274-2426-6", line.toString());
    line = LineParser.parse("004A ƒ0978-3-8274-2426-6", TAG_DB, false);
    assertEquals("2000  |$0|:978-3-8274-2426-6", line.toString());

    line = LineParser.parse("1100 1933$n[19]33", TAG_DB, false);
    assertEquals("1100  |$a|:1933 |$n|:[19]33", line.toString());
    line = LineParser.parse("011@ ƒa1933ƒn[19]33", TAG_DB, false);
    assertEquals("1100  |$a|:1933 |$n|:[19]33", line.toString());

    // $S wurde umdefiniert, daher weg:
    //    line = LineParser.parse(
    //      "4000 Vertrag : Internationaler Halbzeug-Verband ; 18. 7. 33 |a| = Convention", TAG_DB,
    //      false);
    //    assertEquals(
    //      "4000  |$a|:Vertrag |$d|:Internationaler Halbzeug-Verband ; 18. 7. 33 |$S|:a |$f|:Convention",
    //      line.toString());
    //    line = LineParser.parse(
    //      "021A ƒaVertragƒdInternationaler Halbzeug-Verband ; 18. 7. 33ƒSaƒfConvention", TAG_DB, false);
    //    assertEquals(
    //      "4000  |$a|:Vertrag |$d|:Internationaler Halbzeug-Verband ; 18. 7. 33 |$S|:a |$f|:Convention",
    //      line.toString());
    //    assertEquals("4000 Vertrag : Internationaler Halbzeug-Verband ; 18. 7. 33 |a| = Convention",
    //      RecordUtils.toPica(line, Format.PICA3, true, '$'));

    line = LineParser.parse("4030 Bonn ; München ; Paris {[u.a.] : Addison-Wesley", TAG_DB, false);
    assertEquals("4030  |$p|:Bonn |$p|:München |$p|:Paris {[u.a.] |$n|:Addison-Wesley",
      line.toString());
    line = LineParser.parse("033A ƒpBonnƒpMünchenƒpParis {[u.a.]ƒnAddison-Wesley", TAG_DB, false);
    assertEquals("4030  |$p|:Bonn |$p|:München |$p|:Paris {[u.a.] |$n|:Addison-Wesley",
      line.toString());
    assertEquals("4030 Bonn ; München ; Paris {[u.a.] : Addison-Wesley",
      RecordUtils.toPica(line, Format.PICA3, true, '$'));

    line = LineParser.parse("4213 $T01$ULatn%%Zusatz: tvoj stilʹ žizni", TAG_DB, false);
    assertEquals("4213  |$T|:01 |$U|:Latn |$b|:Zusatz |$a|:tvoj stilʹ žizni", line.toString());
    line = LineParser.parse("046D ƒT01ƒULatnƒbZusatzƒatvoj stilʹ žizni", TAG_DB, false);
    assertEquals("4213  |$T|:01 |$U|:Latn |$b|:Zusatz |$a|:tvoj stilʹ žizni", line.toString());

    line = LineParser.parse("4242 Teils Beil.!012834157!--Abvz--: Mathematik alpha", TAG_DB, false);
    assertEquals("4242  |$a|:Teils Beil. |$9|:012834157 |$8|:--Abvz--: Mathematik alpha",
      line.toString());
    line =
      LineParser.parse("039C ƒaTeils Beil.ƒ9012834157ƒ8--Abvz--: Mathematik alpha", TAG_DB, false);
    assertEquals("4242  |$a|:Teils Beil. |$9|:012834157 |$8|:--Abvz--: Mathematik alpha",
      line.toString());

    line = LineParser.parse(
      "4700 wl****Eichstätt : Lehrstuhl für Kulturgeographie, Kath. Univ. Eichstätt", TAG_DB,
      false);
    assertEquals(
      "4700  |$a|:wl |$f|:Eichstätt : Lehrstuhl für Kulturgeographie, Kath. Univ. Eichstätt",
      line.toString());
    line = LineParser.parse(
      "047A ƒawlƒfEichstätt : Lehrstuhl für Kulturgeographie, Kath. Univ. Eichstätt", TAG_DB,
      false);
    assertEquals(
      "4700  |$a|:wl |$f|:Eichstätt : Lehrstuhl für Kulturgeographie, Kath. Univ. Eichstätt",
      line.toString());

    line = LineParser.parse("4700 wl*Eichstätt", TAG_DB, false);
    assertEquals("4700  |$a|:wl |$c|:Eichstätt", line.toString());
    line = LineParser.parse("047A ƒawlƒcEichstätt", TAG_DB, false);
    assertEquals("4700  |$a|:wl |$c|:Eichstätt", line.toString());

    line = LineParser.parse("4700 wl*Eichstätt****Eichs", TAG_DB, false);
    assertEquals("4700  |$a|:wl |$c|:Eichstätt |$f|:Eichs", line.toString());
    line = LineParser.parse("047A ƒawlƒcEichstättƒfEichs", TAG_DB, false);
    assertEquals("4700  |$a|:wl |$c|:Eichstätt |$f|:Eichs", line.toString());

    line = LineParser.parse("1131 !1071861417!Konferenzschrift [Ts1]$y2010$zFallingbostel", TAG_DB,
      false);
    assertEquals("1131  |$9|:1071861417 |$8|:Konferenzschrift [Ts1] |$y|:2010 |$z|:Fallingbostel",
      line.toString());
    line = LineParser.parse("013D ƒ91071861417ƒ8Konferenzschrift [Ts1]ƒy2010ƒzFallingbostel",
      TAG_DB, false);
    assertEquals("1131  |$9|:1071861417 |$8|:Konferenzschrift [Ts1] |$y|:2010 |$z|:Fallingbostel",
      line.toString());

    line = LineParser.parse("1131 r;i", TAG_DB, false);
    assertEquals("1131  |$a|:r |$a|:i", line.toString());
    line = LineParser.parse("013D ƒarƒai", TAG_DB, false);
    assertEquals("1131  |$a|:r |$a|:i", line.toString());

    line = LineParser.parse("1131 {123-4}$yqay", TAG_DB, false);
    assertEquals("1131  |$6|:123-4 |$y|:qay", line.toString());
    line = LineParser.parse("013D ƒ6123-4ƒyqay", TAG_DB, false);
    assertEquals("1131  |$6|:123-4 |$y|:qay", line.toString());
  }

  @Test
  public final void testSE() throws IllFormattedLineException {
    Line line = LineParser.parse("5050 %63+900+920", TAG_DB, false);
    assertEquals("5050  |$a|:63 |$m|:900 |$m|:920", line.toString());

    line = LineParser.parse("5050 900$Ea$Honx$D2011-11-02", TAG_DB, false);
    assertEquals("5050  |$e|:900 |$E|:a |$H|:onx |$D|:2011-11-02", line.toString());

    // wird von IBW akzeptiert:
    line = LineParser.parse("5101 :f Aufsatzsa!04045956X!mmlung", TAG_DB, false);
    assertEquals("5101  |$a|:f Aufsatzsa!04045956X!mmlung", line.toString());

    try {
      line = LineParser.parse("5100 :f Aufsatz", TAG_DB, false);
      // funktioniert nicht mehr, da auch Text in 5100 vorkommen darf
      //            fail();
    } catch (final Exception e) {
      // OK
    }

    line = LineParser.parse("5110 !101886508X!Theon$lSmyrnaeus$aDe utilitate mathematicae", TAG_DB,
      false);
    assertEquals("5110  |$9|:101886508X |$8|:Theon$lSmyrnaeus$aDe utilitate mathematicae",
      line.toString());
    line = LineParser.parse("5111 !101886508X!Theon$lSmyrnaeus$aDe utilitate mathematicae", TAG_DB,
      false);
    assertEquals("5111  |$9|:101886508X |$8|:Theon$lSmyrnaeus$aDe utilitate mathematicae",
      line.toString());

    line = LineParser.parse("041A/11 $9101886508X$8Theon$lSmyrnaeus$aDe utilitate mathematicae",
      TAG_DB, false);
    assertEquals("5111  |$9|:101886508X |$8|:Theon$lSmyrnaeus$aDe utilitate mathematicae",
      line.toString());

    line = LineParser.parse("5540 [GND]!041263936!Profil$gAerodynamik$K0,031$D2013-10-14", TAG_DB,
      false);
    assertEquals(
      "5540  |$b|:GND |$9|:041263936 |$8|:Profil$gAerodynamik |$K|:0,031 |$D|:2013-10-14",
      line.toString());

    line = LineParser.parse("5560 [Produktform]Hardback $ jjj", TAG_DB, false);
    assertEquals("5560  |$b|:Produktform |$a|:Hardback |$a|:jjj", line.toString());
    line = LineParser.parse("044N ƒbProduktformƒaHardbackƒajjj", TAG_DB, false);
    assertEquals("5560  |$b|:Produktform |$a|:Hardback |$a|:jjj", line.toString());
    assertEquals("5560 [Produktform]Hardback $ jjj",
      RecordUtils.toPica(line, Format.PICA3, true, '$'));

    line = LineParser.parse(
      "5560 [lcsh]Goethe, Johann Wolfgang von, 1749-1832.--Werther.--English & German.", TAG_DB,
      false);
    assertEquals(
      "5560  |$b|:lcsh |$a|:Goethe, Johann Wolfgang von, 1749-1832.--Werther.--English & German.",
      line.toString());

    line = LineParser.parse("5593 [Verlag]", TAG_DB, false);
    assertEquals("5593  |$b|:Verlag", line.toString());

    line =
      LineParser.parse("5593 !1004504950!--Tb3--Michael Gottlieb Grießbach Erben", TAG_DB, false);
    assertEquals("5593  |$9|:1004504950 |$8|:--Tb3--Michael Gottlieb Grießbach Erben",
      line.toString());

    line = LineParser.parse("5410 [DDC22ger]738.2709033", TAG_DB, false);
    assertEquals("5410  |$e|:DDC22ger |$a|:738.2709033", line.toString());

    line = LineParser.parse("5411 738.2709", TAG_DB, false);
    assertEquals("5411  |$a|:738.2709", line.toString());

    line = LineParser.parse("5413 -T1--09033", TAG_DB, false);
    assertEquals("5413  |$f|:09033", line.toString());

    line = LineParser.parse("5412 620.1125", TAG_DB, false);
    assertEquals("5412  |$a|:620.1125", line.toString());
    assertEquals("045G/02 ƒa620.1125", RecordUtils.toPica(line, Format.PICA_PLUS, true, 'ƒ'));

    line = LineParser.parse("5530 Italien$G.a.", TAG_DB, false);
    assertEquals("5530  |$a|:Italien |$t|:G.a.", line.toString());

    line =
      LineParser.parse("5530 #Inflation / Länder, Gebiete, Völker // Deutschland", TAG_DB, false);
    assertEquals("5530  |$g|:Inflation |$h|:Länder, Gebiete, Völker // Deutschland",
      line.toString());

    line = LineParser.parse("5530 |p|Ambrosius <von Mailand>", TAG_DB, false);
    assertEquals("5530  |$S|:p |$a|:Ambrosius <von Mailand>", line.toString());

    line = LineParser.parse("5530 }Lebenserinnerungen / Frauen / Einz. Pers. → Buber-Neumann, M.",
      TAG_DB, false);
    assertEquals("5530  |$v|:Lebenserinnerungen / Frauen / Einz. Pers. → Buber-Neumann, M.",
      line.toString());

    line = LineParser.parse("5119 (DE-101){DE-101}|8.4|31.3ab/XA", TAG_DB, false);
    assertEquals("5119  |$e|:DE-101 |$r|:DE-101 |$g|:8.4 |$g|:31.3ab |$h|:XA", line.toString());

    line = LineParser.parse("5560 Atlantikwall $ Norwegenbefestigung $ Dänemarkbefestigung", TAG_DB,
      false);
    assertEquals("5560  |$a|:Atlantikwall |$a|:Norwegenbefestigung |$a|:Dänemarkbefestigung",
      line.toString());

    line = LineParser.parse("5118 $123456$234516$324516$432516$534216", TAG_DB, false);
    assertEquals("5118  |$f|:123456 |$f|:234516 |$f|:324516 |$f|:432516 |$f|:534216",
      line.toString());

    line = LineParser.parse("5450 [rvk]AL 43600 $ AL 17800 $ AL 19700", TAG_DB, false);
    assertEquals("5450  |$b|:rvk |$a|:AL 43600 |$a|:AL 17800 |$a|:AL 19700", line.toString());
    line = LineParser.parse("045Z ƒbrvkƒaAL 43600ƒaAL 17800ƒaAL 19700", TAG_DB, false);
    assertEquals("5450  |$b|:rvk |$a|:AL 43600 |$a|:AL 17800 |$a|:AL 19700", line.toString());

    line = LineParser.parse("5550 [rvk]!948790741!{The Potteries{123}", TAG_DB, false);
    assertEquals("5550  |$b|:rvk |$9|:948790741 |$8|:{The Potteries |$5|:123", line.toString());
    line = LineParser.parse("044K ƒbrvkƒ9948790741ƒ8{The Potteriesƒ5123", TAG_DB, false);
    assertEquals("5550  |$b|:rvk |$9|:948790741 |$8|:{The Potteries |$5|:123", line.toString());

    line = LineParser.parse("5550 [gnd]{4135467-9}", TAG_DB, false);
    assertEquals("5550  |$b|:gnd |$5|:4135467-9", line.toString());
    line = LineParser.parse("044K ƒbgndƒ54135467-9", TAG_DB, false);
    assertEquals("5550  |$b|:gnd |$5|:4135467-9", line.toString());

    line = LineParser.parse("5056 17,1;12", TAG_DB, false);
    assertEquals("5056  |$a|:17,1 |$a|:12", line.toString());
    line = LineParser.parse("045T ƒa17,1ƒa12", TAG_DB, false);
    assertEquals("5056  |$a|:17,1 |$a|:12", line.toString());

    line = LineParser.parse("5530 Jahrhundert, 18. / Allg. Geschichte|16.5/XA-DE", TAG_DB, false);
    assertEquals("5530  |$a|:Jahrhundert, 18. |$f|:Allg. Geschichte |$s|:16.5 |$e|:XA-DE",
      line.toString());

    line = LineParser.parse("5530 Kirchenkampf|16.5|3.3c/XA-DE", TAG_DB, false);
    assertEquals("5530  |$a|:Kirchenkampf |$s|:16.5 |$s|:3.3c |$e|:XA-DE", line.toString());

    line = LineParser.parse("5530 |p|Buber-Neumann, Margarete|2.3p", TAG_DB, false);
    assertEquals("5530  |$S|:p |$a|:Buber-Neumann, Margarete |$s|:2.3p", line.toString());

  }

}
