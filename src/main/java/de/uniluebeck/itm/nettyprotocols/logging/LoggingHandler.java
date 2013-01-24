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
package de.uniluebeck.itm.nettyprotocols.logging;

import de.uniluebeck.itm.tr.util.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingHandler implements ChannelDownstreamHandler, ChannelUpstreamHandler {

	private final Logger log;

	public LoggingHandler() {
		this(null);
	}

	public LoggingHandler(String instanceName) {
		log = LoggerFactory.getLogger(instanceName != null ? instanceName : LoggingHandler.class.getName());
	}

	@Override
	public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {

		if (e instanceof DownstreamMessageEvent && ((DownstreamMessageEvent) e).getMessage() instanceof ChannelBuffer) {
			logChannelBuffer((ChannelBuffer) ((DownstreamMessageEvent) e).getMessage(), true);
		} else {
			log.debug("DOWNSTREAM : {}", e);
		}

		ctx.sendDownstream(e);
	}

	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {

		if (e instanceof UpstreamMessageEvent && ((UpstreamMessageEvent) e).getMessage() instanceof ChannelBuffer) {
			logChannelBuffer((ChannelBuffer) ((UpstreamMessageEvent) e).getMessage(), false);
		} else {
			log.debug("UPSTREAM   : {}", e);
		}

		ctx.sendUpstream(e);
	}

	private void logChannelBuffer(final ChannelBuffer msg, final boolean downstream) {
		if (log.isDebugEnabled()) {
			log.debug((downstream ? "DOWNSTREAM" : "UPSTREAM  ") + " : {} | {}",
					StringUtils.replaceNonPrintableAsciiCharacters(msg.array(), msg.readerIndex(), msg.readableBytes()),
					StringUtils.toHexString(msg.array(), msg.readerIndex(), msg.readableBytes())
			);
		}
	}

}
