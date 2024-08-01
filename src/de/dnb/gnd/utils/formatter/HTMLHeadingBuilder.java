package de.dnb.gnd.utils.formatter;

import de.dnb.gnd.parser.Record;

/**
 * Implementiert das Strategiemuster. In verschiedenen Kontexten kann
 * eine geeignete Titelzeile erzeugt werden.
 * 
 * @author baumann
 *
 */
public interface HTMLHeadingBuilder {

    String getHeading(Record record);

}
