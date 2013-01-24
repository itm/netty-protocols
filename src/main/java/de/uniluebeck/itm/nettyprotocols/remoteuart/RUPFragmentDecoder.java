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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import de.uniluebeck.itm.nettyprotocols.isense.ISensePacket;
import de.uniluebeck.itm.tr.util.TimeDiff;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Decoder for the Remote UART Protocol (RUP) that can decode RUP packet
 * fragments out of either a ChannelBuffer or an ISensePacket. Also, this
 * decoder makes sure the packet order is correct according to the sequence
 * number for a given window of number of packets and/or time.
 * <p/>
 * // TODO implement time based window
 */
public class RUPFragmentDecoder extends SimpleChannelUpstreamHandler {

	private static final Logger log = LoggerFactory.getLogger(RUPFragmentDecoder.class);

	private static final long TIMEWINDOW = 500;

	private static class PacketBuffer {

		private Map<Integer, RUPFragment> packets = Maps.newTreeMap();

		private int windowSize = 10;

		private int windowOffset = 0;

		public boolean veryFirstPacket = true;

		private TimeDiff timeWindow = new TimeDiff(TIMEWINDOW);

		/**
		 * Checks if a sequenceNumber is inside the acceptance window.
		 *
		 * @param sequenceNumber
		 * 		the sequenceNumber of the packet received
		 *
		 * @return {@code true} if sequenceNumber lies inside the window,
		 *         {@code false} otherwise
		 */
		private boolean isInWindow(int sequenceNumber) {

			// if window overlaps number overflow at 255 check if sequenceNumber
			// either lies in between windowOffset and 255
			// or in between zero and ((windowOffset+windowSize)%255)
			if (windowOffset + windowSize > 255) {

				return sequenceNumber > windowOffset
						|| (sequenceNumber > 0 && sequenceNumber < ((windowOffset + windowSize) % 255));
			}

			// check if sequenceNumber lies in between the non-overlapping
			// window
			return sequenceNumber >= windowOffset && sequenceNumber < (windowOffset + windowSize);
		}

	}

	private ScheduledExecutorService scheduler;

	private Map<Long, PacketBuffer> packetBuffers = Maps.newHashMap();

	public RUPFragmentDecoder(final ScheduledExecutorService scheduler) {
		Preconditions.checkNotNull(scheduler);
		this.scheduler = scheduler;
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

		ChannelBuffer payload;

		// extract payload out of either a ChannelBuffer or an ISensePacket
		if (e.getMessage() instanceof ISensePacket) {

			final ISensePacket iSensePacket = (ISensePacket) e.getMessage();

			// only do something if the packet is a Remote UART packet,
			// otherwise send it upstream
			if (!RUPHelper.isRemoteUARTPacket(iSensePacket)) {
				ctx.sendUpstream(e);
				return;
			}

			payload = iSensePacket.getPayload();

		} else if (e.getMessage() instanceof ChannelBuffer) {

			payload = (ChannelBuffer) e.getMessage();

		} else {

			ctx.sendUpstream(e);
			return;
		}

		final RUPFragment fragment = RUPFragmentFactory.wrap(payload);

		final int sequenceNumber = fragment.getSequenceNumber();
		final long source = fragment.getSource();

		// get the packetBuffer for the sender of fragment
		PacketBuffer packetBuffer = packetBuffers.get(source);
		if (packetBuffer == null) {
			packetBuffer = new PacketBuffer();
			packetBuffers.put(source, packetBuffer);
		}

		if (packetBuffer.veryFirstPacket) {
			packetBuffer.veryFirstPacket = false;
			packetBuffer.windowOffset = sequenceNumber;
		}

		// only accept packets that are within window bounds
		if (packetBuffer.isInWindow(sequenceNumber) || packetBuffer.timeWindow.isTimeout()) {

			packetBuffer.timeWindow.touch();

			if (log.isTraceEnabled()) {
				log.trace("Received packet with sequenceNumber in window ({} -> {}): {}",
						new Object[]{
								packetBuffer.windowOffset,
								((packetBuffer.windowOffset + packetBuffer.windowSize) % 255), fragment
						}
				);
			}

			packetBuffer.packets.put(sequenceNumber, fragment);
			final PacketBuffer finalPacketBuffer = packetBuffer;
			scheduler.schedule(new Runnable() {
				public void run() {
					sendUpstreamWindowTimeout(finalPacketBuffer, ctx);
				}
			}, TIMEWINDOW, TimeUnit.MILLISECONDS
			);
			sendUpstreamIfBuffered(packetBuffer, ctx);

		}

		// discard packets outside window bounds
		else {
			if (log.isTraceEnabled()) {
				log.trace("Ignored packet outside of packetBuffer window ({} -> {}): {}",
						new Object[]{
								packetBuffer.windowOffset,
								((packetBuffer.windowOffset + packetBuffer.windowSize) % 255), fragment
						}
				);
			}
		}

	}

	private void sendUpstreamWindowTimeout(final PacketBuffer packetBuffer, final ChannelHandlerContext ctx) {
		for (RUPFragment packet : packetBuffer.packets.values()) {
			sendUpstream(packetBuffer, packet, ctx);
		}
	}

	private void sendUpstream(final PacketBuffer packetBuffer, final RUPFragment packetFragment,
							  final ChannelHandlerContext ctx) {

		// send packet upstream
		if (log.isTraceEnabled()) {
			log.trace("Sending packet upstream: {}", packetFragment);
		}

		ctx.sendUpstream(
				new UpstreamMessageEvent(ctx.getChannel(), packetFragment, ctx.getChannel().getRemoteAddress())
		);

		packetBuffer.windowOffset = (packetFragment.getSequenceNumber() + 1) % 255;
	}

	private void sendUpstreamIfBuffered(final PacketBuffer packetBuffer, final ChannelHandlerContext ctx) {

		// if there's no packet at currents offset it might mean we should
		// possibly wait for it to arrive before sending
		// packages upstream
		int currentOffset = packetBuffer.windowOffset;
		while (packetBuffer.packets.containsKey(currentOffset)) {
			sendUpstream(packetBuffer, packetBuffer.packets.remove(currentOffset), ctx);
			currentOffset = packetBuffer.windowOffset;
		}
	}

}
