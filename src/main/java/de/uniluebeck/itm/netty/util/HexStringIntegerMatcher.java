package de.uniluebeck.itm.netty.util;

import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

public class HexStringIntegerMatcher implements Matcher {

	@Override
	public Transform match(final Class type) throws Exception {
		if (type.equals(Integer.class)) {
			return new HexStringIntegerTransform();
		}
		return null;
	}
}
