package de.uniluebeck.itm.nettyprotocols.isense.otap;


import com.coalesenses.tools.iSenseAes128BitKey;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

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
