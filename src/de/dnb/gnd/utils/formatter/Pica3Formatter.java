package de.dnb.gnd.utils.formatter;

import java.util.Collection;

import de.dnb.basics.utils.HTMLEntities;
import de.dnb.basics.utils.HTMLUtils;
import de.dnb.basics.utils.OutputUtils;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.utils.RecordUtils;

public class Pica3Formatter extends AbstractFormatter {

	@Override
	protected String format(final Subfield subfield) {
		if (printNoDollar()) {
			String s = subfieldPre;
			// erstes Unterfeld ohne Indikator?
			if (isFirstSubfield && actualIndicator == actualTag.getDefaultFirst()) {
				s += subfield.getContent();
			}
			// Prä- und Postfix:
			else if (actualIndicator.prefix != null) {
				String prefix;
				/*
				 * Alternatives Präfix wird benutzt, wenn Indikator nicht an erster Stelle und
				 * nicht anschliessend ist:
				 */
				if (!isFirstSubfield && actualIndicator.prefixAlt != null && !actualIndicator.isAttaching) {
					prefix = actualIndicator.prefixAlt;
				} else if (isFirstAttaching && actualIndicator.prefixAlt != null) {
					prefix = actualIndicator.prefix;
					isFirstAttaching = false;
				} else if (!isFirstAttaching && actualIndicator.prefixAlt != null) {
					prefix = actualIndicator.prefixAlt;
				} else {
					prefix = actualIndicator.prefix;
				}
				// postfix immer != null:
				s += prefix + subfield.getContent() + actualIndicator.postfix;
			}
			s += subfieldPost;
			return s;
		} else
			// Standardverhalten für GND, also $<indikator>:
			return super.format(subfield);
	}

	/**
	 * @return
	 */
	private boolean printNoDollar() {
		return (isFirstSubfield && actualIndicator == actualTag.getDefaultFirst()) || actualIndicator.prefix != null;
	}

	/**
	 * @param record nicht null
	 *
	 * @return Eine schlichte HTML-Darstellung des Pica-3-Formats, die z.B. in
	 *         Tabellen verarbeitet werden kann. $. wird farblich hervorgehoben, die
	 *         1XX-Zeile und alle Tags fett.
	 */
	public static String toHTML(final Record record) {
		final Pica3Formatter formatter = new Pica3Formatter();
		String txt = formatter.format(record);
		txt = HTMLEntities.allCharacters(txt);

		txt = txt.replaceAll("\\d{3,4} ", "<b>$0</b>");
		if (RecordUtils.isAuthority(record)) {
			txt = txt.replaceAll("1\\d\\d .+", "<b>$0</b>");
		}
		txt = txt.replace("\n", "<br>");
		txt = txt.replaceAll("(\\$.)", "<font color=\"#CC3300\"><b>$1</b></font>");

		return txt;
	}

	/**
	 * 
	 * @param records	nicht null
	 * @param heading	Überschrift und Fenstertitel in einem.
	 * @return	html-Darstellung der Datensätze
	 */
	public static String toHTML(Collection<Record> records, String heading) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
		buffer.append("\n");
		buffer.append("<head>");
		buffer.append("\n");
		buffer.append("<title>" + heading + "</title>");
		buffer.append("\n");
		buffer.append("</head>");
		buffer.append("\n");
		buffer.append("<body>");
		buffer.append("\n");
		if (heading != null) {
			buffer.append(HTMLUtils.heading(heading, 4));
			buffer.append("\n");
			buffer.append("<br>");
			buffer.append("\n");
		}
		records.forEach(rec -> {
			buffer.append(Pica3Formatter.toHTML(rec));
			buffer.append("\n");
			buffer.append("<br><br><br>");
			buffer.append("\n");
		});
		buffer.append("</body>");
		buffer.append("\n");
		buffer.append("</html>");
		buffer.append("\n");
		return buffer.toString();
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final Record record = RecordUtils.readFromClip();
		final String html = toHTML(record);

		OutputUtils.show(html);
	}

}
