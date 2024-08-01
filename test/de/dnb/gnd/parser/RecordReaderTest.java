/**
 *
 */
package de.dnb.gnd.parser;

import static org.junit.Assert.*;

import org.junit.Test;

import de.dnb.basics.Constants;
import de.dnb.basics.applicationComponents.FileUtils;
import de.dnb.basics.applicationComponents.strings.StringInputStream;
import de.dnb.gnd.utils.RecordUtils;

/**
 * @author baumann
 *
 */
public class RecordReaderTest {

  /**
   * Test method for
   * {@link
   * de.dnb.gnd.parser.RecordReader#setRecordDelimiter(java.lang.String)}.
   */
  @Test
  public void testSetRecordDelimiter() {
    //@formatter:off
        String s =                  "001A " +
                        Constants.US + "01250:01-07-88" +
                    Constants.RS +  "001B " +
                        Constants.US + "00832:06-01-10" +
                        Constants.US + "t12:56:50.000";
        //@formatter:on
    s = s + "\n" + s;
    final StringInputStream inputStream = new StringInputStream(s);
    final RecordReader recordReader = new RecordReader(inputStream);
    recordReader.gzipSettings();
    recordReader.forEach(rec ->
    {
      assertTrue(RecordUtils.containsField(rec, "0200"));
    });
    FileUtils.safeClose(recordReader);
  }

}
