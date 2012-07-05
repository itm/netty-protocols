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
package de.uniluebeck.itm.netty.handlerstack.isense;


import java.util.HashMap;
import java.util.Map;

public enum ISensePacketType {

	//reserved inbound types
	CODE((byte) (0xFF & 0)), // deprecated, might be removed in the future

	RESET((byte) (0xFF & 1)),

	SERAERIAL((byte) (0xFF & 2)),

	TIME((byte) (0xFF & 3)),

	CAMERA_APPLICATION((byte) (0xFF & 4)),

	AMR_APPLICATION((byte) (0xFF & 5)),

	ACC_APPLICATION((byte) (0xFF & 6)),

	VIRTUAL_RADIO_IN((byte) (0xFF & 7)),

	IN_RESERVED_2((byte) (0xFF & 8)),

	IN_RESERVED_3((byte) (0xFF & 9)),

	//inbound types for users
	CUSTOM_IN_1((byte) (0xFF & 10)),

	CUSTOM_IN_2((byte) (0xFF & 11)),

	CUSTOM_IN_3((byte) (0xFF & 12)),

	NETWORK_IN((byte) (0xFF & 13)),

	//output types
	//DEBUG(100),
	//INFO(101),
	//WARNING(102),
	//ERROR(103),
	LOG((byte) (0xFF & 104)),

	PLOT((byte) (0xFF & 105)),

	CUSTOM_OUT((byte) (0xFF & 106)),

	JPEG((byte) (0xFF & 108)),

	TIMEREQUEST((byte) (0xFF & 109)),

	AUDIO((byte) (0xFF & 110)),

	SPYGLASS((byte) (0xFF & 111)),

	FLOAT_BUFFER((byte) (0xFF & 112)),

	SQL((byte) (0xFF & 113)),

	VIRTUAL_RADIO_OUT((byte) (0xFF & 114)),

	NETWORK_OUT((byte) (0xFF & 115));

	private static final Map<Byte, ISensePacketType> typesMap = new HashMap<Byte, ISensePacketType>();

	static {
		for (ISensePacketType packetType : ISensePacketType.values()) {
			typesMap.put(packetType.value, packetType);
		}
	}

	private final byte value;

	ISensePacketType(byte value) {
		this.value = value;
	}

	/**
	 * Returns the enum constant with value {@code value} or null if none of the enum values matches {@code value}.
	 *
	 * @param value the packets type
	 * @return an ISensePacketType enum constant or {@code null} if unknown
	 */
	public static ISensePacketType fromValue(byte value) {
		return typesMap.get(value);
	}

	public byte getValue() {
		return value;
	}
}
