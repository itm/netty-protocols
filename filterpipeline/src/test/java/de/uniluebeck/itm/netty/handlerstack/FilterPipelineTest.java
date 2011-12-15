package de.uniluebeck.itm.netty.handlerstack;

import com.google.common.collect.Lists;
import de.uniluebeck.itm.netty.handlerstack.discard.DiscardHandler;
import de.uniluebeck.itm.tr.util.Logging;
import de.uniluebeck.itm.tr.util.Tuple;
import org.apache.log4j.Level;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static de.uniluebeck.itm.netty.handlerstack.util.ChannelBufferTools.getToByteArray;
import static org.jboss.netty.channel.Channels.future;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
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
	private Channel outerChannelMock;

	@Mock
	private ChannelPipeline outerChannelPipelineMock;

	@Mock
	private ChannelSink outerChannelSinkMock;
	
	@Mock
	private ChannelHandlerContext outerChannelHandlerContextMock;

	@Spy
	private PassThroughChannelHandler handler1 = new PassThroughChannelHandler();

	@Spy
	private PassThroughChannelHandler handler2 = new PassThroughChannelHandler();

	@Spy
	private PassThroughChannelHandler handler3 = new PassThroughChannelHandler();

	@Spy
	private DiscardHandler discardHandler = new DiscardHandler(true, true);

	private InetSocketAddress messageRemoteAddress;

	private ChannelBuffer messageBuffer;

	private byte[] messageBytes;

	private boolean asPipeline;

	@Parameterized.Parameters
	public static Collection<Object[]> getAsPipelineParameter() {
		return newArrayList(new Object[]{true}, new Object[]{false});
	}

	public FilterPipelineTest(final boolean asPipeline) {
		this.asPipeline = asPipeline;
	}

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);
		Logging.setLoggingDefaults(Level.WARN);

		pipeline = new FilterPipelineImpl();

		messageRemoteAddress = new InetSocketAddress("localhost", 1234);
		messageBytes = "Hello, World".getBytes();
		messageBuffer = ChannelBuffers.wrappedBuffer(messageBytes);
	}

	private DownstreamMessageEvent createDownstreamMessageEvent() {
		return new DownstreamMessageEvent(
				pipeline.getChannel(),
				future(pipeline.getChannel()),
				messageBuffer,
				messageRemoteAddress
		);
	}

	private UpstreamMessageEvent createUpstreamMessageEvent() {
		return new UpstreamMessageEvent(pipeline.getChannel(), messageBuffer, messageRemoteAddress);
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
	public void testIfMessageSentDownstreamIsReceivedByOneHandler() throws Exception {

		pipeline.addFirst("first", handler1);

		assertThatMessageSentDownstreamIsReceivedByDownstreamListener(handler1);
	}

	@Test
	public void testIfMessageSentDownstreamIsReceivedByTwoHandlersInCorrectOrder() throws Exception {

		pipeline.addFirst("second", handler2);
		pipeline.addFirst("first", handler1);

		assertThatMessageSentDownstreamIsReceivedByDownstreamListener(handler2, handler1);
	}

	@Test
	public void testIfMessageSentDownstreamIsReceivedByThreeHandlersInCorrectOrderAsPipeline() throws Exception {

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

		setUpPipelineAsAttachedOrHandler();

		final UpstreamMessageEvent upstreamMessageEvent = createUpstreamMessageEvent();
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

		setUpPipelineAsAttachedOrHandler();

		final DownstreamMessageEvent downstreamMessageEvent = createDownstreamMessageEvent();
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

		setUpPipelineAsAttachedOrHandler();
		assertThatUpstreamEventIsPassedThroughHandlerMock(handlerMock);
	}

	@Test
	public void testIfUpstreamEventsArePassedThroughTwoHandlers() throws Exception {

		final SimpleChannelHandler handlerMock = mock(SimpleChannelHandler.class);
		pipeline.addLast("first", handlerMock);
		pipeline.addLast("second", handler1);
		setUpPipelineAsAttachedOrHandler();
		assertThatUpstreamEventIsPassedThroughHandlerMock(handlerMock);
	}

	@Test
	public void testIfDownstreamEventsArePassedThroughOneHandler() throws Exception {

		final SimpleChannelHandler handlerMock = mock(SimpleChannelHandler.class);
		pipeline.addLast("first", handlerMock);
		setUpPipelineAsAttachedOrHandler();
		assertThatDownstreamEventIsPassedThroughHandlerMock(handlerMock);
	}

	@Test
	public void testIfDownstreamEventsArePassedThroughTwoHandlers() throws Exception {

		final SimpleChannelHandler handlerMock = mock(SimpleChannelHandler.class);
		pipeline.addLast("second", handlerMock);
		pipeline.addLast("first", handler1);
		setUpPipelineAsAttachedOrHandler();
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

		pipeline.attach(outerChannelMock, outerChannelSinkMock);

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

		pipeline.attach(outerChannelMock, outerChannelSinkMock);

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
			pipeline.attach(outerChannelMock, outerChannelSinkMock);
			fail();
		} catch (IllegalStateException e) {
		}

		pipeline.beforeRemove(mockContext);
		pipeline.afterRemove(mockContext);

		pipeline.attach(outerChannelMock, outerChannelSinkMock);

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

	@Test(expected = IllegalStateException.class)
	public void testIfHandleUpstreamThrowsIllegalStateExceptionIfNotAttachedOrAddedAsHandler() throws Exception {
		pipeline.handleUpstream(mock(ChannelHandlerContext.class), mock(UpstreamMessageEvent.class));
	}

	@Test(expected = IllegalStateException.class)
	public void testIfHandleDownstreamThrowsIllegalStateExceptionIfNotAttachedOrAddedAsHandler() throws Exception {
		pipeline.handleDownstream(mock(ChannelHandlerContext.class), mock(DownstreamMessageEvent.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIfHandleDownstreamThrowsIllegalArgumentExceptionForUpstreamMessageEvent() throws Exception {
		setUpPipelineAsAttachedOrHandler();
		pipeline.handleDownstream(mock(ChannelHandlerContext.class), mock(UpstreamMessageEvent.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIfHandleUpstreamThrowsIllegalArgumentExceptionForDownstreamMessageEvent() throws Exception {
		setUpPipelineAsAttachedOrHandler();
		pipeline.handleUpstream(mock(ChannelHandlerContext.class), mock(DownstreamMessageEvent.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIfHandleDownstreamThrowsIllegalArgumentExceptionForUpstreamChannelStateEvent() throws Exception {
		setUpPipelineAsAttachedOrHandler();
		pipeline.handleDownstream(mock(ChannelHandlerContext.class), mock(UpstreamChannelStateEvent.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIfHandleUpstreamThrowsIllegalArgumentExceptionForDownstreamChannelStateEvent() throws Exception {
		setUpPipelineAsAttachedOrHandler();
		pipeline.handleUpstream(mock(ChannelHandlerContext.class), mock(DownstreamChannelStateEvent.class));
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

		setUpPipelineAsAttachedOrHandler();

		final UpstreamMessageEvent upstreamMessageEvent = createUpstreamMessageEvent();

		if (asPipeline) {
			pipeline.sendUpstream(upstreamMessageEvent);
		} else {
			pipeline.handleUpstream(outerChannelHandlerContextMock, upstreamMessageEvent);
		}

		InOrder inOrder = inOrder(mocks);

		for (PassThroughChannelHandler mock : mocks) {

			ArgumentCaptor<ChannelEvent> channelEventCaptor = ArgumentCaptor.forClass(ChannelEvent.class);

			inOrder.verify(mock).handleUpstream(Matchers.<ChannelHandlerContext>any(), channelEventCaptor.capture());

			assertCapturedMessageEventEqualsSent(messageBytes, messageRemoteAddress, channelEventCaptor);
		}

		if (!asPipeline) {
			verify(outerChannelHandlerContextMock).sendUpstream(eq(upstreamMessageEvent));
		}
	}

	private void setUpPipelineAsAttachedOrHandler() throws Exception {
		if (asPipeline) {
			pipeline.attach(outerChannelMock, outerChannelSinkMock);
		} else {
			when(outerChannelHandlerContextMock.getChannel()).thenReturn(outerChannelMock);
			when(outerChannelMock.getPipeline()).thenReturn(outerChannelPipelineMock);
			when(outerChannelPipelineMock.getSink()).thenReturn(outerChannelSinkMock);
			pipeline.beforeAdd(outerChannelHandlerContextMock);
			pipeline.afterAdd(outerChannelHandlerContextMock);
		}
	}

	private void assertThatMessageSentDownstreamIsReceivedByDownstreamListener(PassThroughChannelHandler... mocks)
			throws Exception {

		setUpPipelineAsAttachedOrHandler();

		final DownstreamMessageEvent downstreamMessageEvent = createDownstreamMessageEvent();

		if (asPipeline) {
			pipeline.sendDownstream(downstreamMessageEvent);
		} else {
			pipeline.handleDownstream(outerChannelHandlerContextMock, downstreamMessageEvent);
		}


		InOrder inOrder = inOrder(mocks);

		for (PassThroughChannelHandler mock : mocks) {

			ArgumentCaptor<ChannelEvent> channelEventCaptor = ArgumentCaptor.forClass(ChannelEvent.class);

			inOrder.verify(mock).handleDownstream(Matchers.<ChannelHandlerContext>any(), channelEventCaptor.capture());

			assertCapturedMessageEventEqualsSent(messageBytes, messageRemoteAddress, channelEventCaptor);
		}

		if (!asPipeline) {
			verify(outerChannelHandlerContextMock).sendDownstream(eq(downstreamMessageEvent));
		}
	}

	private void assertCapturedMessageEventEqualsSent(final byte[] expectedMessageBytes,
													  final InetSocketAddress expectedRemoteAddress,
													  final ArgumentCaptor<ChannelEvent> channelEventCaptor) {

		final ChannelEvent capturedChannelEvent = channelEventCaptor.getValue();
		assertNotNull(capturedChannelEvent);

		final Object actualMessageObject;
		final SocketAddress actualRemoteAddress;

		if (capturedChannelEvent instanceof UpstreamMessageEvent) {

			actualMessageObject = ((UpstreamMessageEvent) capturedChannelEvent).getMessage();
			actualRemoteAddress = ((UpstreamMessageEvent) capturedChannelEvent).getRemoteAddress();

		} else if (capturedChannelEvent instanceof DownstreamMessageEvent) {

			actualMessageObject = ((DownstreamMessageEvent) capturedChannelEvent).getMessage();
			actualRemoteAddress = ((DownstreamMessageEvent) capturedChannelEvent).getRemoteAddress();

		} else {
			throw new AssertionError();
		}

		assertCapturedMessageEqualsMessageSent(expectedMessageBytes, actualMessageObject);
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
