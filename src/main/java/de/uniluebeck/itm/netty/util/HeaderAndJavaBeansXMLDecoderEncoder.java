package de.uniluebeck.itm.netty.util;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;


public abstract class HeaderAndJavaBeansXMLDecoderEncoder {

	@SuppressWarnings("unchecked")
	public static <T> T decode(String expectedHeader, Class<T> expectedClass, ChannelBuffer buffer) {
		buffer.markReaderIndex();

		byte[] requestBytes = getWithoutHeader(expectedHeader, buffer);

		if (requestBytes == null) {
			buffer.resetReaderIndex();
			return null;
		}

		try {

			ByteArrayInputStream bufferInputStream = new ByteArrayInputStream(requestBytes);
			Serializer serializer = new Persister();
			return serializer.read(expectedClass, bufferInputStream);

		} catch (Exception e) {
			buffer.resetReaderIndex();
			return null;
		}

	}

	public static ChannelBuffer encode(String serializationHeader, Object object) {

		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {

			out.write(serializationHeader.getBytes());

			Serializer serializer = new Persister();
			serializer.write(object, out);

			return ChannelBuffers.wrappedBuffer(out.toByteArray());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}


	/**
	 * Extracts the XML bytes from the received object by removing the {@link this#header} SERIALIZATION_HEADER string.
	 * Checks if the received message is a ChannelBuffer, has a minimal length and contains {@link this#header} prefix.
	 * If not, null is returned.
	 *
	 * @param msg
	 * 		The received object
	 *
	 * @return The bytes containing the request as serialized XML or null if the request could not be parsed.
	 */
	private static byte[] getWithoutHeader(String headerString, ChannelBuffer msg) {
		if (msg.readableBytes() <= headerString.length()) {
			return null;
		}

		byte[] byteArray = ChannelBufferTools.readToByteArray(msg);

		String receivedHeader = new String(byteArray, 0, headerString.length());

		if (!headerString.equals(receivedHeader)) {
			return null;
		}

		byteArray = Arrays.copyOfRange(byteArray, headerString.length(), byteArray.length);
		return byteArray;
	}

}
