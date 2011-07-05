package de.uniluebeck.itm.netty.handlerstack.isenseotap;


import com.coalesenses.tools.iSenseAes128BitKey;
import com.google.common.collect.Sets;
import de.uniluebeck.itm.netty.handlerstack.util.HexStringIntegerMatcher;
import de.uniluebeck.itm.netty.handlerstack.util.HexStringIntegerTransform;
import de.uniluebeck.itm.tr.util.StringUtils;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.TreeStrategy;
import org.simpleframework.xml.strategy.Type;
import org.simpleframework.xml.strategy.Visitor;
import org.simpleframework.xml.strategy.VisitorStrategy;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.NodeMap;
import org.simpleframework.xml.stream.OutputNode;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;

public class ISenseOtapAutomatedProgrammingRequestTest {

	@Test
	public void testSerializationDeserialization() throws Exception {

		Serializer serializer = new Persister();

		ISenseOtapAutomatedProgrammingRequest request =
				new ISenseOtapAutomatedProgrammingRequest(
						Sets.newHashSet(1, 2, 3),
						new byte[]{
								1, 2, 3
						}
				);
		request.setAesKeyFromISenseAes128BitKey(
				new iSenseAes128BitKey(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15})
		);
		request.setMaxRerequests((short) 1);
		request.setOtapInitTimeout(2);
		request.setPresenceDetectTimeout(3);
		request.setProgrammingTimeout(4);
		request.setTimeoutMultiplier((short) 5);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		serializer.write(request, outputStream);

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		ISenseOtapAutomatedProgrammingRequest requestRead = serializer.read(
				ISenseOtapAutomatedProgrammingRequest.class,
				inputStream
		);

		assertEquals(request, requestRead);
	}
}
