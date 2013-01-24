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
package de.uniluebeck.itm.nettyprotocols.isense.ishellinterpreter;

import java.util.HashMap;
import java.util.Map;

public enum IShellInterpreterPacketTypes {
	COMMAND_SET_CHANNEL((byte) (0xFF & 2)),
	COMMAND_SEND_ID_TO_ISHELL((byte) (0xFF & 3)),
	COMMAND_ISHELL_TO_ROUTING((byte) (0xFF & 4)),
	COMMAND_SET_STD_KEY((byte) (0xFF & 5)),
	PACKET_TYPE_ISENSE_ID((byte) (0xFF & 113)),
	ROUTING_TREE_ROUTING((byte) (0xFF & 7));

	private static final Map<Byte, IShellInterpreterPacketTypes> typesMap =
			new HashMap<Byte, IShellInterpreterPacketTypes>();

	static {
		for (IShellInterpreterPacketTypes packetType : IShellInterpreterPacketTypes.values()) {
			typesMap.put(packetType.value, packetType);
		}
	}

	private final byte value;

	IShellInterpreterPacketTypes(byte value) {
		this.value = value;
	}

	/**
	 * Returns the enum constant with value {@code value} or null if none of the enum values matches {@code value}.
	 *
	 * @param value
	 * 		the packets type
	 *
	 * @return an IShellInterpreterPacketTypes enum constant or {@code null} if unknown
	 */
	public static IShellInterpreterPacketTypes fromValue(byte value) {
		return typesMap.get(value);
	}

	public byte getValue() {
		return value;
	}

}
