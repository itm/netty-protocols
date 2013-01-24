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
package de.uniluebeck.itm.nettyprotocols.dlestxetx;

import de.uniluebeck.itm.tr.util.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DleStxEtxFramingDecoder extends FrameDecoder {

	private final Logger log;

	private boolean foundDLE;

	private boolean foundPacket;

	private ChannelBuffer packet;

	public DleStxEtxFramingDecoder() {
		this(null);
	}

	public DleStxEtxFramingDecoder(String instanceName) {
		log = LoggerFactory.getLogger(instanceName != null ? instanceName : DleStxEtxFramingDecoder.class.getName());
		resetDecodingState();
	}

	@Override
	protected Object decode(final ChannelHandlerContext ctx, final Channel channel, final ChannelBuffer buffer)
			throws Exception {

		while (buffer.readable()) {

			byte c = buffer.readByte();

			// check if last character read was DLE
			if (foundDLE) {
				foundDLE = false;

				if (c == DleStxEtxConstants.STX && !foundPacket) {

					foundPacket = true;

				} else if (c == DleStxEtxConstants.ETX && foundPacket) {

					ChannelBuffer packetRead = packet;
					resetDecodingState();
					return packetRead;

				} else if (c == DleStxEtxConstants.DLE && foundPacket) {

					// Stuffed DLE found
					packet.writeByte(DleStxEtxConstants.DLE);

				} else {

					if (log.isWarnEnabled()) {
						log.warn("Incomplete packet received: {}",
								StringUtils.toHexString(packet.array(), packet.readerIndex(), packet.readableBytes())
						);
					}
					resetDecodingState();
				}

			} else {

				if (c == DleStxEtxConstants.DLE) {
					// log.trace("Plain DLE received");
					foundDLE = true;
				} else if (foundPacket) {
					packet.writeByte(c);
				}

			}
		}

		// decoding is not yet complete, we'll need more bytes until we find DLE
		// ETX
		return null;
	}

	private void resetDecodingState() {
		foundDLE = false;
		foundPacket = false;
		packet = ChannelBuffers.dynamicBuffer(512);
	}

}
