package de.dnb.gnd.utils.isbd;

import java.awt.print.PrinterException;
import java.io.IOException;

import de.dnb.basics.Misc;
import de.dnb.basics.applicationComponents.strings.StringUtils;

public class Link {
	public final String text;
	public final String url;

	/**
	 * @param text
	 * @param url
	 */
	public Link(String text, String url) {
		this.text = text;
		this.url = url;
	}

	/**
	 * 
	 * @return Link ohne Formatierung: &gt;a href="url"&lt;link text&gt;/a&lt;
	 */
	String toHTML() {
		return "<a href=\"" + url + "\">" + text + "</a>";
	}

	/**
	 * 
	 * @param text text
	 * @param url  url
	 * @return gültigen Link oder null
	 */
	public static Link getLink(String text, String url) {
		if (checkUri(url))
			return new Link(text, url);
		return null;
	}

	@Override
	public String toString() {
		return text + ": " + url;
	}

	public static void main(final String[] args) {
		final String s = StringUtils.readClipboard();
		System.err.println(s);
		System.out.println(checkUri(s));
	}

	static public boolean checkUri(String url) {	
//		System.err.println(url);
		String website = Misc.getWebsite(url);
		return website != null;
	}
}
