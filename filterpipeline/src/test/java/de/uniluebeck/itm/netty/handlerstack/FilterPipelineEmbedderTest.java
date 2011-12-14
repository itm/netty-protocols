package de.uniluebeck.itm.netty.handlerstack;

import org.mockito.Mock;

public class FilterPipelineEmbedderTest {

	private FilterPipelineEmbedder embedder;

	@Mock
	private FilterPipelineUpstreamListener upstreamOutputListenerMock;

	@Mock
	private FilterPipelineDownstreamListener downstreamOutputListenerMock;
}
