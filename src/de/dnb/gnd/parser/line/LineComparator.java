/**
 *
 */
package de.dnb.gnd.parser.line;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.utils.RecordUtils;

/**
 * @author baumann
 *
 */
public class LineComparator implements Comparator<Line> {

  @Override
  public int compare(final Line l1, final Line l2) {
    final Tag tag1 = l1.getTag();
    final Tag tag2 = l2.getTag();
    if (tag1 != tag2)
      return tag1.compareTo(tag2);
    Collection<Subfield> sub1 = RecordUtils.removeExpansion(l1.getSubfields());
    sub1 = RecordUtils.getRelevantSubfields(tag1, sub1);
    final String ls1 = RecordUtils.toString(tag1, sub1);
    Collection<Subfield> sub2 = RecordUtils.removeExpansion(l2.subfields);
    sub2 = RecordUtils.getRelevantSubfields(tag1, sub2);
    final String ls2 = RecordUtils.toString(tag2, sub2);
    return ls1.compareToIgnoreCase(ls2);
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final TreeSet<Line> lines = new TreeSet<>(new LineComparator());
    final Record record = RecordUtils.readFromClip();
    lines.addAll(record.getLines());
    lines.forEach(System.out::println);

  }

}
