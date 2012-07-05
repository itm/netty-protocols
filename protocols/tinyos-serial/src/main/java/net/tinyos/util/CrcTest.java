package net.tinyos.util;

import com.google.common.base.Joiner;
import de.uniluebeck.itm.tr.util.StringUtils;

import static net.tinyos.util.Crc.calc;

public class CrcTest {

	public static void main(String[] args) {
		final byte[] ia = StringUtils.fromStringToByteArray(Joiner.on(" ").join(args));
		System.out.println(StringUtils.toHexString(calc(ia, ia.length)));
	}

}
