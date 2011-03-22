/**
 * Copyright (c) 2010, Dennis Pfisterer, Institute of Telematics, University of Luebeck
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
package de.uniluebeck.itm.nettyrxtx.rup;

import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Decoder that decodes a series of RUPFragment instances (fragments) into one
 * {@link RUPFragment} by applying another decoder (e.g
 * {@link de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxFramingDecoder} on the
 * packets payload. The result of the decoding is on {@link RUPFragment}
 * instance with a reassembled payload and the same packet headers as the
 * individual fragments (except of the sequenceNumber field).
 */
public class RUPPacketDecoder extends SimpleChannelUpstreamHandler {

    private static final Logger log = LoggerFactory.getLogger(RUPPacketDecoder.class);

    private static class Reassembler {

        private final DecoderEmbedder<ChannelBuffer> decoder;

        private final long source;

        private final long destination;

        public Reassembler(final long source, final long destination,
                final ChannelUpstreamHandler[] channelUpstreamHandlers) {
            this.source = source;
            this.destination = destination;
            this.decoder = new DecoderEmbedder<ChannelBuffer>(channelUpstreamHandlers);
        }

        public RUPPacket[] receiveFragment(final RUPFragment fragment) {

            // let the decoder try to reassemble the package
            decoder.offer(fragment.getPayload());

            // check if one or more packages have been reassembled
            Object[] decodedPayloads = decoder.pollAll();
            RUPPacket[] decodedRUPPackets = new RUPPacket[decodedPayloads.length];

            // return all reassembled packages
            for (int i = 0; i < decodedPayloads.length; i++) {

                ChannelBuffer decodedPayloadBuffer = (ChannelBuffer) decodedPayloads[i];
                decodedRUPPackets[i] =
                        new RUPPacketImpl(RUPPacket.Type.MESSAGE, destination, source, decodedPayloadBuffer);
            }

            return decodedRUPPackets;
        }
    }

    /**
     * A set of factories that are called to create ChannelUpstreamHandler
     * instances upon creation of a new Reassembler instance.
     */
    private final ChannelUpstreamHandlerFactory[] channelUpstreamHandlerFactories;

    /**
     * Map that holds a Reassembler instance for every source address of
     * RUPFragment instances received.
     */
    private final Map<Long, Reassembler> reassemblersMap = Maps.newHashMap();

    /**
     * Constructs a new RUPPacketDecoder instance that uses a
     * {@link DecoderEmbedder} that wraps decoders created by the
     * {@code channelUpstreamHandlerFactories} to reassemble a RUPFragment (type
     * {@link RUPPacket.Type#MESSAGE}) instance from a series of RUPFragment
     * fragments. For each RUP endpoint one {@link DecoderEmbedder} instance
     * that uses the decoders created by {@code channelUpstreamHandlers}.
     * 
     * @param channelUpstreamHandlerFactories
     *            the factories for creating handlers for reassembling the
     *            packet from a series of packet fragments
     */
    public RUPPacketDecoder(final ChannelUpstreamHandlerFactory... channelUpstreamHandlerFactories) {
        this.channelUpstreamHandlerFactories = channelUpstreamHandlerFactories;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

        if (!(e.getMessage() instanceof RUPFragment)) {
            ctx.sendUpstream(e);
            return;
        }

        RUPFragment fragment = (RUPFragment) e.getMessage();

        // only reassembly RUP message packets, other types don't need
        // reassembly
        if (RUPPacket.Type.MESSAGE.getValue() != fragment.getCmdType()) {
            ctx.sendUpstream(new UpstreamMessageEvent(ctx.getChannel(), new RUPPacketImpl(fragment.getCmdType(),
                    fragment.getDestination(), fragment.getSource(), fragment.getPayload()), ctx.getChannel()
                    .getRemoteAddress()));
            return;
        }

        Reassembler reassembler = getReassembler(fragment);
        RUPPacket[] reassembledPackets = reassembler.receiveFragment(fragment);

        for (RUPPacket reassembledPacket : reassembledPackets) {
            log.trace("[{}] Sending decoded RUPPacket upstream: {}", ctx.getName(), reassembledPacket);
            ctx.sendUpstream(new UpstreamMessageEvent(ctx.getChannel(), reassembledPacket, ctx.getChannel()
                    .getRemoteAddress()));
        }

    }

    /**
     * Returns or constructs the Reassembler responsible for packets from the
     * source node of {@code fragmentedRupPacket}.
     * 
     * @param fragment
     *            the packet for which to get the reassembler
     * 
     * @return the responsible Reassembler instance
     */
    private Reassembler getReassembler(final RUPFragment fragment) {

        long source = fragment.getSource();
        long destination = fragment.getDestination();
        Reassembler reassembler = reassemblersMap.get(source);

        // construct new assembler if this packet is the first received from
        // source
        if (reassembler == null) {
            reassembler = new Reassembler(source, destination, createChannelUpstreamHandlers());
            reassemblersMap.put(source, reassembler);
        }

        return reassembler;
    }

    private ChannelUpstreamHandler[] createChannelUpstreamHandlers() {

        ChannelUpstreamHandler[] channelUpstreamHandlers =
                new ChannelUpstreamHandler[channelUpstreamHandlerFactories.length];

        for (int i = 0; i < channelUpstreamHandlerFactories.length; i++) {
            channelUpstreamHandlers[i] = channelUpstreamHandlerFactories[i].create();
        }

        return channelUpstreamHandlers;
    }
}
