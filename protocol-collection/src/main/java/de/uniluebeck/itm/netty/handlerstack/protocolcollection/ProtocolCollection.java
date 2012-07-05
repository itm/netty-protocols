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
package de.uniluebeck.itm.netty.handlerstack.protocolcollection;

import com.coalesenses.isense.ishell.interpreter.IShellInterpreterHandlerFactory;
import de.uniluebeck.itm.netty.handlerstack.HandlerFactoryRegistry;
import de.uniluebeck.itm.netty.handlerstack.discard.DiscardMessagesHandlerFactory;
import de.uniluebeck.itm.netty.handlerstack.dlestxetx.DleStxEtxFramingDecoderFactory;
import de.uniluebeck.itm.netty.handlerstack.dlestxetx.DleStxEtxFramingEncoderFactory;
import de.uniluebeck.itm.netty.handlerstack.dlestxetx.DleStxEtxFramingFactory;
import de.uniluebeck.itm.netty.handlerstack.hdlctranslatec.HdlcTranslateCDecoderFactory;
import de.uniluebeck.itm.netty.handlerstack.hdlctranslatec.HdlcTranslateCEncoderFactory;
import de.uniluebeck.itm.netty.handlerstack.hdlctranslatec.HdlcTranslateCFactory;
import de.uniluebeck.itm.netty.handlerstack.isense.*;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.ISenseOtapFactory;
import de.uniluebeck.itm.netty.handlerstack.iseraerial.ISerAerialPacketDecoderFactory;
import de.uniluebeck.itm.netty.handlerstack.iseraerial.ISerAerialPacketEncoderFactory;
import de.uniluebeck.itm.netty.handlerstack.iseraerial.ISerAerialPacketFactory;
import de.uniluebeck.itm.netty.handlerstack.nettyincluded.*;
import de.uniluebeck.itm.netty.handlerstack.serialp.SerialPDecoderFactory;
import de.uniluebeck.itm.netty.handlerstack.serialp.SerialPEncoderFactory;
import de.uniluebeck.itm.netty.handlerstack.serialp.SerialPFactory;
import de.uniluebeck.netty.handlerstack.logginghandler.LoggingHandlerFactory;

public class ProtocolCollection {

	/**
	 * Registers all Plug-ins from ITM's netty handlerstack project with the factory
	 */
	public static void registerProtocols(HandlerFactoryRegistry registry) {

		try {
			registry.register(new DiscardMessagesHandlerFactory());

			registry.register(new HdlcTranslateCDecoderFactory());
			registry.register(new HdlcTranslateCEncoderFactory());
			registry.register(new HdlcTranslateCFactory());

			registry.register(new SerialPDecoderFactory());
			registry.register(new SerialPEncoderFactory());
			registry.register(new SerialPFactory());

			registry.register(new ISerAerialPacketDecoderFactory());
			registry.register(new ISerAerialPacketEncoderFactory());
			registry.register(new ISerAerialPacketFactory());

			registry.register(new ISensePacketDecoderFactory());
			registry.register(new ISensePacketEncoderFactory());
			registry.register(new ISensePacketDownstreamDecoderFactory());
			registry.register(new ISensePacketUpstreamEncoderFactory());
			registry.register(new ISensePacketFactory());

			registry.register(new DleStxEtxFramingDecoderFactory());
			registry.register(new DleStxEtxFramingEncoderFactory());
			registry.register(new DleStxEtxFramingFactory());

			registry.register(new ISenseOtapFactory());
			registry.register(new IShellInterpreterHandlerFactory());
			registry.register(new LoggingHandlerFactory());

			registerNettyIncluded(registry);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void registerNettyIncluded(final HandlerFactoryRegistry registry) throws Exception {
		registry.register(new Base64DecoderFactory());
		registry.register(new Base64EncoderFactory());
		registry.register(new CompatibleObjectDecoderFactory());
		registry.register(new CompatibleObjectEncoderFactory());
		registry.register(new FixedLengthFrameDecoderFactory());
		registry.register(new LengthFieldBasedFrameDecoderFactory());
		registry.register(new LengthFieldPrependerFactory());
		registry.register(new ObjectDecoderFactory());
		registry.register(new ObjectEncoderFactory());
		registry.register(new StringDecoderFactory());
		registry.register(new StringEncoderFactory());
		registry.register(new ZlibDecoderFactory());
		registry.register(new ZlibEncoderFactory());
	}

}