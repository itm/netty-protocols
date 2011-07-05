package de.uniluebeck.itm.netty.handlerstack.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;


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
            XMLDecoder decoder = new XMLDecoder(bufferInputStream);
            Object object = decoder.readObject();

            if (!object.getClass().equals(expectedClass)) {
                buffer.resetReaderIndex();
                return null;
            }

            return (T) object;
        } catch (Exception e) {
            buffer.resetReaderIndex();
            return null;
        }

    }

    public static ChannelBuffer encode(String serializationHeader, Object object) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            out.write(serializationHeader.getBytes());
        } catch (IOException e) {
           throw new RuntimeException(e);
        }

        final XMLEncoder encoder = new XMLEncoder(out);
        encoder.writeObject(object);
        encoder.flush();
        
        return ChannelBuffers.wrappedBuffer(out.toByteArray());
    }
    
    
    /**
     * Extracts the XML bytes from the received object by removing the {@link this#header} SERIALIZATION_HEADER string. Checks if the
     * received message is a ChannelBuffer, has a minimal length and contains {@link this#header} prefix. If not, null
     * is returned.
     * 
     * @param msg
     *            The received object
     * @return The bytes containing the request as serialized XML or null if the request could not be parsed.
     */
    private static byte[] getWithoutHeader(String headerString, ChannelBuffer msg) {
        if (msg.readableBytes() <= headerString.length())
            return null;

        byte[] byteArray = ChannelBufferTools.readToByteArray(msg);

        String receivedHeader = new String(byteArray, 0, headerString.length());

        if (!headerString.equals(receivedHeader))
            return null;

        byteArray = Arrays.copyOfRange(byteArray, headerString.length(), byteArray.length);
        return byteArray;
    }    
    
}
