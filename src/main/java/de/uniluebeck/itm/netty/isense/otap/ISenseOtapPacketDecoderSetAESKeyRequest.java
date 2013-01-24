package de.uniluebeck.itm.netty.isense.otap;

import com.coalesenses.tools.iSenseAes128BitKey;

public class ISenseOtapPacketDecoderSetAESKeyRequest {
    private final iSenseAes128BitKey key;

    public ISenseOtapPacketDecoderSetAESKeyRequest(iSenseAes128BitKey key) {
        super();
        this.key = key;
    }

    /**
     * @return the key
     */
    public iSenseAes128BitKey getKey() {
        return key;
    }
}
