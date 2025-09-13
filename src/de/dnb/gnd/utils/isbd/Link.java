package de.dnb.gnd.utils.isbd;

import de.dnb.basics.Misc;

public class Link {
	public final String text;
	public final  String adresse;
	/**
	 * @param text
	 * @param adresse
	 */
	public Link(String text, String adresse) {
		this.text = text;
		this.adresse = adresse;
	}

	
	
	public static Link getLink(String text, String adresse) {
		if(checkUri(adresse))
			return new Link(text, adresse);
		return null;
	}
	
	@Override
	public String toString() {		
		return text + ": " + adresse;
	}



	static public boolean checkUri(String uri) {
		return Misc.getWebsite(uri) != null;
	}
}
