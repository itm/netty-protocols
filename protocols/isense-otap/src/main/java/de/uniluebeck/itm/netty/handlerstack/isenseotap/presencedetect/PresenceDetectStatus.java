package de.uniluebeck.itm.netty.handlerstack.isenseotap.presencedetect;

import java.util.Set;

public class PresenceDetectStatus {
    Set<Integer> detectedDevices;

    public PresenceDetectStatus(Set<Integer> detectedDevices) {
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
