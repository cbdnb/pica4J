package de.dnb.gnd;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.dnb.basics.applicationComponents.StringUtilsTest;
import de.dnb.basics.applicationComponents.collections.TrieTest;
import de.dnb.basics.filtering.BetweenTest;
import de.dnb.basics.utils.PortalUtilsTest;
import de.dnb.basics.utils.TimeUtilsTest;
import de.dnb.gnd.parser.FieldTest;
import de.dnb.gnd.parser.IndicatorTest;
import de.dnb.gnd.parser.RecordParserTest;
import de.dnb.gnd.parser.RecordReaderTest;
import de.dnb.gnd.parser.RecordTest;
import de.dnb.gnd.parser.SubfieldTest;
import de.dnb.gnd.parser.TagDBTest;
import de.dnb.gnd.parser.TagTest;
import de.dnb.gnd.parser.line.BibLineFactoryTest;
import de.dnb.gnd.parser.line.LineFactoryTest;
import de.dnb.gnd.parser.line.LineParserTest;
import de.dnb.gnd.parser.line.LineTest;
import de.dnb.gnd.parser.tag.BibTagDBTest;
import de.dnb.gnd.parser.tag.HoldingsTagTest;
import de.dnb.gnd.utils.GNDUtilsTest;
import de.dnb.gnd.utils.IDNUtilsTest;
import de.dnb.gnd.utils.mx.MxTest;

@RunWith(Suite.class)
@SuiteClasses({ TagTest.class, TagDBTest.class, IndicatorTest.class, SubfieldTest.class,
  LineParserTest.class, RecordParserTest.class, RecordTest.class, GNDUtilsTest.class,
  LineTest.class, FieldTest.class, LineFactoryTest.class, BibLineFactoryTest.class,
  BibTagDBTest.class, HoldingsTagTest.class, StringUtilsTest.class, TrieTest.class,
  RecordReaderTest.class, PortalUtilsTest.class, MxTest.class, TimeUtilsTest.class,
  BetweenTest.class, IDNUtilsTest.class

})
public class AllTests {

}
