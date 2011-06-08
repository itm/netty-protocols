package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import de.uniluebeck.itm.tr.util.TimedCache;
import org.jboss.netty.channel.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.SocketAddress;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NodeAPIHandlerTest {

	private NodeAPIHandler handler;

	@Mock
	private ChannelHandlerContext channelHandlerContext;

	@Mock
	private DownstreamMessageEvent messageEvent;

	@Mock
	private Channel channel;

	@Mock
	private SocketAddress remoteAddress;

	@Mock
	private ChannelFuture future;

	@Mock
	private Map<Long, Request> requestCache;

	@Before
	public void setUp() throws Exception {
		handler = (NodeAPIHandler) Guice.createInjector(new Module() {
			@Override
			public void configure(final Binder binder) {
				binder.bind(new TypeLiteral<Map<Long, Request>>(){})
						.annotatedWith(Names.named("requestCache"))
						.toInstance(requestCache);

				binder.bind(ChannelHandler.class).to(NodeAPIHandler.class);
			}
		}
		).getInstance(ChannelHandler.class);
	}

	@Test
	public void ifSetVirtualLinkRequestReceivedDownstreamSetVirtualLinkCommandIsSentDownstream() throws Exception {

		// setup
		final long destinationNode = 0x1234;
		SetVirtualLinkRequest request = new SetVirtualLinkRequest(destinationNode);

		when(messageEvent.getMessage()).thenReturn(request);

		when(messageEvent.getChannel()).thenReturn(channel);
		when(messageEvent.getRemoteAddress()).thenReturn(remoteAddress);
		when(messageEvent.getFuture()).thenReturn(future);
		doNothing().when(channelHandlerContext).sendDownstream(Matchers.<DownstreamMessageEvent>any());

		final ArgumentCaptor<DownstreamMessageEvent> argumentCaptor =
				ArgumentCaptor.forClass(DownstreamMessageEvent.class);

		// act
		handler.handleDownstream(channelHandlerContext, messageEvent);

		// verify
		verify(channelHandlerContext).sendDownstream(argumentCaptor.capture());

		final Object message = argumentCaptor.getValue().getMessage();
		assertTrue(message instanceof SetVirtualLinkCommand);
		assertEquals(destinationNode, ((SetVirtualLinkCommand) message).getDestinationNode());
	}

}
