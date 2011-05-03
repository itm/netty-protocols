package de.uniluebeck.itm.netty.handlerstack.isenseotap;

import java.util.Set;

public class ISenseOtapProgramRequest {

    private final Set<Integer> otapDevices;
    private final byte[] otapProgram;

    public ISenseOtapProgramRequest(Set<Integer> otapDevices, byte[] otapProgram) {
        this.otapDevices = otapDevices;
        this.otapProgram = otapProgram;
    }

    /**
     * @return the otapDevices
     */
    public Set<Integer> getOtapDevices() {
        return otapDevices;
    }

    /**
     * @return the otapProgram
     */
    public byte[] getOtapProgram() {
        return otapProgram;
    }

}
