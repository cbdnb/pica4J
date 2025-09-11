package de.dnb.gnd.utils.isbd;

public class Link {
	public String text;
	public String adresse;
	
	@Override
	public String toString() {		
		return text + ": " + adresse;
	}
}
