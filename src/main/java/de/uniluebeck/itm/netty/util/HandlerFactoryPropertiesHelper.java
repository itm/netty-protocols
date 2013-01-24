package de.uniluebeck.itm.netty.util;

import com.google.common.collect.Multimap;

import java.util.concurrent.TimeUnit;

public class HandlerFactoryPropertiesHelper {

	public static String getFirstValueOf(Multimap<String, String> properties, String name, String defaultValue) {
		if (properties.containsKey(name)) {
			return properties.get(name).iterator().next();
		} else {
			return defaultValue;
		}
	}

	public static long getFirstValueOf(Multimap<String, String> properties, String name, long defaultValue) {
		return Long.parseLong(getFirstValueOf(properties, name, "" + defaultValue));
	}

	public static int getFirstValueOf(Multimap<String, String> properties, String name, int defaultValue) {
		return Integer.parseInt(getFirstValueOf(properties, name, "" + defaultValue));
	}

	public static short getFirstValueOf(Multimap<String, String> properties, String name, short defaultValue) {
		return Short.parseShort(getFirstValueOf(properties, name, "" + defaultValue));
	}

	public static TimeUnit getFirstValueOf(Multimap<String, String> properties, String name, TimeUnit defaultValue) {
		return TimeUnit.valueOf(getFirstValueOf(properties, name, defaultValue.toString()));
	}

	public static boolean getFirstValueOf(final Multimap<String, String> properties, final String name,
										  final boolean defaultValue) {
		return Boolean.parseBoolean(getFirstValueOf(properties, name, "" + defaultValue));
	}
}
