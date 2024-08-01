package de.dnb.basics.applicationComponents;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Beispielklasse zum einfacheren Zugriff auf Datenbankverbindungseinstellungen
 * <br>
 * <b>Auflage 2.</b>
 * 
 * @author Michael Inden
 * 
 * Copyright 2012 by Michael Inden 
 */
public final class DbProperties {
	private static final String URL = "db.url";
	private static final String USER_NAME = "db.username";
	private static final String PASSWORD = "db.password";

	private static final String[] REQUIRED_KEYS = { URL, USER_NAME,
		PASSWORD };

	private final String filePath;
	private final Properties properties = new Properties();

	public DbProperties(String filename) {
		final File file = new File(filename);
		filePath = file.getAbsolutePath();

		try (final InputStream inputStream = new FileInputStream(file)) {
			properties.load(inputStream);

			ensureAllKeysAvailable();
		} catch (final IOException ioException) {
			throw new IllegalStateException("problems while accessing "
				+ "db config file '" + filePath + "'", ioException);

			// Ressourcen werden automatisch durch ARM wieder freigegeben  
		}
	}

	private void ensureAllKeysAvailable() {
		final SortedSet<String> missingKeys = new TreeSet<String>();
		for (final String key : REQUIRED_KEYS) {
			if (!properties.containsKey(key)) {
				missingKeys.add(key);
			}
		}

		if (!missingKeys.isEmpty()) {
			throw new IllegalStateException("db config file '" + filePath
				+ "' " + "is incomplete! Missing keys: " + missingKeys);
		}
	}

	public String getUrl() {
		final String url = properties.getProperty(URL, "");
		if (url.isEmpty()) {
			throw new IllegalStateException("db config file '" + filePath
				+ "' is incomplete! " + "Missing value for key: '" + URL
				+ "'");
		}
		return url;
	}

	public String getUserName() {
		return properties.getProperty(USER_NAME, "");
	}

	public String getPassword() {
		return properties.getProperty(PASSWORD, "");
	}
}
