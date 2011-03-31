package de.uniluebeck.itm.netty.handlerstack.protocolcollection;

import de.uniluebeck.itm.netty.handlerstack.HandlerFactoryRegistry;
import de.uniluebeck.itm.netty.handlerstack.dlestxetx.DleStxEtxFramingDecoderFactory;
import de.uniluebeck.itm.netty.handlerstack.dlestxetx.DleStxEtxFramingEncoderFactory;
import de.uniluebeck.itm.netty.handlerstack.isense.ISensePacketDecoderFactory;
import de.uniluebeck.itm.netty.handlerstack.isense.ISensePacketEncoderFactory;
import de.uniluebeck.itm.netty.handlerstack.iseraerial.ISerAerialPacketDecoderFactory;
import de.uniluebeck.itm.netty.handlerstack.iseraerial.ISerAerialPacketEncoderFactory;
import de.uniluebeck.netty.handlerstack.logginghandler.LoggingHandlerFactory;

public class ProtocolCollection {

    /** Registers all Plug-ins from ITM's netty handlerstack project with the factory */
    public static void registerProtocols(HandlerFactoryRegistry factory) throws Exception {

        factory.register(new ISerAerialPacketDecoderFactory());
        factory.register(new ISerAerialPacketEncoderFactory());

        factory.register(new ISensePacketDecoderFactory());
        factory.register(new ISensePacketEncoderFactory());

        factory.register(new DleStxEtxFramingDecoderFactory());
        factory.register(new DleStxEtxFramingEncoderFactory());

        factory.register(new LoggingHandlerFactory());
    }

}