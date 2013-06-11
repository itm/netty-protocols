package de.uniluebeck.itm.nettyprotocols;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import de.uniluebeck.itm.nettyprotocols.contiki.ContikiHandlerFactory;
import de.uniluebeck.itm.nettyprotocols.discard.DiscardHandlerFactory;
import de.uniluebeck.itm.nettyprotocols.dlestxetx.DleStxEtxFramingDecoderFactory;
import de.uniluebeck.itm.nettyprotocols.dlestxetx.DleStxEtxFramingEncoderFactory;
import de.uniluebeck.itm.nettyprotocols.dlestxetx.DleStxEtxFramingFactory;
import de.uniluebeck.itm.nettyprotocols.isense.*;
import de.uniluebeck.itm.nettyprotocols.isense.iseraerial.ISerAerialPacketDecoderFactory;
import de.uniluebeck.itm.nettyprotocols.isense.iseraerial.ISerAerialPacketEncoderFactory;
import de.uniluebeck.itm.nettyprotocols.isense.iseraerial.ISerAerialPacketFactory;
import de.uniluebeck.itm.nettyprotocols.isense.ishellinterpreter.IShellInterpreterHandlerFactory;
import de.uniluebeck.itm.nettyprotocols.isense.otap.ISenseOtapFactory;
import de.uniluebeck.itm.nettyprotocols.logging.LoggingHandlerFactory;
import de.uniluebeck.itm.nettyprotocols.tinyos.*;

public class NettyProtocolsModule extends AbstractModule {

	@Override
	protected void configure() {

		final Multibinder<HandlerFactory> setBinder = Multibinder.newSetBinder(binder(), HandlerFactory.class);

		setBinder.addBinding().to(Base64DecoderFactory.class);
		setBinder.addBinding().to(Base64EncoderFactory.class);
		setBinder.addBinding().to(CompatibleObjectDecoderFactory.class);
		setBinder.addBinding().to(CompatibleObjectEncoderFactory.class);
		setBinder.addBinding().to(ContikiHandlerFactory.class);
		setBinder.addBinding().to(DiscardHandlerFactory.class);
		setBinder.addBinding().to(DleStxEtxFramingDecoderFactory.class);
		setBinder.addBinding().to(DleStxEtxFramingEncoderFactory.class);
		setBinder.addBinding().to(DleStxEtxFramingFactory.class);
		setBinder.addBinding().to(FixedLengthFrameDecoderFactory.class);
		setBinder.addBinding().to(HdlcTranslateDecoderFactory.class);
		setBinder.addBinding().to(HdlcTranslateEncoderFactory.class);
		setBinder.addBinding().to(HdlcTranslateFactory.class);
		setBinder.addBinding().to(ISenseFactory.class);
		setBinder.addBinding().to(ISenseOtapFactory.class);
		setBinder.addBinding().to(ISensePacketDecoderFactory.class);
		setBinder.addBinding().to(ISensePacketDownstreamDecoderFactory.class);
		setBinder.addBinding().to(ISensePacketEncoderFactory.class);
		setBinder.addBinding().to(ISensePacketFactory.class);
		setBinder.addBinding().to(ISensePacketUpstreamEncoderFactory.class);
		setBinder.addBinding().to(ISerAerialPacketDecoderFactory.class);
		setBinder.addBinding().to(ISerAerialPacketEncoderFactory.class);
		setBinder.addBinding().to(ISerAerialPacketFactory.class);
		setBinder.addBinding().to(IShellInterpreterHandlerFactory.class);
		setBinder.addBinding().to(LengthFieldBasedFrameDecoderFactory.class);
		setBinder.addBinding().to(LengthFieldPrependerFactory.class);
		setBinder.addBinding().to(LoggingHandlerFactory.class);
		setBinder.addBinding().to(ObjectDecoderFactory.class);
		setBinder.addBinding().to(ObjectEncoderFactory.class);
		setBinder.addBinding().to(StringDecoderFactory.class);
		setBinder.addBinding().to(StringEncoderFactory.class);
		setBinder.addBinding().to(TinyOsFactory.class);
		setBinder.addBinding().to(TinyOsSerialDecoderFactory.class);
		setBinder.addBinding().to(TinyOsSerialEncoderFactory.class);
		setBinder.addBinding().to(TinyOsSerialFactory.class);
		setBinder.addBinding().to(ZlibDecoderFactory.class);
		setBinder.addBinding().to(ZlibEncoderFactory.class);

		bind(HandlerFactoryMap.class).to(HandlerFactoryMapImpl.class);
	}
}
