package de.uniluebeck.itm.nettyprotocols;

import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;

public class NewlineDecoder extends DelimiterBasedFrameDecoder {

	public static final int DEFAULT_MAX_FRAME_LENGTH = 16*1000;

	public static final boolean DEFAULT_STRIP_NEWLINE = true;

	public NewlineDecoder() {
		this(null, null);
	}

	public NewlineDecoder(Integer maxFrameLength, Boolean stripNewline) {
		super(
				maxFrameLength != null ? maxFrameLength : DEFAULT_MAX_FRAME_LENGTH,
				stripNewline != null ? stripNewline : DEFAULT_STRIP_NEWLINE,
				Delimiters.lineDelimiter()
		);
	}
}
