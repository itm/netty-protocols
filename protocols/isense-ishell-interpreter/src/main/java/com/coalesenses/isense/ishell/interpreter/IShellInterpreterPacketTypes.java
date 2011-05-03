package com.coalesenses.isense.ishell.interpreter;

import java.util.HashMap;
import java.util.Map;

public enum IShellInterpreterPacketTypes {
    COMMAND_SET_CHANNEL((byte) (0xFF & 2)),
    COMMAND_SEND_ID_TO_ISHELL((byte) (0xFF & 3)),
    COMMAND_ISHELL_TO_ROUTING((byte) (0xFF & 4)),
    COMMAND_SET_STD_KEY((byte) (0xFF & 5)), 
    PACKET_TYPE_ISENSE_ID((byte) (0xFF & 113)),
    ROUTING_TREE_ROUTING((byte) (0xFF & 7));
    
    private static final Map<Byte, IShellInterpreterPacketTypes> typesMap = new HashMap<Byte, IShellInterpreterPacketTypes>();

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
     * @param value the packets type
     * @return an IShellInterpreterPacketTypes enum constant or {@code null} if unknown
     */
    public static IShellInterpreterPacketTypes fromValue(byte value) {
            return typesMap.get(value);
    }

    public byte getValue() {
            return value;
    }
    
}
