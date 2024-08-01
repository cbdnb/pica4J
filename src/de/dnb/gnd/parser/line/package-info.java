package de.dnb.gnd.parser.line;

/**
 * 
 * Als Muster werden Abstract Factory und Static Factory verwendet.
 * 
 * Die Abstrakte Fabrik ist {@link de.dnb.gnd.parser.line.GNDLineFactory}, 
 * deren Erzeugungsmethode ist createLine().
 * Implementierungen sind bisher:
 * 	- {@link de.dnb.gnd.parser.line.DefaultLineFactory}
 * 	- {@link de.dnb.gnd.parser.line.EnumeratingLineFactory}
 * 	- {@link de.dnb.gnd.parser.line.PersonLineFactory}
 * 
 * Das abstrakte Produkt ist {@link de.dnb.gnd.parser.line.Line}.
 * Implementierungen sind:
 * 	- {@link de.dnb.gnd.parser.line.DefaultGNDLine}
 * 	- {@link de.dnb.gnd.parser.line.EnumeratingLine}
 *  - {@link de.dnb.gnd.parser.line.PersonLine}
 * 
 * Da der Anwender nicht weiss, welche konkrete Fabrik er im 
 * Einzelfall auswählen muss, gibt es die Klasse 
 * {@link de.dnb.gnd.parser.line.LineParser} mit
 * einigen statischen Fabrikmethoden, die alle parse(...) heißen.
 * Die richtige Fabrik erhält man mit der Methodenfamilie getFactory(...)
 * 
 * 
 * @author baumann
 */

