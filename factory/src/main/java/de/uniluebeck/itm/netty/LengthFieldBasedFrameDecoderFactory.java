package de.uniluebeck.itm.netty;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class LengthFieldBasedFrameDecoderFactory implements HandlerFactory {

	private static final String MAX_FRAME_LENGTH = "maxFrameLength";

	private static final String LENGTH_FIELD_OFFSET = "lengthFieldOffset";

	private static final String LENGTH_FIELD_LENGTH = "lengthFieldLength";

	private static final String LENGTH_ADJUSTMENT = "lengthAdjustment";

	private static final String INITIAL_BYTES_TO_STRIP = "initialBytesToStrip";

	@Override
	public List<Tuple<String, ChannelHandler>> create(@Nullable final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		int maxFrameLength = Util.getIntFromProperties(properties, MAX_FRAME_LENGTH);
		int lengthFieldOffset = Util.getIntFromProperties(properties, LENGTH_FIELD_OFFSET);
		int lengthFieldLength = Util.getIntFromProperties(properties, LENGTH_FIELD_LENGTH);

		Integer lengthAdjustment = Util.getIntFromProperties(properties, LENGTH_ADJUSTMENT);
		Integer initialBytesToStrip = Util.getIntFromProperties(properties, INITIAL_BYTES_TO_STRIP);

		boolean invalidOptionalProperties = (lengthAdjustment == null && initialBytesToStrip != null) ||
				(lengthAdjustment != null && initialBytesToStrip == null);

		if (invalidOptionalProperties) {
			throw new RuntimeException(
					"The properties \"" + LENGTH_ADJUSTMENT + "\" and \"" + INITIAL_BYTES_TO_STRIP + "\" must both"
							+ " either be null or non-null."
			);
		}

		boolean optionalPropertiesProvided = lengthAdjustment != null && initialBytesToStrip != null;

		LengthFieldBasedFrameDecoder decoder;
		if (optionalPropertiesProvided) {
			decoder = new LengthFieldBasedFrameDecoder(
					maxFrameLength,
					lengthFieldOffset,
					lengthFieldLength,
					lengthAdjustment,
					initialBytesToStrip
			);
		} else {
			decoder = new LengthFieldBasedFrameDecoder(
					maxFrameLength,
					lengthFieldOffset,
					lengthFieldLength
			);
		}

		return newArrayList(new Tuple<String, ChannelHandler>(instanceName, decoder));
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> map = HashMultimap.create();
		map.put(MAX_FRAME_LENGTH,
				"(int) the maximum length of the frame. If the length of the frame is greater than this value, TooLongFrameException will be thrown."
		);
		map.put(LENGTH_FIELD_OFFSET, "(int) the offset of the length field");
		map.put(LENGTH_FIELD_LENGTH, "(int) the length of the length field");
		map.put(LENGTH_ADJUSTMENT, "(int, optional, default=0) the compensation value to add to the value of the length field");
		map.put(INITIAL_BYTES_TO_STRIP, "(int, optional, default=0) the number of first bytes to strip out from the decoded frame");
		return map;
	}

	@Override
	public String getDescription() {
		return "A decoder that splits the received ChannelBuffers dynamically by the value of the length field in "
				+ "the message. See http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/frame/LengthFieldBasedFrameDecoder.html.";
	}

	@Override
	public String getName() {
		return "length-field-based-frame-decoder";
	}
}
