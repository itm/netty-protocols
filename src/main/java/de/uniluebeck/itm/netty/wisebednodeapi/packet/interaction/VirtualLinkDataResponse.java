package de.uniluebeck.itm.netty.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 10:08
 * To change this template use File | Settings | File Templates.
 */
public class VirtualLinkDataResponse extends Response {
	public VirtualLinkDataResponse(final VirtualLinkDataRequest request) {
		super(request);
	}

	public byte getRssi(){
		return ((VirtualLinkDataRequest)request).getRssi();
	}

	public byte getLqi(){
		return ((VirtualLinkDataRequest)request).getLqi();
	}

	public byte getLen(){
		return ((VirtualLinkDataRequest)request).getLen();
	}

	public long getDest(){
		return ((VirtualLinkDataRequest)request).getDest();
	}

	public long getSource(){
		return ((VirtualLinkDataRequest)request).getSource();
	}

}
