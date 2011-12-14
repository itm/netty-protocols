package de.uniluebeck.itm.netty.handlerstack;

import de.uniluebeck.itm.tr.util.ListenerManagerImpl;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FilterPipelineUpstreamListenerManager extends ListenerManagerImpl<FilterPipelineUpstreamListener>
		implements FilterPipelineUpstreamListener {

	private static final Logger log = LoggerFactory.getLogger(FilterPipelineUpstreamListenerManager.class);

	@Override
	public void upstreamExceptionCaught(final Throwable e) {

		for (FilterPipelineUpstreamListener listener : listeners) {
			listener.upstreamExceptionCaught(e);
		}
	}

	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) {

		for (FilterPipelineUpstreamListener listener : listeners) {

			try {
				listener.handleUpstream(ctx, e);
			} catch (Exception e1) {
				log.error(
						"The FilterPipelineUpstreamListener {} throw the following exception on calling handleUpstream(): {}",
						listener, e1
				);
			}
		}
	}

}