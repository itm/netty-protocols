/**
 * Copyright (c) 2010, Daniel Bimschas and Dennis Pfisterer, Institute of Telematics, University of Luebeck
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uniluebeck.itm.nettyrxtx.rup;

import java.util.Arrays;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.google.common.base.Preconditions;

import de.uniluebeck.itm.tr.util.StringUtils;


class RUPFragmentImpl implements RUPFragment {

	private static final int POS_CMD_TYPE = 0;

	private static final int POS_SEQUENCE_NUMBER = 1;

	private static final int POS_DESTINATION = 2;

	private static final int POS_SOURCE = 10;

	private static final int POS_PAYLOAD = 19;

	private final ChannelBuffer packet;

	public RUPFragmentImpl(final ChannelBuffer channelBuffer) {

		Preconditions.checkNotNull(channelBuffer);
		Preconditions.checkArgument(
				channelBuffer.readableBytes() >= 19,
				"Unable to parse %s from %s as bytes is too short.",
				RUPFragment.class.getSimpleName(), channelBuffer
		);

		packet = channelBuffer;
	}

	public RUPFragmentImpl(final byte cmdType, final byte sequenceNumber, final long destination,
						   final long source,
						   final byte[] payload) {

		this(cmdType, sequenceNumber, destination, source, ChannelBuffers.wrappedBuffer(payload));
	}

	public RUPFragmentImpl(byte cmdType, byte sequenceNumber, long destination, long source, ChannelBuffer payload) {

		Preconditions.checkNotNull(cmdType, "cmdType is null");
		Preconditions.checkNotNull(sequenceNumber, "sequenceNumber is null");
		Preconditions.checkNotNull(destination, "destination is null");
		Preconditions.checkNotNull(source, "source is null");
		// payload is allowed to be null in case somebody wants to send empty packets

		ChannelBuffer headers = ChannelBuffers.buffer(1 + 1 + 8 + 8 + 1);
		headers.writeByte(cmdType);
		headers.writeByte(sequenceNumber);
		headers.writeLong(destination);
		headers.writeLong(source);
		headers.writeByte((byte) (payload.readableBytes() & 0xFF));

		packet = ChannelBuffers.wrappedBuffer(headers, payload);

	}

	public byte getCmdType() {
		return packet.getByte(POS_CMD_TYPE);
	}

	public byte getSequenceNumber() {
		return packet.getByte(POS_SEQUENCE_NUMBER);
	}

	public long getDestination() {
		return packet.getLong(POS_DESTINATION);
	}

	public long getSource() {
		return packet.getLong(POS_SOURCE);
	}

	public ChannelBuffer getPayload() {
		return packet.slice(POS_PAYLOAD, packet.readableBytes() - POS_PAYLOAD);
	}

	public ChannelBuffer getChannelBuffer() {
		return packet;
	}

	@Override
	public boolean equals(final Object o) {

		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final RUPFragmentImpl that = (RUPFragmentImpl) o;
		return Arrays.equals(packet.array(), that.packet.array());

	}

	@Override
	public int hashCode() {
		return packet.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RUPFragment[cmdType=");
		builder.append(getCmdType());
		builder.append(",sequenceNumber=");
		builder.append(getSequenceNumber());
		builder.append(",destination=");
		builder.append(getDestination());
		builder.append(",source=");
		builder.append(getSource());
		builder.append(",payload=");
		builder.append(StringUtils.toHexString(getPayload().array()));
		return builder.toString();
	}
}
