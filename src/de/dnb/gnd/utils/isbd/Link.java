package de.dnb.gnd.utils.isbd;

import java.util.Objects;

import de.dnb.basics.Misc;
import de.dnb.basics.applicationComponents.strings.StringUtils;

public class Link {
	@Override
	public int hashCode() {
		return Objects.hash(text, url);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Link other = (Link) obj;
		return Objects.equals(text, other.text) && Objects.equals(url, other.url);
	}

	public final String text;
	public final String url;

	/**
	 * @param text
	 * @param url
	 */
	public Link(final String text, final String url) {
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
	public static Link getLink(final String text, final String url) {
		if (Misc.isUrlAccessible(url)) {
			return new Link(text, url);
		}
		return null;
	}

	@Override
	public String toString() {
		return text + ": " + url;
	}

	public static void main(final String[] args) {
		final String s = StringUtils.readClipboard();
		System.err.println(s);
		System.out.println(Misc.isUrlAccessible(s));
	}

	static public boolean checkUri(final String url) {
//		System.err.println(url);
		final String website = Misc.getWebsite(url);
		return website != null;
	}
}
