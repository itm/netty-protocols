package de.uniluebeck.itm.nettyprotocols.util;

import com.google.common.collect.Multimap;

import java.util.Iterator;

public class PropertiesHelper {

	public static Integer getIntFromProperties(final Multimap<String, String> properties, final String key) {

		final Iterator<String> iterator = properties.get(key).iterator();
		if (iterator.hasNext()) {
			String value = iterator.next();
			return value == null ? null : Integer.parseInt(value);
		}
		return null;
	}

	public static Boolean getBooleanFromProperties(final Multimap<String, String> properties,
												   final String key) {

		final Iterator<String> iterator = properties.get(key).iterator();
		if (iterator.hasNext()) {
			String value = iterator.next();
			return value == null ? null : Boolean.parseBoolean(value);
		}
		return null;
	}
}
