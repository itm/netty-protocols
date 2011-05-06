package de.uniluebeck.itm.netty.handlerstack.isenseotap;

import com.coalesenses.tools.iSenseAes128BitKey;

public class ISenseOtapPacketEncoderSetAESKeyRequest {
    private final iSenseAes128BitKey key;

    public ISenseOtapPacketEncoderSetAESKeyRequest(iSenseAes128BitKey key) {
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
