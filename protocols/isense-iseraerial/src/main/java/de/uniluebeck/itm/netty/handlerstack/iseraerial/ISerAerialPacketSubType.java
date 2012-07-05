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
package de.uniluebeck.itm.netty.handlerstack.iseraerial;

import java.util.HashMap;
import java.util.Map;

public enum ISerAerialPacketSubType {
    PACKET((byte) (0xFF & 0x00)), CONFIRM((byte) (0xFF & 0x01));

    private static final Map<Byte, ISerAerialPacketSubType> typesMap = new HashMap<Byte, ISerAerialPacketSubType>();

    static {
        for (ISerAerialPacketSubType packetType : ISerAerialPacketSubType.values()) {
            typesMap.put(packetType.value, packetType);
        }
    }

    private final byte value;

    ISerAerialPacketSubType(byte value) {
        this.value = value;
    }

    /**
     * Returns the enum constant with value {@code value} or null if none of the enum values matches {@code value}.
     * 
     * @param value
     *            the packet's type
     * @return an ISerAerialPacketSubType enum constant or {@code null} if unknown
     */
    public static ISerAerialPacketSubType fromValue(byte value) {
        return typesMap.get(value);
    }

    public byte getValue() {
        return value;
    }
}
