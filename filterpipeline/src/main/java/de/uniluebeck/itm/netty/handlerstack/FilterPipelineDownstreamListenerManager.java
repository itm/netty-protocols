package de.uniluebeck.itm.netty.handlerstack;

import de.uniluebeck.itm.tr.util.ListenerManagerImpl;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FilterPipelineDownstreamListenerManager extends ListenerManagerImpl<FilterPipelineDownstreamListener>
		implements FilterPipelineDownstreamListener {

	private static final Logger log = LoggerFactory.getLogger(FilterPipelineDownstreamListenerManager.class);

	public void downstreamExceptionCaught(final Throwable e) {

		for (FilterPipelineDownstreamListener listener : listeners) {
			listener.downstreamExceptionCaught(e);
		}
	}

	public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent e) {
		for (FilterPipelineDownstreamListener listener : listeners) {
			try {
				listener.handleDownstream(ctx, e);
			} catch (Exception e1) {
				log.error(
						"The FilterPipelineDownstreamListener {} throw the following exception on calling handleDownstream(): {}",
						listener, e1
				);
			}
		}
	}

}