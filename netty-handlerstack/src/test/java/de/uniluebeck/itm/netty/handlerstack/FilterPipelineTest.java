package de.uniluebeck.itm.netty.handlerstack;

import com.google.common.collect.Lists;
import de.uniluebeck.itm.netty.handlerstack.discard.DiscardMessagesHandler;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.LifeCycleAwareChannelHandler;
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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FilterPipelineTest {

	private FilterPipeline filterPipeline;

	@Mock
	private FilterPipeline.UpstreamOutputListener upstreamOutputListener;

	@Mock
	private FilterPipeline.DownstreamOutputListener downstreamOutputListener;

	private ArgumentCaptor<ChannelBuffer> messageCaptor;

	private ArgumentCaptor<SocketAddress> sourceAddressCaptor;

	private InetSocketAddress sourceAddress;

	private byte[] bytes;

	@Before
	public void setUp() throws Exception {
		filterPipeline = new FilterPipelineImpl();
		filterPipeline.addListener(upstreamOutputListener);
		filterPipeline.addListener(downstreamOutputListener);

		messageCaptor = ArgumentCaptor.forClass(ChannelBuffer.class);
		sourceAddressCaptor = ArgumentCaptor.forClass(SocketAddress.class);

		sourceAddress = new InetSocketAddress("localhost", 1234);
		bytes = "Hello, World".getBytes();
	}

	@Test
	public void testSendUpstream() throws Exception {

		filterPipeline.sendUpstream(ChannelBuffers.wrappedBuffer(bytes), sourceAddress);

		verify(upstreamOutputListener).receiveUpstreamOutput(messageCaptor.capture(), sourceAddressCaptor.capture());

		final byte[] bytesReceived = getToByteArray(messageCaptor.getValue());

		assertArrayEquals(bytes, bytesReceived);
		assertEquals(sourceAddress, sourceAddressCaptor.getValue());
	}

	@Test
	public void testSendDownstream() throws Exception {

		filterPipeline.sendDownstream(ChannelBuffers.wrappedBuffer(bytes), sourceAddress);

		verify(downstreamOutputListener)
				.receiveDownstreamOutput(messageCaptor.capture(), sourceAddressCaptor.capture());

		final byte[] bytesReceived = getToByteArray(messageCaptor.getValue());

		assertArrayEquals(bytes, bytesReceived);
		assertEquals(sourceAddress, sourceAddressCaptor.getValue());
	}

	@Test
	public void testSetChannelPipeline() throws Exception {

		final List<Tuple<String, ChannelHandler>> discardPipeline = newArrayList(
				new Tuple<String, ChannelHandler>("discard", new DiscardMessagesHandler(true, true))
		);

		filterPipeline.setChannelPipeline(discardPipeline);

		filterPipeline.sendUpstream(ChannelBuffers.wrappedBuffer(bytes), sourceAddress);

		verify(upstreamOutputListener, never()).receiveUpstreamOutput(
				Matchers.<ChannelBuffer>any(),
				Matchers.<SocketAddress>any()
		);

		filterPipeline.sendDownstream(ChannelBuffers.wrappedBuffer(bytes), sourceAddress);

		verify(downstreamOutputListener, never()).receiveDownstreamOutput(
				Matchers.<ChannelBuffer>any(),
				Matchers.<SocketAddress>any()
		);
	}

	private static interface LifeCycleAwareDownstreamChannelHandler extends LifeCycleAwareChannelHandler,
			ChannelDownstreamHandler {
	}

	@Test
	public void testLifeCycleAwareHandler() throws Exception {

		final LifeCycleAwareDownstreamChannelHandler mockHandler = mock(LifeCycleAwareDownstreamChannelHandler.class);
		final InOrder inOrder = inOrder(mockHandler);

		verify(mockHandler, never()).beforeAdd(Matchers.<ChannelHandlerContext>any());
		verify(mockHandler, never()).afterAdd(Matchers.<ChannelHandlerContext>any());
		verify(mockHandler, never()).beforeRemove(Matchers.<ChannelHandlerContext>any());
		verify(mockHandler, never()).afterRemove(Matchers.<ChannelHandlerContext>any());

		filterPipeline.setChannelPipeline(newArrayList(
				new Tuple<String, ChannelHandler>("mock", mockHandler)
		)
		);

		verify(mockHandler, never()).beforeRemove(Matchers.<ChannelHandlerContext>any());
		verify(mockHandler, never()).afterRemove(Matchers.<ChannelHandlerContext>any());

		inOrder.verify(mockHandler).beforeAdd(Matchers.<ChannelHandlerContext>any());
		inOrder.verify(mockHandler).afterAdd(Matchers.<ChannelHandlerContext>any());

		filterPipeline.setChannelPipeline(Lists.<Tuple<String, ChannelHandler>>newArrayList());

		inOrder.verify(mockHandler).beforeRemove(Matchers.<ChannelHandlerContext>any());
		inOrder.verify(mockHandler).afterRemove(Matchers.<ChannelHandlerContext>any());
	}
}
