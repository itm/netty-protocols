package de.uniluebeck.itm.netty.handlerstack;

import com.google.common.collect.Lists;
import de.uniluebeck.itm.netty.handlerstack.discard.DiscardHandler;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static de.uniluebeck.itm.netty.handlerstack.util.ChannelBufferTools.getToByteArray;
import static org.jboss.netty.channel.Channels.future;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FilterPipelineTest {

	/**
	 * Helper interface that allows to create mocks of lifecycle-aware ChannelHandler mocks in unit tests.
	 */
	private static interface LifeCycleAwareDownstreamChannelHandler extends LifeCycleAwareChannelHandler,
			ChannelDownstreamHandler {

	}

	/**
	 * The same as a SimpleChannelHandler. Using the name PassThroughChannelHandler only to illustrate the purpose of
	 * its usage in this unit tests which is "passing through" as is the default behaviour of SimpleChannelHandler.
	 */
	private static class PassThroughChannelHandler extends SimpleChannelHandler {

	}

	/**
	 * The FilterPipeline under test.
	 */
	private FilterPipeline pipeline;

	@Mock
	private ChannelSink channelSinkMock;

	@Mock
	private ChannelDownstreamHandler downstreamHandlerMock;

	@Mock
	private ChannelUpstreamHandler upstreamHandlerMock;

	private PassThroughChannelHandler passThroughChannelHandler1;

	private PassThroughChannelHandler passThroughChannelHandler2;

	private PassThroughChannelHandler passThroughChannelHandler3;

	private ArgumentCaptor<ChannelEvent> channelEventCaptor;

	private InetSocketAddress messageRemoteAddress;

	private UpstreamMessageEvent upstreamMessageEvent;

	private DownstreamMessageEvent downstreamMessageEvent;

	private ChannelBuffer messageBuffer;

	private byte[] messageBytes;

	@Before
	public void setUp() throws Exception {

		pipeline = new FilterPipelineImpl();

		channelEventCaptor = ArgumentCaptor.forClass(ChannelEvent.class);

		passThroughChannelHandler1 = new PassThroughChannelHandler();
		passThroughChannelHandler2 = new PassThroughChannelHandler();
		passThroughChannelHandler3 = new PassThroughChannelHandler();

		messageRemoteAddress = new InetSocketAddress("localhost", 1234);
		messageBytes = "Hello, World".getBytes();
		messageBuffer = ChannelBuffers.wrappedBuffer(messageBytes);
		upstreamMessageEvent = new UpstreamMessageEvent(pipeline.getChannel(), messageBuffer, messageRemoteAddress);
		downstreamMessageEvent = new DownstreamMessageEvent(
				pipeline.getChannel(),
				future(pipeline.getChannel()),
				messageBuffer,
				messageRemoteAddress
		);
	}

	@Test
	public void testIfMessageSentUpstreamIsReceivedByUpstreamListenerWithNoHandlers() throws Exception {
		
		assertThatMessageSentUpstreamIsReceivedByUpstreamListener();
	}

	@Test
	public void testIfMessageSentUpstreamIsReceivedByUpstreamListenerWithOneHandler() throws Exception {

		pipeline.addFirst("first", passThroughChannelHandler1);

		assertThatMessageSentUpstreamIsReceivedByUpstreamListener();
	}

	@Test
	public void testIfMessageSentUpstreamIsReceivedByUpstreamListenerWithTwoHandlers() throws Exception {

		pipeline.addLast("first", passThroughChannelHandler1);
		pipeline.addLast("second", passThroughChannelHandler2);

		assertThatMessageSentUpstreamIsReceivedByUpstreamListener();
	}

	@Test
	public void testIfMessageSentUpstreamIsReceivedByUpstreamListenerWithThreeHandlers() throws Exception {

		pipeline.addLast("first", passThroughChannelHandler1);
		pipeline.addLast("second", passThroughChannelHandler2);
		pipeline.addLast("third", passThroughChannelHandler3);

		assertThatMessageSentUpstreamIsReceivedByUpstreamListener();
	}

	@Test
	public void testIfMessageSentDownstreamIsReceivedWithNoHandlers() throws Exception {
		assertThatMessageSentDownstreamIsReceivedByDownstreamListener();
	}

	@Test
	public void testIfMessageSentDownstreamIsReceivedWithOneHandler() throws Exception {

		pipeline.addLast("first", passThroughChannelHandler1);
		assertThatMessageSentDownstreamIsReceivedByDownstreamListener();
	}

	@Test
	public void testIfMessageSentDownstreamIsReceivedWithTwoHandlers() throws Exception {

		pipeline.addLast("first", passThroughChannelHandler1);
		pipeline.addLast("second", passThroughChannelHandler2);
		assertThatMessageSentDownstreamIsReceivedByDownstreamListener();
	}

	@Test
	public void testIfMessageSentDownstreamIsReceivedWithThreeHandlers() throws Exception {

		pipeline.addLast("first", passThroughChannelHandler1);
		pipeline.addLast("second", passThroughChannelHandler2);
		pipeline.addLast("third", passThroughChannelHandler3);
		assertThatMessageSentDownstreamIsReceivedByDownstreamListener();
	}

	@Test
	public void testIfAllEventsAreSwallowedWhenDiscardHandlerIsSet() throws Exception {

		final List<Tuple<String, ChannelHandler>> discardPipeline = newArrayList(
				new Tuple<String, ChannelHandler>("upstreamHandler", upstreamHandlerMock),
				new Tuple<String, ChannelHandler>("discard", new DiscardHandler(true, true)),
				new Tuple<String, ChannelHandler>("downstreamHandler", downstreamHandlerMock)
		);

		pipeline.setChannelPipeline(discardPipeline);
		System.out.println(pipeline.toMap());

		pipeline.sendUpstream(new UpstreamMessageEvent(
				pipeline.getChannel(),
				messageBuffer,
				messageRemoteAddress
		)
		);

		verify(upstreamHandlerMock, never()).handleUpstream(
				Matchers.<ChannelHandlerContext>any(),
				Matchers.<ChannelEvent>any()
		);

		pipeline.sendDownstream(new DownstreamMessageEvent(
				pipeline.getChannel(),
				future(pipeline.getChannel()),
				messageBuffer,
				messageRemoteAddress
		)
		);

		verify(downstreamHandlerMock, never()).handleDownstream(
				Matchers.<ChannelHandlerContext>any(),
				Matchers.<ChannelEvent>any()
		);
	}

	@Test
	public void testIfUpstreamEventsArePassedThroughOneHandler() throws Exception {

		final SimpleChannelHandler handlerMock = mock(SimpleChannelHandler.class);
		pipeline.addLast("first", handlerMock);
		assertThatUpstreamEventIsPassedThroughHandlerMock(handlerMock);
	}

	@Test
	public void testIfUpstreamEventsArePassedThroughTwoHandlers() throws Exception {

		final SimpleChannelHandler handlerMock = mock(SimpleChannelHandler.class);
		pipeline.addLast("first", handlerMock);
		pipeline.addLast("second", passThroughChannelHandler1);
		assertThatUpstreamEventIsPassedThroughHandlerMock(handlerMock);
	}

	@Test
	public void testIfDownstreamEventsArePassedThroughOneHandler() throws Exception {

		final SimpleChannelHandler handlerMock = mock(SimpleChannelHandler.class);
		pipeline.addLast("first", handlerMock);
		assertThatDownstreamEventIsPassedThroughHandlerMock(handlerMock);
	}

	@Test
	public void testIfDownstreamEventsArePassedThroughTwoHandlers() throws Exception {

		final SimpleChannelHandler handlerMock = mock(SimpleChannelHandler.class);
		pipeline.addLast("second", handlerMock);
		pipeline.addLast("first", passThroughChannelHandler1);
		assertThatDownstreamEventIsPassedThroughHandlerMock(handlerMock);
	}

	@Test
	public void testIfUpstreamMessageEventArrivesAtTheTopWhenUsedInsideAnOuterPipeline() throws Exception {

		pipeline.addLast("first", passThroughChannelHandler1);
		pipeline.addLast("second", passThroughChannelHandler2);
		pipeline.addLast("third", passThroughChannelHandler3);

		DecoderEmbedder<ChannelBuffer> embedder = new DecoderEmbedder<ChannelBuffer>(pipeline);

		assertNull(embedder.peek());

		// DecoderEmbedder writes message to the pipeline via sendUpstream
		embedder.offer(messageBuffer);

		assertTrue(messageBuffer == embedder.poll());
	}

	@Test
	public void testIfDownstreamMessageEventArrivesAtTheBottomWhenUsedInsideAnOuterPipeline() throws Exception {

		pipeline.addLast("first", passThroughChannelHandler1);
		pipeline.addLast("second", passThroughChannelHandler2);
		pipeline.addLast("third", passThroughChannelHandler3);

		EncoderEmbedder<ChannelBuffer> embedder = new EncoderEmbedder<ChannelBuffer>(pipeline);

		assertNull(embedder.peek());

		// EncoderEmbedder writes message to the pipeline via sendDownstream
		embedder.offer(messageBuffer);

		assertTrue(messageBuffer == embedder.poll());
	}

	@Test
	public void testIfLifeCycleMethodsAreCalledAndInCorrectOrder() throws Exception {

		final LifeCycleAwareDownstreamChannelHandler mockHandler = mock(LifeCycleAwareDownstreamChannelHandler.class);
		final InOrder inOrder = inOrder(mockHandler);

		verify(mockHandler, never()).beforeAdd(Matchers.<ChannelHandlerContext>any());
		verify(mockHandler, never()).afterAdd(Matchers.<ChannelHandlerContext>any());
		verify(mockHandler, never()).beforeRemove(Matchers.<ChannelHandlerContext>any());
		verify(mockHandler, never()).afterRemove(Matchers.<ChannelHandlerContext>any());

		pipeline.setChannelPipeline(newArrayList(
				new Tuple<String, ChannelHandler>("mock", mockHandler)
		)
		);

		verify(mockHandler, never()).beforeRemove(Matchers.<ChannelHandlerContext>any());
		verify(mockHandler, never()).afterRemove(Matchers.<ChannelHandlerContext>any());

		inOrder.verify(mockHandler).beforeAdd(Matchers.<ChannelHandlerContext>any());
		inOrder.verify(mockHandler).afterAdd(Matchers.<ChannelHandlerContext>any());

		pipeline.setChannelPipeline(Lists.<Tuple<String, ChannelHandler>>newArrayList());

		inOrder.verify(mockHandler).beforeRemove(Matchers.<ChannelHandlerContext>any());
		inOrder.verify(mockHandler).afterRemove(Matchers.<ChannelHandlerContext>any());
	}

	private void assertThatUpstreamEventIsPassedThroughHandlerMock(final SimpleChannelHandler handlerMock)
			throws Exception {

		final UpstreamChannelStateEvent channelStateEvent = new UpstreamChannelStateEvent(
				pipeline.getChannel(),
				ChannelState.CONNECTED,
				true
		);

		verify(handlerMock, never())
				.handleUpstream(Matchers.<ChannelHandlerContext>any(), Matchers.<ChannelEvent>any());

		pipeline.sendUpstream(channelStateEvent);

		verify(handlerMock)
				.handleUpstream(Matchers.<ChannelHandlerContext>any(), eq(channelStateEvent));
	}

	private void assertThatDownstreamEventIsPassedThroughHandlerMock(final SimpleChannelHandler handlerMock)
			throws Exception {

		final DownstreamChannelStateEvent channelStateEvent = new DownstreamChannelStateEvent(
				pipeline.getChannel(),
				future(pipeline.getChannel()),
				ChannelState.CONNECTED,
				true
		);

		verify(handlerMock, never())
				.handleDownstream(Matchers.<ChannelHandlerContext>any(), Matchers.<ChannelEvent>any());

		pipeline.sendDownstream(channelStateEvent);

		verify(handlerMock)
				.handleDownstream(Matchers.<ChannelHandlerContext>any(), eq(channelStateEvent));
	}

	private void assertThatMessageSentUpstreamIsReceivedByUpstreamListener() throws Exception {

		// place mock handler on the very top of the stack
		pipeline.addLast("upstreamHandlerMock", upstreamHandlerMock);

		// send message upstream from bottom
		pipeline.sendUpstream(upstreamMessageEvent);

		verify(upstreamHandlerMock)
				.handleUpstream(
						Matchers.<ChannelHandlerContext>any(),
						channelEventCaptor.capture()
				);

		final ChannelEvent capturedChannelEvent = channelEventCaptor.getValue();
		assertNotNull(capturedChannelEvent);
		assertTrue(capturedChannelEvent instanceof UpstreamMessageEvent);

		final Object actualMessageObject = ((UpstreamMessageEvent) capturedChannelEvent).getMessage();
		assertCapturedMessageEqualsMessageSent(messageBytes, actualMessageObject);

		final SocketAddress actualRemoteAddress = ((UpstreamMessageEvent) capturedChannelEvent).getRemoteAddress();
		assertCapturedRemoteAddressEqualsRemoteAddressSent(messageRemoteAddress, actualRemoteAddress);
	}

	private void assertThatMessageSentDownstreamIsReceivedByDownstreamListener() throws Exception {

		// place mock handler on the very bottom of the stack
		pipeline.addFirst("downstreamHandlerMock", downstreamHandlerMock);

		// send message downstream from top
		pipeline.sendDownstream(downstreamMessageEvent);

		verify(downstreamHandlerMock)
				.handleDownstream(
						Matchers.<ChannelHandlerContext>any(),
						channelEventCaptor.capture()
				);

		assertCapturedMessageEqualsMessageSentDownstream(messageBytes, messageRemoteAddress, channelEventCaptor);
	}

	private static void assertCapturedMessageEqualsMessageSent(final byte[] expectedMessageBytes,
															   final Object actualMessageObject) {
		assertNotNull(actualMessageObject);
		assertTrue(actualMessageObject instanceof ChannelBuffer);
		assertArrayEquals(expectedMessageBytes, getToByteArray((ChannelBuffer) actualMessageObject));
	}

	private static void assertCapturedRemoteAddressEqualsRemoteAddressSent(final SocketAddress expectedRemoteAddress,
																		   final SocketAddress actualRemoteAddress) {
		assertNotNull(actualRemoteAddress);
		assertTrue(actualRemoteAddress instanceof InetSocketAddress);
		assertEquals(expectedRemoteAddress, actualRemoteAddress);
	}

	private static void assertCapturedMessageEqualsMessageSentDownstream(
			final byte[] expectedMessageBytes, final SocketAddress expectedRemoteAddress,
			final ArgumentCaptor<ChannelEvent> channelEventCaptor) {

		final ChannelEvent capturedChannelEvent = channelEventCaptor.getValue();
		assertNotNull(capturedChannelEvent);
		assertTrue(capturedChannelEvent instanceof DownstreamMessageEvent);

		final Object actualMessageObject = ((DownstreamMessageEvent) capturedChannelEvent).getMessage();
		assertCapturedMessageEqualsMessageSent(expectedMessageBytes, actualMessageObject);

		final SocketAddress actualRemoteAddress = ((DownstreamMessageEvent) capturedChannelEvent).getRemoteAddress();
		assertCapturedRemoteAddressEqualsRemoteAddressSent(expectedRemoteAddress, actualRemoteAddress);
	}
}
