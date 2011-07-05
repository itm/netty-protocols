package de.uniluebeck.itm.netty.handlerstack.isenseotap.program;

import com.coalesenses.tools.iSenseAes128BitKey;
import com.google.common.collect.Sets;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.ISenseOtapAutomatedProgrammingRequest;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.program.ISenseOtapProgramResult;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;

public class ISenseOtapProgramResultTest {

	@Test
	public void testSerializationDeserialization() throws Exception {

		Serializer serializer = new Persister();

		ISenseOtapProgramResult result = new ISenseOtapProgramResult(newHashSet(1,2,3));
		result.addDoneDevice(1);
		result.addFailedDevice(2);
		result.addFailedDevice(3);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		serializer.write(result, outputStream);

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		final ISenseOtapProgramResult resultRead = serializer.read(ISenseOtapProgramResult.class, inputStream);

		assertEquals(result, resultRead);
	}

}
