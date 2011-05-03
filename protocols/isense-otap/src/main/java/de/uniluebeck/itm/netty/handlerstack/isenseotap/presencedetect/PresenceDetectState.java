package de.uniluebeck.itm.netty.handlerstack.isenseotap.presencedetect;

import java.util.Set;

public class PresenceDetectState {
    Set<Integer> detectedDevices;

    public PresenceDetectState(Set<Integer> detectedDevices) {
        super();
        this.detectedDevices = detectedDevices;
    }
    
    /**
     * @return the detectedDevices
     */
    public Set<Integer> getDetectedDevices() {
        return detectedDevices;
    }
    
}
