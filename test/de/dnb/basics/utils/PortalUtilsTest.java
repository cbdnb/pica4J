/**
 * 
 */
package de.dnb.basics.utils;

import static org.junit.Assert.*;

import org.junit.Test;
import org.marc4j.marc.Record;
//import static org.junit.Assert.*;

/**
 * @author baumann
 *
 */
public class PortalUtilsTest {

    // "Auszeichnungssprache"
    public static String idn = "957561504";

    public static String nid = "4561529-9";

    /**
     * Test method for 
     * {@link de.dnb.basics.utils.PortalUtils#getRecordViaPortal(java.lang.String)}.
     */
    @Test
    public void testGetRecordViaPortal() {
        String r = PortalUtils.getRecordViaPortal(idn);
        assertNotNull(r);
        r = PortalUtils.getRecordViaPortal(nid);
        assertNotNull(r);
    }

    /**
     * Test method for 
     * {@link de.dnb.basics.utils.PortalUtils#getDNBturtle(java.lang.String)}.
     */
    @Test
    public void testGetDNBturtle() {
        String r = PortalUtils.getDNBturtle(idn);
        assertNotNull(r);
    }

    /**
     * Test method for 
     * {@link de.dnb.basics.utils.PortalUtils#getDNBrdf(java.lang.String)}.
     */
    @Test
    public void testGetDNBrdf() {
        String r = PortalUtils.getDNBrdf(idn);
        assertNotNull(r);
    }

    /**
     * Test method for 
     * {@link de.dnb.basics.utils.PortalUtils#getMARC21xml(java.lang.String)}.
     */
    @Test
    public void testGetMARC21xml() {
        String r = PortalUtils.getMARC21xml(idn);
        assertNotNull(r);
    }

    /**
     * Test method for 
     * {@link de.dnb.basics.utils.PortalUtils#getMarcRecord(java.lang.String)}.
     */
    @Test
    public void testGetMarcRecord() {
        Record r = PortalUtils.getMarcRecord(idn);
        assertNotNull(r);
    }

    /**
     * Test method for 
     * {@link de.dnb.basics.utils.PortalUtils#getDNBidn(java.lang.String)}.
     */
    @Test
    public void testGetDNBidn() {
        String r = PortalUtils.getDNBidn(idn);
        assertNotNull(r);
        r = PortalUtils.getDNBidn(nid);
        assertNotNull(r);
    }

    /**
     * Test method for 
     * {@link de.dnb.basics.utils.PortalUtils#getDNBuri(java.lang.String)}.
     */
    @Test
    public void testGetDNBuri() {
        String r = PortalUtils.getDNBuri(idn);
        assertNotNull(r);
        r = PortalUtils.getDNBuri(nid);
        assertNotNull(r);
    }

}
