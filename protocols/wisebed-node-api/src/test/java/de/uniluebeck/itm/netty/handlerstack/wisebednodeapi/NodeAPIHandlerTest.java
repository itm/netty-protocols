package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi;

import com.google.inject.*;
import com.google.inject.name.Names;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandResponse;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.interaction.*;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.linkcontrol.*;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.networkdescription.GetNeighborhoodCommand;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.networkdescription.GetNeighborhoodCommandResponse;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.networkdescription.GetPropertyValueCommand;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.networkdescription.GetPropertyValueCommandResponse;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.nodecontrol.*;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Request;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.TimeOutResponse;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.interaction.*;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.linkcontrol.*;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.networkdescription.GetNeighborhoodRequest;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.networkdescription.GetNeighborhoodResponse;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.networkdescription.GetPropertyValueRequest;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.networkdescription.GetPropertyValueResponse;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.nodecontrol.*;
import de.uniluebeck.itm.tr.util.TimedCache;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.SocketAddress;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NodeAPIHandlerTest {

	private NodeAPIHandler handler;

	@Mock
	private ChannelHandlerContext channelHandlerContext;

	@Mock
	private DownstreamMessageEvent downstreamMessageEvent;

	@Mock
	private UpstreamMessageEvent upstreamMessageEvent;

	@Mock
	private Channel channel;

	@Mock
	private SocketAddress remoteAddress;

	@Mock
	private ChannelFuture future;

	@Mock
	private TimedCache<Byte, NodeAPIHandler.RequestCacheEntry> requestCache;

	final byte requestID = 0x004A;
	final byte payload[] = new byte[]{(byte) 1238};
	final long destinationNode = 0x1234;
	final byte result = 0; //command_success
	final long nodeB = 0x1235;
	final short time = 10;
	final long virtualNodeId = 0x1236;
	final byte property = 21;
	final byte messageLevel = 1;
	final byte rssi = 2;
	final byte lqi = 3;
	final byte len = 4;
	final long sourceNode = 0x1237;

	@Before
	public void setUp() throws Exception {
		handler = (NodeAPIHandler) Guice.createInjector(new Module() {
			@Override
			public void configure(final Binder binder) {
				binder.bind(new TypeLiteral<TimedCache<Byte, NodeAPIHandler.RequestCacheEntry>>() {
				})
						.annotatedWith(Names.named("requestCache"))
						.toInstance(requestCache);

				binder.bind(ChannelHandler.class).to(NodeAPIHandler.class);
			}
		}
		).getInstance(ChannelHandler.class);
	}

	private Request createAndReturnRequestAndSetupMessageEvent(Class requestClass, MessageEvent messageEvent) {
		Request request;
		CommandResponse commandResponse;

		//link Control
		if (requestClass.equals(SetVirtualLinkRequest.class)) {
			request = new SetVirtualLinkRequest(requestID, payload, destinationNode);
			commandResponse = new SetVirtualLinkCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(DestroyVirtualLinkRequest.class)) {
			request = new DestroyVirtualLinkRequest(requestID, payload, destinationNode);
			commandResponse = new DestroyVirtualLinkCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(EnablePhysicalLinkRequest.class)) {
			request = new EnablePhysicalLinkRequest(requestID, payload, nodeB);
			commandResponse = new EnablePhysicalLinkCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(DisablePhysicalLinkRequest.class)) {
			request = new DisablePhysicalLinkRequest(requestID, payload, nodeB);
			commandResponse = new DisablePhysicalLinkCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
			//Node Control
		} else if (requestClass.equals(EnableNodeRequest.class)) {
			request = new EnableNodeRequest(requestID, payload);
			commandResponse = new EnableNodeCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(DisableNodeRequest.class)) {
			request = new DisableNodeRequest(requestID, payload);
			commandResponse = new DisableNodeCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(ResetNodeRequest.class)) {
			request = new ResetNodeRequest(requestID, payload, time);
			commandResponse = new ResetNodeCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(SetStartTimeRequest.class)) {
			request = new SetStartTimeRequest(requestID, payload, time);
			commandResponse = new SetStartTimeCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(SetVirtualIdRequest.class)) {
			request = new SetVirtualIdRequest(requestID, payload, virtualNodeId);
			commandResponse = new SetVirtualIDCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(AreNodesAliveRequest.class)) {
			request = new AreNodesAliveRequest(requestID, payload);
			commandResponse = new AreNodesAliveCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(GetVersionRequest.class)) {
			request = new GetVersionRequest(requestID, payload);
			commandResponse = new GetVersionCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
			//Network description
		} else if (requestClass.equals(GetPropertyValueRequest.class)) {
			request = new GetPropertyValueRequest(requestID, payload, property);
			commandResponse = new GetPropertyValueCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(GetNeighborhoodRequest.class)) {
			request = new GetNeighborhoodRequest(requestID, payload);
			commandResponse = new GetNeighborhoodCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
			//Interaction
		} else if (requestClass.equals(TextDataRequest.class)) {
			request = new TextDataRequest(requestID, messageLevel, payload);
			commandResponse = new TextDataCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(VirtualLinkDataRequest.class)) {
			request = new VirtualLinkDataRequest(requestID, rssi, lqi, len, destinationNode, sourceNode, payload);
			commandResponse = new VirtualLinkDataCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(BinaryDataRequest.class)) {
			request = new BinaryDataRequest(requestID, len, payload);
			commandResponse = new BinaryDataCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(FlashProgramDataRequest.class)) {
			request = new FlashProgramDataRequest(requestID, len, payload);
			commandResponse = new FlashProgramDataCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(NodeOutputTextRequest.class)) {
			request = new NodeOutputTextRequest(requestID, messageLevel, payload);
			commandResponse = new NodeOutputTextCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(NodeOutputBinaryRequest.class)) {
			request = new NodeOutputBinaryRequest(requestID, len, payload);
			commandResponse = new NodeOutputBinaryCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else if (requestClass.equals(NodeOutputVirtualLinkRequest.class)) {
			request = new NodeOutputVirtualLinkRequest(requestID, rssi, lqi, len, destinationNode, sourceNode, payload);
			commandResponse = new NodeOutputVirtualLinkCommandResponse(requestID, result, ChannelBuffers.wrappedBuffer(payload));
		} else {
			throw new RuntimeException("Request not initialized!");
		}

		when(messageEvent.getChannel()).thenReturn(channel);
		when(messageEvent.getRemoteAddress()).thenReturn(remoteAddress);
		when(messageEvent.getFuture()).thenReturn(future);

		if (messageEvent instanceof DownstreamMessageEvent) {
			when(downstreamMessageEvent.getMessage()).thenReturn(request);
			doNothing().when(channelHandlerContext).sendDownstream(Matchers.<DownstreamMessageEvent>any());

		} else {
			when(upstreamMessageEvent.getMessage()).thenReturn(commandResponse);
			doNothing().when(channelHandlerContext).sendUpstream(Matchers.<UpstreamMessageEvent>any());

			final NodeAPIHandler.RequestCacheEntry requestCacheEntry =
					new NodeAPIHandler.RequestCacheEntry(request, channel, channelHandlerContext, remoteAddress);
			when(requestCache.get(requestID)).thenReturn(requestCacheEntry);
		}
		return request;
	}

	private Object setupActAndVerifyDownstreamMessageEvent(Class requestClass) throws Exception {
		// setup
		createAndReturnRequestAndSetupMessageEvent(requestClass, downstreamMessageEvent);

		final ArgumentCaptor<DownstreamMessageEvent> argumentCaptor =
				ArgumentCaptor.forClass(DownstreamMessageEvent.class);

		// act
		handler.handleDownstream(channelHandlerContext, downstreamMessageEvent);

		// verify
		verify(channelHandlerContext).sendDownstream(argumentCaptor.capture());

		return argumentCaptor.getValue().getMessage();
	}

	//test downstream requests
	//  link control
	@Test
	public void ifSetVirtualLinkRequestReceivedDownstreamSetVirtualLinkCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(SetVirtualLinkRequest.class);
		assertTrue(message instanceof SetVirtualLinkCommand);
		assertEquals(requestID, ((SetVirtualLinkCommand) message).getRequestID());
		assertEquals(CommandType.LinkControl.SET_VIRTUAL_LINK, ((SetVirtualLinkCommand) message).getCommandType());
		assertEquals(destinationNode, ((SetVirtualLinkCommand) message).getDestinationNode());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((SetVirtualLinkCommand) message).getPayload());
	}

	@Test
	public void ifDestroyVirtualLinkRequestReceivedDownstreamDestroyVirtualLinkCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(DestroyVirtualLinkRequest.class);
		assertTrue(message instanceof DestroyVirtualLinkCommand);
		assertEquals(requestID, ((DestroyVirtualLinkCommand) message).getRequestID());
		assertEquals(CommandType.LinkControl.DESTROY_VIRTUAL_LINK, ((DestroyVirtualLinkCommand) message).getCommandType());
		assertEquals(destinationNode, ((DestroyVirtualLinkCommand) message).getDestinationNode());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((DestroyVirtualLinkCommand) message).getPayload());
	}

	@Test
	public void ifEnablePhysicalLinkRequestReceivedDownstreamEnablePhysicalLinkCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(EnablePhysicalLinkRequest.class);
		assertTrue(message instanceof EnablePhysicalLinkCommand);
		assertEquals(requestID, ((EnablePhysicalLinkCommand) message).getRequestID());
		assertEquals(CommandType.LinkControl.ENABLE_PHYSICAL_LINK, ((EnablePhysicalLinkCommand) message).getCommandType());
		assertEquals(nodeB, ((EnablePhysicalLinkCommand) message).getNodeB());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((EnablePhysicalLinkCommand) message).getPayload());
	}

	@Test
	public void ifDisablePhysicalLinkRequestReceivedDownstreamDisablePhysicalLinkCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(DisablePhysicalLinkRequest.class);
		assertTrue(message instanceof DisablePhysicalLinkCommand);
		assertEquals(requestID, ((DisablePhysicalLinkCommand) message).getRequestID());
		assertEquals(CommandType.LinkControl.DISABLE_PHYSICAL_LINK, ((DisablePhysicalLinkCommand) message).getCommandType());
		assertEquals(nodeB, ((DisablePhysicalLinkCommand) message).getNodeB());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((DisablePhysicalLinkCommand) message).getPayload());
	}

	//  node control
	@Test
	public void ifEnableNodeRequestReceivedDownstreamEnableNodeCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(EnableNodeRequest.class);
		assertTrue(message instanceof EnableNodeCommand);
		assertEquals(requestID, ((EnableNodeCommand) message).getRequestID());
		assertEquals(CommandType.NodeControl.ENABLE_NODE, ((EnableNodeCommand) message).getCommandType());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((EnableNodeCommand) message).getPayload());
	}

	@Test
	public void ifDisableNodeRequestReceivedDownstreamDisableNodeCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(DisableNodeRequest.class);
		assertTrue(message instanceof DisableNodeCommand);
		assertEquals(requestID, ((DisableNodeCommand) message).getRequestID());
		assertEquals(CommandType.NodeControl.DISABLE_NODE, ((DisableNodeCommand) message).getCommandType());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((DisableNodeCommand) message).getPayload());
	}

	@Test
	public void ifResetNodeRequestReceivedDownstreamResetNodeCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(ResetNodeRequest.class);
		assertTrue(message instanceof ResetNodeCommand);
		assertEquals(requestID, ((ResetNodeCommand) message).getRequestID());
		assertEquals(CommandType.NodeControl.RESET_NODE, ((ResetNodeCommand) message).getCommandType());
		assertEquals(time, ((ResetNodeCommand) message).getTime());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((ResetNodeCommand) message).getPayload());
	}

	@Test
	public void ifSetStartTimeRequestReceivedDownstreamSetStartTimeCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(SetStartTimeRequest.class);
		assertTrue(message instanceof SetStartTimeCommand);
		assertEquals(requestID, ((SetStartTimeCommand) message).getRequestID());
		assertEquals(CommandType.NodeControl.SET_START_TIME, ((SetStartTimeCommand) message).getCommandType());
		assertEquals(time, ((SetStartTimeCommand) message).getTime());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((SetStartTimeCommand) message).getPayload());
	}

	@Test
	public void ifSetVirtualIdRequestReceivedDownstreamSetVirtualIDCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(SetVirtualIdRequest.class);
		assertTrue(message instanceof SetVirtualIDCommand);
		assertEquals(requestID, ((SetVirtualIDCommand) message).getRequestID());
		assertEquals(CommandType.NodeControl.SET_VIRTUAL_ID, ((SetVirtualIDCommand) message).getCommandType());
		assertEquals(virtualNodeId, ((SetVirtualIDCommand) message).getVirtualNodeId());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((SetVirtualIDCommand) message).getPayload());
	}

	@Test
	public void ifAreNodesAliveRequestReceivedDownstreamAreNodesAliveCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(AreNodesAliveRequest.class);
		assertTrue(message instanceof AreNodesAliveCommand);
		assertEquals(requestID, ((AreNodesAliveCommand) message).getRequestID());
		assertEquals(CommandType.NodeControl.ARE_NODES_ALIVE, ((AreNodesAliveCommand) message).getCommandType());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((AreNodesAliveCommand) message).getPayload());
	}

	@Test
	public void ifGetVersionRequestReceivedDownstreamGetVersionCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(GetVersionRequest.class);
		assertTrue(message instanceof GetVersionCommand);
		assertEquals(requestID, ((GetVersionCommand) message).getRequestID());
		assertEquals(CommandType.NodeControl.GET_VERSION, ((GetVersionCommand) message).getCommandType());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((GetVersionCommand) message).getPayload());
	}

	// Network description
	@Test
	public void ifGetPropertyValueRequestReceivedDownstreamGetPropertyValueCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(GetPropertyValueRequest.class);
		assertTrue(message instanceof GetPropertyValueCommand);
		assertEquals(requestID, ((GetPropertyValueCommand) message).getRequestID());
		assertEquals(CommandType.NetworkDescription.GET_PROPERTY_VALUE, ((GetPropertyValueCommand) message).getCommandType());
		assertEquals(property, ((GetPropertyValueCommand) message).getProperty());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((GetPropertyValueCommand) message).getPayload());
	}

	@Test
	public void ifGetNeighborhoodRequestReceivedDownstreamGetNeighborhoodCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(GetNeighborhoodRequest.class);
		assertTrue(message instanceof GetNeighborhoodCommand);
		assertEquals(requestID, ((GetNeighborhoodCommand) message).getRequestID());
		assertEquals(CommandType.NetworkDescription.GET_NEIGHBORHOOD, ((GetNeighborhoodCommand) message).getCommandType());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((GetNeighborhoodCommand) message).getPayload());
	}

	//Interaction
	@Test
	public void ifTextDataRequestReceivedDownstreamTextDataCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(TextDataRequest.class);
		assertTrue(message instanceof TextDataCommand);
		assertEquals(requestID, ((TextDataCommand) message).getRequestID());
		assertEquals(CommandType.Interaction.TEXT_DATA, ((TextDataCommand) message).getCommandType());
		assertEquals(messageLevel, ((TextDataCommand) message).getMessageLevel());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((TextDataCommand) message).getPayload());
	}

	@Test
	public void ifVirtualLinkDataRequestReceivedDownstreamVirtualLinkDataCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(VirtualLinkDataRequest.class);
		assertTrue(message instanceof VirtualLinkDataCommand);
		assertEquals(requestID, ((VirtualLinkDataCommand) message).getRequestID());
		assertEquals(CommandType.Interaction.VIRTUAL_LINK_DATA, ((VirtualLinkDataCommand) message).getCommandType());
		assertEquals(rssi, ((VirtualLinkDataCommand) message).getRssi());
		assertEquals(lqi, ((VirtualLinkDataCommand) message).getLqi());
		assertEquals(len, ((VirtualLinkDataCommand) message).getLen());
		assertEquals(destinationNode, ((VirtualLinkDataCommand) message).getDest());
		assertEquals(sourceNode, ((VirtualLinkDataCommand) message).getSource());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((VirtualLinkDataCommand) message).getPayload());
	}

	@Test
	public void ifBinaryDataRequestReceivedDownstreamBinaryDataCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(BinaryDataRequest.class);
		assertTrue(message instanceof BinaryDataCommand);
		assertEquals(requestID, ((BinaryDataCommand) message).getRequestID());
		assertEquals(CommandType.Interaction.BINARY_DATA, ((BinaryDataCommand) message).getCommandType());
		assertEquals(len, ((BinaryDataCommand) message).getLen());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((BinaryDataCommand) message).getPayload());
	}

	@Test
	public void ifFlashProgramDataRequestReceivedDownstreamFlashProgramDataCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(FlashProgramDataRequest.class);
		assertTrue(message instanceof FlashProgramDataCommand);
		assertEquals(requestID, ((FlashProgramDataCommand) message).getRequestID());
		assertEquals(CommandType.Interaction.FLASH_PROGRAM_DATA, ((FlashProgramDataCommand) message).getCommandType());
		assertEquals(len, ((FlashProgramDataCommand) message).getLen());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((FlashProgramDataCommand) message).getPayload());
	}

	@Test
	public void ifNodeOutputTextRequestReceivedDownstreamNodeOutputTextCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(NodeOutputTextRequest.class);
		assertTrue(message instanceof NodeOutputTextCommand);
		assertEquals(requestID, ((NodeOutputTextCommand) message).getRequestID());
		assertEquals(CommandType.Interaction.NODE_OUTPUT_TEXT, ((NodeOutputTextCommand) message).getCommandType());
		assertEquals(messageLevel, ((NodeOutputTextCommand) message).getMessageLevel());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((NodeOutputTextCommand) message).getPayload());
	}

	@Test
	public void ifNodeOutputBinaryRequestReceivedDownstreamNodeOutputBinaryCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(NodeOutputBinaryRequest.class);
		assertTrue(message instanceof NodeOutputBinaryCommand);
		assertEquals(requestID, ((NodeOutputBinaryCommand) message).getRequestID());
		assertEquals(CommandType.Interaction.NODE_OUTPUT_BINARY, ((NodeOutputBinaryCommand) message).getCommandType());
		assertEquals(len, ((NodeOutputBinaryCommand) message).getLen());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((NodeOutputBinaryCommand) message).getPayload());
	}

	@Test
	public void ifNodeOutputVirtualLinkRequestReceivedDownstreamNodeOutputVirtualLinkCommandIsSentDownstream() throws Exception {
		final Object message = setupActAndVerifyDownstreamMessageEvent(NodeOutputVirtualLinkRequest.class);
		assertTrue(message instanceof NodeOutputVirtualLinkCommand);
		assertEquals(requestID, ((NodeOutputVirtualLinkCommand) message).getRequestID());
		assertEquals(CommandType.Interaction.NODE_OUTPUT_VIRTUAL_LINK, ((NodeOutputVirtualLinkCommand) message).getCommandType());
		assertEquals(rssi, ((NodeOutputVirtualLinkCommand) message).getRssi());
		assertEquals(lqi, ((NodeOutputVirtualLinkCommand) message).getLqi());
		assertEquals(len, ((NodeOutputVirtualLinkCommand) message).getLen());
		assertEquals(destinationNode, ((NodeOutputVirtualLinkCommand) message).getDest());
		assertEquals(sourceNode, ((NodeOutputVirtualLinkCommand) message).getSource());
		assertEquals(ChannelBuffers.wrappedBuffer(payload), ((NodeOutputVirtualLinkCommand) message).getPayload());
	}

	private Object setupActAndVerifyUpstreamMessageEventForRequest() throws Exception {
		//set up
		final ArgumentCaptor<UpstreamMessageEvent> argumentCaptor =
				ArgumentCaptor.forClass(UpstreamMessageEvent.class);

		//act
		handler.handleUpstream(channelHandlerContext, upstreamMessageEvent);

		//verify
		verify(requestCache).get(requestID);
		verify(channelHandlerContext).sendUpstream(argumentCaptor.capture());

		return argumentCaptor.getValue().getMessage();
	}

	//test upstream requests
	//  link control
	@Test
	public void ifExpectedSetVirtualLinkCommandResponseReceivedUpstreamSetVirtualLinkResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(SetVirtualLinkRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof SetVirtualLinkResponse);
		assertSame(request, ((SetVirtualLinkResponse) message).getRequest());
		assertEquals(destinationNode, ((SetVirtualLinkResponse) message).getDestinationNode());
	}

	@Test
	public void ifExpectedDestroyVirtualLinkCommandResponseReceivedUpstreamDestroyVirtualLinkResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(DestroyVirtualLinkRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof DestroyVirtualLinkResponse);
		assertSame(request, ((DestroyVirtualLinkResponse) message).getRequest());
		assertEquals(destinationNode, ((DestroyVirtualLinkResponse) message).getDestinationNode());
	}

	@Test
	public void ifExpectedEnablePhysicalLinkCommandResponseReceivedUpstreamEnablePhysicalLinkResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(EnablePhysicalLinkRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof EnablePhysicalLinkResponse);
		assertSame(request, ((EnablePhysicalLinkResponse) message).getRequest());
		assertEquals(nodeB, ((EnablePhysicalLinkResponse) message).getNodeB());
	}

	@Test
	public void ifExpectedDisablePhysicalLinkCommandResponseReceivedUpstreamDisablePhysicalLinkResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(DisablePhysicalLinkRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof DisablePhysicalLinkResponse);
		assertSame(request, ((DisablePhysicalLinkResponse) message).getRequest());
		assertEquals(nodeB, ((DisablePhysicalLinkResponse) message).getNodeB());
	}

	//  node control
	@Test
	public void ifExpectedEnableNodeCommandResponseReceivedUpstreamEnableNodeResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(EnableNodeRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof EnableNodeResponse);
		assertSame(request, ((EnableNodeResponse) message).getRequest());
	}

	@Test
	public void ifExpectedDisableNodeCommandResponseReceivedUpstreamDisableNodeResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(DisableNodeRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof DisableNodeResponse);
		assertSame(request, ((DisableNodeResponse) message).getRequest());
	}

	@Test
	public void ifExpectedResetNodeCommandResponseReceivedUpstreamResetNodeResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(ResetNodeRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof ResetNodeResponse);
		assertSame(request, ((ResetNodeResponse) message).getRequest());
		assertEquals(time, ((ResetNodeRequest) ((ResetNodeResponse) message).getRequest()).getTime());
	}

	@Test
	public void ifExpectedSetStartTimeCommandResponseReceivedUpstreamSetStartTimeResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(SetStartTimeRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof SetStartTimeResponse);
		assertSame(request, ((SetStartTimeResponse) message).getRequest());
		assertEquals(time, ((SetStartTimeRequest) ((SetStartTimeResponse) message).getRequest()).getTime());
	}

	@Test
	public void ifExpectedSetVirtualIdCommandResponseReceivedUpstreamSetVirtualIdResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(SetVirtualIdRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof SetVirtualIdResponse);
		assertSame(request, ((SetVirtualIdResponse) message).getRequest());
		assertEquals(virtualNodeId, ((SetVirtualIdRequest) ((SetVirtualIdResponse) message).getRequest()).getVirtualNodeId());
	}

	@Test
	public void ifExpectedAreNodesAliveCommandResponseReceivedUpstreamAreNodesAliveResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(AreNodesAliveRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof AreNodesAliveResponse);
		assertSame(request, ((AreNodesAliveResponse) message).getRequest());
	}

	@Test
	public void ifExpectedGetVersionCommandResponseReceivedUpstreamGetVersionResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(GetVersionRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof GetVersionResponse);
		assertSame(request, ((GetVersionResponse) message).getRequest());
	}

	// network description
	@Test
	public void ifExpectedGetPropertyValueCommandResponseReceivedUpstreamGetPropertyValueResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(GetPropertyValueRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof GetPropertyValueResponse);
		assertSame(request, ((GetPropertyValueResponse) message).getRequest());
		assertEquals(property, ((GetPropertyValueRequest) ((GetPropertyValueResponse) message).getRequest()).getProperty());
	}

	@Test
	public void ifExpectedGetNeighborhoodCommandResponseReceivedUpstreamGetNeighborhoodResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(GetNeighborhoodRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof GetNeighborhoodResponse);
		assertSame(request, ((GetNeighborhoodResponse) message).getRequest());
	}

	//Interaction
	@Test
	public void ifExpectedTextDataCommandResponseReceivedUpstreamTextDataResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(TextDataRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof TextDataResponse);
		assertSame(request, ((TextDataResponse) message).getRequest());
		assertEquals(messageLevel, ((TextDataRequest) ((TextDataResponse) message).getRequest()).getMessageLevel());
	}

	@Test
	public void ifExpectedVirtualLinkDataCommandResponseReceivedUpstreamVirtualLinkDataResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(VirtualLinkDataRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof VirtualLinkDataResponse);
		assertSame(request, ((VirtualLinkDataResponse) message).getRequest());
		assertEquals(rssi, ((VirtualLinkDataRequest) ((VirtualLinkDataResponse) message).getRequest()).getRssi());
		assertEquals(lqi, ((VirtualLinkDataRequest) ((VirtualLinkDataResponse) message).getRequest()).getLqi());
		assertEquals(len, ((VirtualLinkDataRequest) ((VirtualLinkDataResponse) message).getRequest()).getLen());
		assertEquals(destinationNode, ((VirtualLinkDataRequest) ((VirtualLinkDataResponse) message).getRequest()).getDest());
		assertEquals(sourceNode, ((VirtualLinkDataRequest) ((VirtualLinkDataResponse) message).getRequest()).getSource());
	}

	@Test
	public void ifExpectedBinaryDataCommandResponseReceivedUpstreamBinaryDataResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(BinaryDataRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof BinaryDataResponse);
		assertSame(request, ((BinaryDataResponse) message).getRequest());
		assertEquals(len, ((BinaryDataRequest) ((BinaryDataResponse) message).getRequest()).getLen());
	}

	@Test
	public void ifExpectedFlashProgramDataCommandResponseReceivedUpstreamFlashProgramDataResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(FlashProgramDataRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof FlashProgramDataResponse);
		assertSame(request, ((FlashProgramDataResponse) message).getRequest());
		assertEquals(len, ((FlashProgramDataRequest) ((FlashProgramDataResponse) message).getRequest()).getLen());
	}

	@Test
	public void ifExpectedNodeOutputTextCommandResponseReceivedUpstreamNodeOutputTextResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(NodeOutputTextRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof NodeOutputTextResponse);
		assertSame(request, ((NodeOutputTextResponse) message).getRequest());
		assertEquals(messageLevel, ((NodeOutputTextRequest) ((NodeOutputTextResponse) message).getRequest()).getMessageLevel());
	}

	@Test
	public void ifExpectedNodeOutputBinaryCommandResponseReceivedUpstreamNodeOutputBinaryResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(NodeOutputBinaryRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof NodeOutputBinaryResponse);
		assertSame(request, ((NodeOutputBinaryResponse) message).getRequest());
		assertEquals(len, ((NodeOutputBinaryRequest) ((NodeOutputBinaryResponse) message).getRequest()).getLen());
	}

	@Test
	public void ifExpectedNodeOutputVirtualLinkCommandResponseReceivedUpstreamNodeOutputVirtualLinkResponseIsSentUpstream() throws Exception {
		//set up
		Request request = createAndReturnRequestAndSetupMessageEvent(NodeOutputVirtualLinkRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForRequest();

		assertTrue(message instanceof NodeOutputVirtualLinkResponse);
		assertSame(request, ((NodeOutputVirtualLinkResponse) message).getRequest());
		assertEquals(rssi, ((NodeOutputVirtualLinkRequest) ((NodeOutputVirtualLinkResponse) message).getRequest()).getRssi());
		assertEquals(lqi, ((NodeOutputVirtualLinkRequest) ((NodeOutputVirtualLinkResponse) message).getRequest()).getLqi());
		assertEquals(len, ((NodeOutputVirtualLinkRequest) ((NodeOutputVirtualLinkResponse) message).getRequest()).getLen());
		assertEquals(destinationNode, ((NodeOutputVirtualLinkRequest) ((NodeOutputVirtualLinkResponse) message).getRequest()).getDest());
		assertEquals(sourceNode, ((NodeOutputVirtualLinkRequest) ((NodeOutputVirtualLinkResponse) message).getRequest()).getSource());
	}

	private Object setupActAndVerifyUpstreamMessageEventForTimeOut(Request request) throws Exception {
		final ArgumentCaptor<UpstreamMessageEvent> argumentCaptor =
				ArgumentCaptor.forClass(UpstreamMessageEvent.class);

		// act
		final NodeAPIHandler.RequestCacheEntry requestCacheEntry = new NodeAPIHandler.RequestCacheEntry(request, channel, channelHandlerContext, remoteAddress);
		handler.getRequestCacheListener().timeout(requestID, requestCacheEntry);

		// verify
		verify(channelHandlerContext).sendUpstream(argumentCaptor.capture());

		return argumentCaptor.getValue().getMessage();
	}

	//test upstream timeout
	//  link control
	@Test
	public void testTimeOutOccurrenceForSetVirtualLinkResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(SetVirtualLinkRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof SetVirtualLinkRequest);
		assertEquals(destinationNode, ((SetVirtualLinkRequest) ((TimeOutResponse) message).getRequest()).getDestinationNode());
	}

	@Test
	public void testTimeOutOccurrenceForDestroyVirtualLinkResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(DestroyVirtualLinkRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof DestroyVirtualLinkRequest);
		assertEquals(destinationNode, ((DestroyVirtualLinkRequest) ((TimeOutResponse) message).getRequest()).getDestinationNode());
	}

	@Test
	public void testTimeOutOccurrenceForEnablePhysicalLinkResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(EnablePhysicalLinkRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof EnablePhysicalLinkRequest);
		assertEquals(nodeB, ((EnablePhysicalLinkRequest) ((TimeOutResponse) message).getRequest()).getNodeB());
	}

	@Test
	public void testTimeOutOccurrenceForDisablePhysicalLinkResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(DisablePhysicalLinkRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof DisablePhysicalLinkRequest);
		assertEquals(nodeB, ((DisablePhysicalLinkRequest) ((TimeOutResponse) message).getRequest()).getNodeB());
	}

	//node control
	@Test
	public void testTimeOutOccurrenceForEnableNodeResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(EnableNodeRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof EnableNodeRequest);
	}

	@Test
	public void testTimeOutOccurrenceForDisableNodeResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(DisableNodeRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof DisableNodeRequest);
	}

	@Test
	public void testTimeOutOccurrenceForResetNodeResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(ResetNodeRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof ResetNodeRequest);
		assertEquals(time, ((ResetNodeRequest) ((TimeOutResponse) message).getRequest()).getTime());
	}

	@Test
	public void testTimeOutOccurrenceForSetStartTimeResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(SetStartTimeRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof SetStartTimeRequest);
		assertEquals(time, ((SetStartTimeRequest) ((TimeOutResponse) message).getRequest()).getTime());
	}

	@Test
	public void testTimeOutOccurrenceForSetVirtualIdResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(SetVirtualIdRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof SetVirtualIdRequest);
		assertEquals(virtualNodeId, ((SetVirtualIdRequest) ((TimeOutResponse) message).getRequest()).getVirtualNodeId());
	}

	@Test
	public void testTimeOutOccurrenceForAreNodesAliveResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(AreNodesAliveRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof AreNodesAliveRequest);
	}

	@Test
	public void testTimeOutOccurrenceForGetVersionResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(GetVersionRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof GetVersionRequest);
	}

	// network description
	@Test
	public void testTimeOutOccurrenceForGetPropertyValueResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(GetPropertyValueRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof GetPropertyValueRequest);
		assertEquals(property, ((GetPropertyValueRequest) ((TimeOutResponse) message).getRequest()).getProperty());
	}

	@Test
	public void testTimeOutOccurrenceForGetNeighborhoodResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(GetNeighborhoodRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof GetNeighborhoodRequest);
	}

	// interaction
	@Test
	public void testTimeOutOccurrenceForTextDataResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(TextDataRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof TextDataRequest);
		assertEquals(messageLevel, ((TextDataRequest) ((TimeOutResponse) message).getRequest()).getMessageLevel());
	}

	@Test
	public void testTimeOutOccurrenceForVirtualLinkDataResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(VirtualLinkDataRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof VirtualLinkDataRequest);
		assertEquals(rssi, ((VirtualLinkDataRequest) ((TimeOutResponse) message).getRequest()).getRssi());
		assertEquals(lqi, ((VirtualLinkDataRequest) ((TimeOutResponse) message).getRequest()).getLqi());
		assertEquals(len, ((VirtualLinkDataRequest) ((TimeOutResponse) message).getRequest()).getLen());
		assertEquals(destinationNode, ((VirtualLinkDataRequest) ((TimeOutResponse) message).getRequest()).getDest());
		assertEquals(sourceNode, ((VirtualLinkDataRequest) ((TimeOutResponse) message).getRequest()).getSource());
	}

	@Test
	public void testTimeOutOccurrenceForBinaryDataResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(BinaryDataRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof BinaryDataRequest);
		assertEquals(len, ((BinaryDataRequest) ((TimeOutResponse) message).getRequest()).getLen());
	}

	@Test
	public void testTimeOutOccurrenceForFlashProgramDataResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(FlashProgramDataRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof FlashProgramDataRequest);
		assertEquals(len, ((FlashProgramDataRequest) ((TimeOutResponse) message).getRequest()).getLen());
	}

	@Test
	public void testTimeOutOccurrenceForNodeOutputTextResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(NodeOutputTextRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof NodeOutputTextRequest);
		assertEquals(messageLevel, ((NodeOutputTextRequest) ((TimeOutResponse) message).getRequest()).getMessageLevel());
	}

	@Test
	public void testTimeOutOccurrenceForNodeOutputBinaryResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(NodeOutputBinaryRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof NodeOutputBinaryRequest);
		assertEquals(len, ((NodeOutputBinaryRequest) ((TimeOutResponse) message).getRequest()).getLen());
	}

	@Test
	public void testTimeOutOccurrenceForNodeOutputVirtualLinkResponseSentUpstream() throws Exception {
		// setup
		Request request = createAndReturnRequestAndSetupMessageEvent(NodeOutputVirtualLinkRequest.class, upstreamMessageEvent);
		final Object message = setupActAndVerifyUpstreamMessageEventForTimeOut(request);

		assertTrue(message instanceof TimeOutResponse);
		assertSame(request, (((TimeOutResponse) message).getRequest()));
		assertTrue(((TimeOutResponse) message).getRequest() instanceof NodeOutputVirtualLinkRequest);
		assertEquals(rssi, ((NodeOutputVirtualLinkRequest) ((TimeOutResponse) message).getRequest()).getRssi());
		assertEquals(lqi, ((NodeOutputVirtualLinkRequest) ((TimeOutResponse) message).getRequest()).getLqi());
		assertEquals(len, ((NodeOutputVirtualLinkRequest) ((TimeOutResponse) message).getRequest()).getLen());
		assertEquals(destinationNode, ((NodeOutputVirtualLinkRequest) ((TimeOutResponse) message).getRequest()).getDest());
		assertEquals(sourceNode, ((NodeOutputVirtualLinkRequest) ((TimeOutResponse) message).getRequest()).getSource());
	}

}
