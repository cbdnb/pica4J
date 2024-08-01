package de.dnb.gnd.parser;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.dnb.gnd.parser.tag.GNDTagDB;

public class IndicatorTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testHashCode() {
        Indicator indicator1 = GNDTagDB.getDB().findIndicator("100", 'v');
        assertEquals(indicator1.hashCode(), 'v');
    }

    @Test
    public final void testEqualsObject() {
        Indicator indicator1 = GNDTagDB.getDB().findIndicator("100", 'v');
        Indicator indicator2 = GNDTagDB.getDB().findIndicator("110", 'v');
        assertEquals(indicator1, indicator2);

        indicator2 = GNDTagDB.getDB().findIndicator("100", 'a');
        assertNotSame(indicator1, indicator2);
        
        indicator2 = GNDTagDB.getDB().findIndicator("006", 'v');
        assertNotSame(indicator1, indicator2);
    }

    

}
