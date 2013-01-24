package de.uniluebeck.itm.netty.wisebednodeapi.packet;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 09.06.11
 * Time: 13:25
 * To change this template use File | Settings | File Templates.
 */
public abstract class Response {

	final protected Request request;

	public Response(final Request request) {
		this.request = request;
	}

	public Request getRequest() {
		return this.request;
	}

}
