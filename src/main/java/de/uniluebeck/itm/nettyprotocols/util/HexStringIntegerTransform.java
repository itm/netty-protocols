package de.uniluebeck.itm.nettyprotocols.util;

import de.uniluebeck.itm.tr.util.StringUtils;
import org.simpleframework.xml.transform.Transform;

public class HexStringIntegerTransform implements Transform<Integer> {

	@Override
	public Integer read(final String value) throws Exception {
		return (int) StringUtils.fromStringToLong(value.trim());
	}

	@Override
	public String write(final Integer value) throws Exception {
		return StringUtils.toHexString(value).trim();
	}
}
