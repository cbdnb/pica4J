package de.dnb.gnd.parser.tag;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

public class BibTagDBTest {

	static final TagDB TAG_DB = BibTagDB.getDB();

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testGetDB() {
		Collection<Tag> tags = TAG_DB.getAllTags();
		for (Tag tag : tags) {
			if (tag.getAllIndicators().isEmpty()) {
				System.err.println(tag);
				fail();
			}
			String pp = tag.picaPlus;
			if (!pp.equals(pp.trim())) {
				System.err.println(tag);
				fail();
			}
			//			if (pp.contains("/")) {
			//				String tail = pp.substring(pp.indexOf('/') + 1);
			//				int i = Integer.parseInt(tail);
			//			}
			
			if (tag instanceof BibliographicTag) {
				BibliographicTag bTag = (BibliographicTag) tag;
				if (bTag.get1stIndicators().equals(bTag.get2ndIndicators())) {
					System.err.println(tag);
					fail();
				}
			}
		}
		
		tags = TAG_DB.getTagsBetween("5100", "5199");
		assertEquals(80, tags.size());

		tags = TAG_DB.getTagsBetween("5301", "5309");
		assertEquals(9, tags.size());

		tags = TAG_DB.getTagsBetween("5310", "5319");
		assertEquals(10, tags.size());

		tags = TAG_DB.getTagsBetween("5320", "5329");
		assertEquals(1, tags.size());

		tags = TAG_DB.getTagsBetween("5400", "5449");
		assertEquals(25, tags.size());

		tags = TAG_DB.getTagsBetween("5450", "5585");
		assertEquals(9, tags.size());

		tags = TAG_DB.getTagsBetween("5590", "5599");
		assertEquals(10, tags.size());
	}

}
