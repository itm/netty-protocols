package de.uniluebeck.itm.netty.handlerstack.isenseotap.otap.program;

import java.util.Set;

public class ISenseOtapProgramRequest {

    private final Set<Integer> devicesToProgram;
    private final byte[] otapProgram;

    public ISenseOtapProgramRequest(Set<Integer> otapDevices, byte[] otapProgram) {
        this.devicesToProgram = otapDevices;
        this.otapProgram = otapProgram;
    }

    /**
     * @return the devicesToProgram
     */
    public Set<Integer> getDevicesToProgram() {
        return devicesToProgram;
    }

    /**
     * @return the otapProgram
     */
    public byte[] getOtapProgram() {
        return otapProgram;
    }

}
