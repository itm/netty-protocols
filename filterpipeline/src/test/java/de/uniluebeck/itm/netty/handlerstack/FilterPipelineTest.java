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
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

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
	private Channel channelMock;

	@Mock
	private ChannelSink channelSinkMock;

	@Spy
	private PassThroughChannelHandler handler1 = new PassThroughChannelHandler();

	@Spy
	private PassThroughChannelHandler handler2 = new PassThroughChannelHandler();

	@Spy
	private PassThroughChannelHandler handler3 = new PassThroughChannelHandler();

	@Spy
	private DiscardHandler discardHandler = new DiscardHandler(true, true);

	private InetSocketAddress messageRemoteAddress;

	private UpstreamMessageEvent upstreamMessageEvent;

	private DownstreamMessageEvent downstreamMessageEvent;

	private ChannelBuffer messageBuffer;

	private byte[] messageBytes;

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);

		pipeline = new FilterPipelineImpl();

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
	public void testIfMessageSentUpstreamIsReceivedOneHandler() throws Exception {

		pipeline.addFirst("first", handler1);

		assertThatMessageSentUpstreamIsReceivedByUpstreamListener(handler1);
	}

	@Test
	public void testIfMessageSentUpstreamIsReceivedByTwoHandlersInCorrectOrder() throws Exception {

		pipeline.addFirst("second", handler2);
		pipeline.addFirst("first", handler1);

		assertThatMessageSentUpstreamIsReceivedByUpstreamListener(handler1, handler2);
	}

	@Test
	public void testIfMessageSentUpstreamIsReceivedByThreeHandlersInCorrectOrder() throws Exception {

		pipeline.addFirst("third", handler3);
		pipeline.addFirst("second", handler2);
		pipeline.addFirst("first", handler1);

		assertThatMessageSentUpstreamIsReceivedByUpstreamListener(handler1, handler2, handler3);
	}

	@Test
	public void testIfMessageSentDownstreamIsReceivedWithOneHandler() throws Exception {

		pipeline.addFirst("first", handler1);

		assertThatMessageSentDownstreamIsReceivedByDownstreamListener(handler1);
	}

	@Test
	public void testIfMessageSentDownstreamIsReceivedWithTwoHandlers() throws Exception {

		pipeline.addFirst("second", handler2);
		pipeline.addFirst("first", handler1);

		assertThatMessageSentDownstreamIsReceivedByDownstreamListener(handler2, handler1);
	}

	@Test
	public void testIfMessageSentDownstreamIsReceivedWithThreeHandlers() throws Exception {

		pipeline.addFirst("third", handler3);
		pipeline.addFirst("second", handler2);
		pipeline.addFirst("first", handler1);

		assertThatMessageSentDownstreamIsReceivedByDownstreamListener(handler3, handler2, handler1);
	}

	@Test
	public void testIfAllUpstreamEventsAreSwallowedWhenDiscardHandlerIsSet() throws Exception {

		pipeline.addFirst("handler1", handler1);
		pipeline.addFirst("discardHandler", discardHandler);
		pipeline.addFirst("handler2", handler2);

		pipeline.sendUpstream(upstreamMessageEvent);

		InOrder upstreamOrder = inOrder(handler2, discardHandler);

		upstreamOrder.verify(handler2).handleUpstream(
				Matchers.<ChannelHandlerContext>any(),
				eq(upstreamMessageEvent)
		);

		upstreamOrder.verify(discardHandler).handleUpstream(
				Matchers.<ChannelHandlerContext>any(),
				eq(upstreamMessageEvent)
		);

		verify(handler1, never()).handleUpstream(Matchers.<ChannelHandlerContext>any(), Matchers.<ChannelEvent>any());

	}

	@Test
	public void testIfAllDownstreamEventsAreSwallowedWhenDiscardHandlerIsSet() throws Exception {

		pipeline.addFirst("handler1", handler1);
		pipeline.addFirst("discardHandler", discardHandler);
		pipeline.addFirst("handler2", handler2);

		pipeline.sendDownstream(downstreamMessageEvent);

		InOrder downstreamOrder = inOrder(handler1, discardHandler);

		downstreamOrder.verify(handler1).handleDownstream(
				Matchers.<ChannelHandlerContext>any(),
				eq(downstreamMessageEvent)
		);
		
		downstreamOrder.verify(discardHandler).handleDownstream(
				Matchers.<ChannelHandlerContext>any(),
				eq(downstreamMessageEvent)
		);

		verify(handler2, never()).handleDownstream(Matchers.<ChannelHandlerContext>any(), Matchers.<ChannelEvent>any());

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
		pipeline.addLast("second", handler1);
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
		pipeline.addLast("first", handler1);
		assertThatDownstreamEventIsPassedThroughHandlerMock(handlerMock);
	}

	@Test
	public void testIfUpstreamMessageEventArrivesAtTheTopWhenUsedInsideAnOuterPipeline() throws Exception {

		pipeline.addLast("first", handler1);
		pipeline.addLast("second", handler2);
		pipeline.addLast("third", handler3);

		DecoderEmbedder<ChannelBuffer> embedder = new DecoderEmbedder<ChannelBuffer>(pipeline);

		assertNull(embedder.peek());

		// DecoderEmbedder writes message to the pipeline via sendUpstream
		embedder.offer(messageBuffer);

		assertTrue(messageBuffer == embedder.poll());
	}

	@Test
	public void testIfDownstreamMessageEventArrivesAtTheBottomWhenUsedInsideAnOuterPipeline() throws Exception {

		pipeline.addLast("first", handler1);
		pipeline.addLast("second", handler2);
		pipeline.addLast("third", handler3);

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

	@Test
	public void testIfFilterPipelineCanBeUsedAsPipelineButNotAsHandlerAtTheSameTime() throws Exception {

		pipeline.attach(channelMock, channelSinkMock);

		try {
			pipeline.beforeAdd(mock(ChannelHandlerContext.class));
			fail();
		} catch (IllegalStateException expected) {
		}

		try {
			pipeline.afterAdd(mock(ChannelHandlerContext.class));
			fail();
		} catch (IllegalStateException expected) {
		}

		try {
			pipeline.beforeRemove(mock(ChannelHandlerContext.class));
			fail();
		} catch (IllegalStateException expected) {
		}

		try {
			pipeline.afterRemove(mock(ChannelHandlerContext.class));
			fail();
		} catch (IllegalStateException expected) {
		}

	}

	@Test
	public void testIfFilterPipelineCantBeAttachedTwice() throws Exception {

		pipeline.attach(channelMock, channelSinkMock);

		try {
			pipeline.attach(mock(Channel.class), mock(ChannelSink.class));
			fail();
		} catch (IllegalStateException expected) {
		}

	}

	@Test
	public void testIfFilterPipelineCantBeAttachedWhenUsedAsHandler() throws Exception {

		ChannelHandlerContext mockContext = mock(ChannelHandlerContext.class);

		pipeline.beforeAdd(mockContext);
		pipeline.afterAdd(mockContext);

		try {
			pipeline.attach(channelMock, channelSinkMock);
			fail();
		} catch (IllegalStateException e) {
		}

		pipeline.beforeRemove(mockContext);
		pipeline.afterRemove(mockContext);

		pipeline.attach(channelMock, channelSinkMock);

	}

	@Test
	public void testIfFilterPipelineCantBeUsedAsHandlerTwice() throws Exception {

		ChannelHandlerContext mockContext = mock(ChannelHandlerContext.class);

		pipeline.beforeAdd(mockContext);
		pipeline.afterAdd(mockContext);

		ChannelHandlerContext mockContext2 = mock(ChannelHandlerContext.class);

		try {
			pipeline.beforeAdd(mockContext2);
			fail();
		} catch (IllegalStateException e) {
		}

		try {
			pipeline.afterAdd(mockContext2);
			fail();
		} catch (IllegalStateException e) {
		}

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

	private void assertThatMessageSentUpstreamIsReceivedByUpstreamListener(PassThroughChannelHandler... mocks)
			throws Exception {

		// send message upstream from bottom
		pipeline.sendUpstream(upstreamMessageEvent);

		InOrder inOrder = inOrder(mocks);

		for (PassThroughChannelHandler mock : mocks) {

			ArgumentCaptor<ChannelEvent> channelEventCaptor = ArgumentCaptor.forClass(ChannelEvent.class);

			inOrder.verify(mock).handleUpstream(Matchers.<ChannelHandlerContext>any(), channelEventCaptor.capture());

			assertCapturedMessageEqualsMessageSentUpstream(messageBytes, messageRemoteAddress, channelEventCaptor);
		}
	}

	private void assertThatMessageSentDownstreamIsReceivedByDownstreamListener(PassThroughChannelHandler... mocks)
			throws Exception {

		// send message downstream from top
		pipeline.sendDownstream(downstreamMessageEvent);

		InOrder inOrder = inOrder(mocks);

		for (PassThroughChannelHandler mock : mocks) {

			ArgumentCaptor<ChannelEvent> channelEventCaptor = ArgumentCaptor.forClass(ChannelEvent.class);

			inOrder.verify(mock).handleDownstream(Matchers.<ChannelHandlerContext>any(), channelEventCaptor.capture());

			assertCapturedMessageEqualsMessageSentDownstream(messageBytes, messageRemoteAddress, channelEventCaptor);

		}
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

	private void assertCapturedMessageEqualsMessageSentUpstream(final byte[] expectedMessageBytes,
																final InetSocketAddress expectedRemoteAddress,
																final ArgumentCaptor<ChannelEvent> channelEventCaptor) {
		final ChannelEvent capturedChannelEvent = channelEventCaptor.getValue();
		assertNotNull(capturedChannelEvent);
		assertTrue(capturedChannelEvent instanceof UpstreamMessageEvent);

		final Object actualMessageObject = ((UpstreamMessageEvent) capturedChannelEvent).getMessage();
		assertCapturedMessageEqualsMessageSent(expectedMessageBytes, actualMessageObject);

		final SocketAddress actualRemoteAddress = ((UpstreamMessageEvent) capturedChannelEvent).getRemoteAddress();
		assertCapturedRemoteAddressEqualsRemoteAddressSent(expectedRemoteAddress, actualRemoteAddress);
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
}
