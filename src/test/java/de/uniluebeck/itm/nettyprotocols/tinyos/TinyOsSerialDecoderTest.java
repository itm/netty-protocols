package de.uniluebeck.itm.nettyprotocols.tinyos;

import de.uniluebeck.itm.util.logging.LogLevel;
import de.uniluebeck.itm.util.logging.Logging;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TinyOsSerialDecoderTest implements TinyOsSerialTestConstants {

    static {
        Logging.setLoggingDefaults(LogLevel.WARN);
    }

    private TinyOsSerialDecoder decoderTinyOs;

    @Before
    public void setUp() throws Exception {
        decoderTinyOs = new TinyOsSerialDecoder();
    }

    @After
    public void tearDown() throws Exception {
        decoderTinyOs = null;
    }

    @Test
    public void testPacket1() throws Exception {
        Object actual = decoderTinyOs.decode(null, null, ENCODED_PACKET_1);
        assertEquals(DECODED_PACKET_1.toString() + " is not equal to " + actual, DECODED_PACKET_1, actual);
    }

    @Test
    public void testPacket2() throws Exception {
        Object actual = decoderTinyOs.decode(null, null, ENCODED_PACKET_2);
        assertEquals(DECODED_PACKET_2.toString() + " is not equal to " + actual, DECODED_PACKET_2, actual);
    }

    @Test
    public void testPacket3() throws Exception {
        Object actual = decoderTinyOs.decode(null, null, ENCODED_PACKET_3);
        assertEquals(DECODED_PACKET_3.toString() + " is not equal to " + actual, DECODED_PACKET_3, actual);
    }

    @Test
    public void testPacket4() throws Exception {
        Object actual = decoderTinyOs.decode(null, null, ENCODED_PACKET_4);
        assertEquals(DECODED_PACKET_4.toString() + " is not equal to " + actual, DECODED_PACKET_4, actual);
    }

    @Test
    public void testCrcInvalidPacket1() throws Exception {
        assertNull(decoderTinyOs.decode(null, null, CRC_INVALID_ENCODED_PACKET_1));
    }

    @Test
    public void testCrcInvalidPacket2() throws Exception {
        assertNull(decoderTinyOs.decode(null, null, CRC_INVALID_ENCODED_PACKET_2));
    }

    @Test
    public void testCrcInvalidPacket3() throws Exception {
        assertNull(decoderTinyOs.decode(null, null, CRC_INVALID_ENCODED_PACKET_3));
    }

    @Test
    public void testCrcInvalidPacket4() throws Exception {
        assertNull(decoderTinyOs.decode(null, null, CRC_INVALID_ENCODED_PACKET_4));
    }
}
