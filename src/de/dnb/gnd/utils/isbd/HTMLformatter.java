package de.dnb.gnd.utils.isbd;

import de.dnb.gnd.parser.Record;
import de.dnb.gnd.utils.RecordUtils;

public class HTMLformatter {

	private ISBD isbd;

	/**
	 * @param isbd
	 */
	public HTMLformatter(final ISBD isbd) {
		this.isbd = isbd;
	}

	public void setISBD(final ISBD isbd) {
		this.isbd = isbd;
	}

	public String format() {
		// @formatter:off
		String html =
				"<div class=\"sachgruppe\" style=\"text-align:left; float:left;\">" + isbd.sgg()
					+ "</div>\n";
		if(isbd.lc != null) {
			html += "<div class=\"land\" style=\"text-align:right;\">" +
					isbd.lc + "</div>";
		} else {
			html += "<br>";
		}
		html += "<div class=\"uri\" style=\"text-align:left; float:left;\">"
				+ (isbd.zumKatalog!=null?isbd.zumKatalog.toHTML():"")
				+ "</div>\n";
		if(isbd.neNr!=null) {
			html +=	"<div class=\"wvn\" style=\"text-align:right;\">"
					+ isbd.neNr
					+ "</div>\n";
		}
		html += "<br>";
		if(isbd.rswk!=null) {
			html += "<div class=\"sacherschliessung\">SW: "
					+ isbd.rswk
					+ "</div>\n";
		}
		if(isbd.ddc!=null) {
			html +=	"<div class=\"sacherschliessung\">DDC: "
					+ isbd.ddc
					+ "</div>";
		}
		return html;
	}

	public static void main(final String[] args) {
		final Record record = RecordUtils.readFromClip();
		final Builder builder = new Builder();
		final ISBD isbd = builder.build(record);
		final HTMLformatter formatter = new HTMLformatter(isbd);
		System.out.println(formatter.format());

	}

}
