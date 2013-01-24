package de.uniluebeck.itm.netty.wisebednodeapi;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.*;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.interaction.*;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.linkcontrol.*;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.networkdescription.*;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.nodecontrol.*;
import de.uniluebeck.itm.tr.util.TimedCache;
import de.uniluebeck.itm.tr.util.TimedCacheListener;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class NodeAPIHandler extends SimpleChannelHandler implements ChannelUpstreamHandler, ChannelDownstreamHandler {

	static class RequestCacheEntry {

		public final Request request;

		public final Channel channel;

		public final ChannelHandlerContext channelHandlerContext;

		public final SocketAddress remoteAddress;

		RequestCacheEntry(Request request, Channel channel, ChannelHandlerContext channelHandlerContext,
						  SocketAddress remoteAddress) {
			this.request = request;
			this.channel = channel;
			this.channelHandlerContext = channelHandlerContext;
			this.remoteAddress = remoteAddress;
		}

		public Request getRequest() {
			return request;
		}
	}

	@Inject
	@Named("requestCache")
	private TimedCache<Byte, RequestCacheEntry> requestCache = new TimedCache<Byte, RequestCacheEntry>();

	private final TimedCacheListener<Byte, RequestCacheEntry> requestCacheListener =
			new TimedCacheListener<Byte, RequestCacheEntry>() {
				@Override
				public Tuple<Long, TimeUnit> timeout(Byte requestId, RequestCacheEntry entry) {
					TimeOutResponse response = new TimeOutResponse(entry.request);
					UpstreamMessageEvent event = new UpstreamMessageEvent(entry.channel, response, entry.remoteAddress);
					entry.channelHandlerContext.sendUpstream(event);
					return null;
				}
			};

	/**
	 * Only for unit testing.
	 *
	 * @return
	 */
	TimedCacheListener<Byte, RequestCacheEntry> getRequestCacheListener() {
		return requestCacheListener;
	}

	NodeAPIHandler() {
		requestCache.setListener(requestCacheListener);
	}

	@Override
	public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
		final DownstreamMessageEvent event = (DownstreamMessageEvent) e;
		final SocketAddress remoteAddress = ((DownstreamMessageEvent) e).getRemoteAddress();
		final Channel channel = e.getChannel();
		final Request request = (Request) event.getMessage();
		final Command command = createCommandMessage(request);

		requestCache.put(request.getRequestID(), new RequestCacheEntry(request, channel, ctx, remoteAddress));

		final DownstreamMessageEvent downstreamMessageEvent = new DownstreamMessageEvent(
				event.getChannel(),
				event.getFuture(),
				command,
				event.getRemoteAddress()
		);
		ctx.sendDownstream(downstreamMessageEvent);
	}

	//Helper method to create Command-instance for request
	private Command createCommandMessage(Request request) throws Exception {
		byte requestID = request.getRequestID();
		ChannelBuffer payload = ChannelBuffers.wrappedBuffer(request.getPayload());
		//Link Control
		if (request instanceof SetVirtualLinkRequest) {
			return new SetVirtualLinkCommand(requestID, payload, ((SetVirtualLinkRequest) request).getDestinationNode()
			);
		} else if (request instanceof DestroyVirtualLinkRequest) {
			return new DestroyVirtualLinkCommand(requestID, payload,
					((DestroyVirtualLinkRequest) request).getDestinationNode()
			);
		} else if (request instanceof EnablePhysicalLinkRequest) {
			return new EnablePhysicalLinkCommand(requestID, payload, ((EnablePhysicalLinkRequest) request).getNodeB());
		} else if (request instanceof DisablePhysicalLinkRequest) {
			return new DisablePhysicalLinkCommand(requestID, payload, ((DisablePhysicalLinkRequest) request).getNodeB()
			);
			//node control
		} else if (request instanceof EnableNodeRequest) {
			return new EnableNodeCommand(requestID, payload);
		} else if (request instanceof DisableNodeRequest) {
			return new DisableNodeCommand(requestID, payload);
		} else if (request instanceof ResetNodeRequest) {
			return new ResetNodeCommand(requestID, payload, ((ResetNodeRequest) request).getTime());
		} else if (request instanceof SetStartTimeRequest) {
			return new SetStartTimeCommand(requestID, payload, ((SetStartTimeRequest) request).getTime());
		} else if (request instanceof SetVirtualIDRequest) {
			return new SetVirtualIDCommand(requestID, payload, ((SetVirtualIDRequest) request).getVirtualNodeId());
		} else if (request instanceof AreNodesAliveRequest) {
			return new AreNodesAliveCommand(requestID, payload);
		} else if (request instanceof GetVersionRequest) {
			return new GetVersionCommand(requestID, payload);
			// network description
		} else if (request instanceof GetPropertyValueRequest) {
			return new GetPropertyValueCommand(requestID, payload, ((GetPropertyValueRequest) request).getProperty());
		} else if (request instanceof GetNeighborhoodRequest) {
			return new GetNeighborhoodCommand(requestID, payload);
			// interaction
		} else if (request instanceof TextDataRequest) {
			return new TextDataCommand(requestID, ((TextDataRequest) request).getMessageLevel(), payload);
		} else if (request instanceof VirtualLinkDataRequest) {
			return new VirtualLinkDataCommand(requestID, ((VirtualLinkDataRequest) request).getRssi(),
					((VirtualLinkDataRequest) request).getLqi(), ((VirtualLinkDataRequest) request).getLen(),
					((VirtualLinkDataRequest) request).getDest(), ((VirtualLinkDataRequest) request).getSource(),
					payload
			);
		} else if (request instanceof BinaryDataRequest) {
			return new BinaryDataCommand(requestID, ((BinaryDataRequest) request).getLen(), payload);
		} else if (request instanceof FlashProgramDataRequest) {
			return new FlashProgramDataCommand(requestID, ((FlashProgramDataRequest) request).getLen(), payload);
		} else if (request instanceof NodeOutputTextRequest) {
			return new NodeOutputTextCommand(requestID, ((NodeOutputTextRequest) request).getMessageLevel(), payload);
		} else if (request instanceof NodeOutputBinaryRequest) {
			return new NodeOutputBinaryCommand(requestID, ((NodeOutputBinaryRequest) request).getLen(), payload);
		} else if (request instanceof NodeOutputVirtualLinkRequest) {
			return new NodeOutputVirtualLinkCommand(requestID, ((NodeOutputVirtualLinkRequest) request).getRssi(),
					((NodeOutputVirtualLinkRequest) request).getLqi(),
					((NodeOutputVirtualLinkRequest) request).getLen(),
					((NodeOutputVirtualLinkRequest) request).getDest(),
					((NodeOutputVirtualLinkRequest) request).getSource(), payload
			);
		} else {
			throw new Exception("Could not create CommandMessage from request");
		}
	}

	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
		UpstreamMessageEvent event = (UpstreamMessageEvent) e;
		final Request originalRequestFromRequestCache =
				requestCache.get(((CommandResponse) event.getMessage()).getRequestID()).getRequest();
		final Response response = createResponseMessage(originalRequestFromRequestCache);

		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
				event.getChannel(),
				response,
				event.getRemoteAddress()
		);
		ctx.sendUpstream(upstreamMessageEvent);
	}

	//helper method to create new Response-instance for request
	private Response createResponseMessage(Request originalRequestFromRequestCache) throws Exception {
		if (originalRequestFromRequestCache instanceof SetVirtualLinkRequest) {
			return new SetVirtualLinkResponse((SetVirtualLinkRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof DestroyVirtualLinkRequest) {
			return new DestroyVirtualLinkResponse((DestroyVirtualLinkRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof EnablePhysicalLinkRequest) {
			return new EnablePhysicalLinkResponse((EnablePhysicalLinkRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof DisablePhysicalLinkRequest) {
			return new DisablePhysicalLinkResponse((DisablePhysicalLinkRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof EnableNodeRequest) {
			return new EnableNodeResponse((EnableNodeRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof DisableNodeRequest) {
			return new DisableNodeResponse((DisableNodeRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof ResetNodeRequest) {
			return new ResetNodeResponse((ResetNodeRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof SetStartTimeRequest) {
			return new SetStartTimeResponse((SetStartTimeRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof SetVirtualIDRequest) {
			return new SetVirtualIDResponse((SetVirtualIDRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof AreNodesAliveRequest) {
			return new AreNodesAliveResponse((AreNodesAliveRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof GetVersionRequest) {
			return new GetVersionResponse((GetVersionRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof GetPropertyValueRequest) {
			return new GetPropertyValueResponse((GetPropertyValueRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof GetNeighborhoodRequest) {
			return new GetNeighborhoodResponse((GetNeighborhoodRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof TextDataRequest) {
			return new TextDataResponse((TextDataRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof VirtualLinkDataRequest) {
			return new VirtualLinkDataResponse((VirtualLinkDataRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof BinaryDataRequest) {
			return new BinaryDataResponse((BinaryDataRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof FlashProgramDataRequest) {
			return new FlashProgramDataResponse((FlashProgramDataRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof NodeOutputTextRequest) {
			return new NodeOutputTextResponse((NodeOutputTextRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof NodeOutputBinaryRequest) {
			return new NodeOutputBinaryResponse((NodeOutputBinaryRequest) originalRequestFromRequestCache);
		} else if (originalRequestFromRequestCache instanceof NodeOutputVirtualLinkRequest) {
			return new NodeOutputVirtualLinkResponse((NodeOutputVirtualLinkRequest) originalRequestFromRequestCache);
		} else {
			throw new Exception("Could not create valid response for request from requestCache");
		}

	}

}
