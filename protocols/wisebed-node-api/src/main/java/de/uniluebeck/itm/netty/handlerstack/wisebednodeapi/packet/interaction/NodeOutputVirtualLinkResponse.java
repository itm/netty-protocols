package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Request;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */
public class NodeOutputVirtualLinkResponse extends Response{
	public NodeOutputVirtualLinkResponse(final NodeOutputVirtualLinkRequest request) {
		super(request);
	}

	public byte getRssi(){
		return ((NodeOutputVirtualLinkRequest)request).getRssi();
	}

	public byte getLqi(){
		return ((NodeOutputVirtualLinkRequest)request).getLqi();
	}

	public byte getLen(){
		return ((NodeOutputVirtualLinkRequest)request).getLen();
	}

	public long getDest(){
		return ((NodeOutputVirtualLinkRequest)request).getDest();
	}

	public long getSource(){
		return ((NodeOutputVirtualLinkRequest)request).getSource();
	}

}
