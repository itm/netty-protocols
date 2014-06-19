/**
 * Copyright (c) 2010, Daniel Bimschas and Dennis Pfisterer, Institute of Telematics, University of Luebeck
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uniluebeck.itm.nettyprotocols.remoteuart;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.nettyprotocols.ChannelHandlerConfig;
import de.uniluebeck.itm.nettyprotocols.HandlerFactory;
import de.uniluebeck.itm.nettyprotocols.NamedChannelHandler;
import de.uniluebeck.itm.nettyprotocols.NamedChannelHandlerList;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.google.common.base.Throwables.propagate;

public class RUPPacketEncoder extends SimpleChannelDownstreamHandler {

	private static final Logger log = LoggerFactory.getLogger(RUPPacketEncoder.class);

	private static class Fragmenter {

		/**
		 * An encoder chain that may e.g. wrap the payload of a packet to encode with e.g. DLE STX ... DLE ETX.
		 */
		private EncoderEmbedder<ChannelBuffer> encoder;

		/**
		 * The maximum size of a fragment payload (i.e. excluding headers).
		 */
		private final int maximumFragmentPayloadSize;

		/**
		 * The last sequence number that was produced by this fragmenter.
		 */
		private byte lastSequenceNumber = 0;

		public Fragmenter(final int maximumFragmentPayloadSize,
						  final ChannelDownstreamHandler[] channelDownstreamHandlers) {

			this.maximumFragmentPayloadSize = maximumFragmentPayloadSize;
			if (channelDownstreamHandlers != null && channelDownstreamHandlers.length > 0) {
				this.encoder = new EncoderEmbedder<ChannelBuffer>(channelDownstreamHandlers);
			}
		}

		/**
		 * Fragments {@code packet} into fragments (i.e. packets of type {@link RUPFragment}.
		 *
		 * @param packet
		 * 		the packet to fragment
		 *
		 * @return a {@link List} of fragments
		 */
		public List<RUPFragment> fragment(RUPPacket packet) {

			final ChannelBuffer encodedPayload;
			if (encoder != null) {
				encoder.offer(packet.getPayload());
				encodedPayload = encoder.poll();
			} else {
				encodedPayload = packet.getPayload();
			}

			int payloadBytesRemaining = encodedPayload.readableBytes();
			int payloadBytesWritten = 0;

			ArrayList<RUPFragment> fragments = Lists.newArrayList();

			while (payloadBytesRemaining > 0) {

				int bytesToWrite = payloadBytesRemaining < maximumFragmentPayloadSize ? payloadBytesRemaining :
						maximumFragmentPayloadSize;

				fragments.add(RUPFragmentFactory.create(
						packet.getCmdType(),
						getNextSequenceNumber(),
						packet.getDestination(),
						packet.getSource(),
						encodedPayload.slice(payloadBytesWritten, bytesToWrite)
				)
				);

				payloadBytesWritten += bytesToWrite;
				payloadBytesRemaining -= bytesToWrite;
			}

			return fragments;


		}

		private byte getNextSequenceNumber() {
			lastSequenceNumber = (byte) ((++lastSequenceNumber) % 255);
			return lastSequenceNumber;
		}

	}

	/**
	 * The maximum size of a fragment, including headers.
	 */
	private int maximumFragmentSize;

	/**
	 * A set of factories that are called to create ChannelDownstreamHandler instances upon a write request of a new
	 * sender.
	 */
	private final Map<HandlerFactory, Multimap<String, String>> channelDownstreamHandlerFactories;

	private Map<Long, Fragmenter> fragmenters = Maps.newHashMap();

	/**
	 * <p>
	 * Constructs a new RUPFragmentEncoder with a maximum fragment size (including 19 bytes RUP packet headers) of
	 * {@code maximumFragmentSize}. The maximum fragment size is depending on the actual protocol stack that is used
	 * for the current application, therefore it cannot be statically defined but differs from application to
	 * application.
	 * </p>
	 * <p>
	 * <i>Please note:</i> Another 19 bytes will be used for RUP headers and another 4 bytes will be used for
	 * DLE STX ... ETX encoding that frames the fragmented payload.
	 * </p>
	 *
	 * @param maximumFragmentSize
	 * 		the maximum fragment size (including 19 bytes of packet headers)
	 * @param channelDownstreamHandlerFactories
	 * 		a set of factories that create e.g. encoders to encode a packets payload
	 */
	public RUPPacketEncoder(int maximumFragmentSize,
							Map<HandlerFactory, Multimap<String, String>> channelDownstreamHandlerFactories) {
		this.maximumFragmentSize = maximumFragmentSize;
		this.channelDownstreamHandlerFactories = channelDownstreamHandlerFactories;
	}

	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		if (!(e.getMessage() instanceof RUPPacket)) {
			ctx.sendDownstream(e);
			return;
		}


		RUPPacket packet = (RUPPacket) e.getMessage();
		log.trace("Encoding into fragments: {}", packet);

		// only messages need fragmentation, set sink, sink requests and sink responses don't
		if (packet.getCmdType() != RUPPacket.Type.MESSAGE.getValue()) {
			RUPFragment fragment = RUPFragmentFactory.create(
					packet.getCmdType(),
					(byte) 0,
					packet.getDestination(),
					packet.getSource(),
					packet.getPayload()
			);
			log.trace("Sending non-MESSAGE type RUP packet downstream", packet);
			ctx.sendDownstream(
					new DownstreamMessageEvent(ctx.getChannel(), e.getFuture(), fragment, e.getRemoteAddress())
			);
			return;
		}

		for (RUPFragment fragment : getFragmenter(packet).fragment(packet)) {
			log.trace("Sending fragment downstream: {}", fragment);
			ctx.sendDownstream(
					new DownstreamMessageEvent(ctx.getChannel(), e.getFuture(), fragment, e.getRemoteAddress())
			);
		}

	}

	private Fragmenter getFragmenter(RUPPacket packet) {
		long source = packet.getSource();
		Fragmenter fragmenter = fragmenters.get(source);
		if (fragmenter == null) {
			fragmenter = new Fragmenter(maximumFragmentSize - 19, createChannelDownstreamHandlers());
			fragmenters.put(source, fragmenter);
		}
		return fragmenter;
	}

	private ChannelDownstreamHandler[] createChannelDownstreamHandlers() {

		Set<HandlerFactory> factories = channelDownstreamHandlerFactories.keySet();
		List<ChannelDownstreamHandler> channelDownstreamHandlers = Lists.newArrayList();

		for (HandlerFactory factory : factories) {
			try {
				final String randomHandlerName = Integer.toString(new Random().nextInt());
				final NamedChannelHandlerList factoryHandlers = factory.create(new ChannelHandlerConfig(
						factory.getName(),
						randomHandlerName,
						channelDownstreamHandlerFactories.get(factory)
				));
				for (NamedChannelHandler factoryHandler : factoryHandlers) {
					channelDownstreamHandlers.add((ChannelDownstreamHandler) factoryHandler.getChannelHandler());
				}
			} catch (Exception e) {
				throw propagate(e);
			}
		}

		return channelDownstreamHandlers.toArray(new ChannelDownstreamHandler[channelDownstreamHandlers.size()]);
	}
}
