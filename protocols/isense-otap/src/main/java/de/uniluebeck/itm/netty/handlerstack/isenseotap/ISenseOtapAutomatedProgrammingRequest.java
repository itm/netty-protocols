package de.uniluebeck.itm.netty.handlerstack.isenseotap;

import java.util.Set;

import de.uniluebeck.itm.netty.handlerstack.isenseotap.otap.program.ISenseOtapProgramRequest;
import de.uniluebeck.itm.netty.handlerstack.util.DurationPlusUnit;

public class ISenseOtapAutomatedProgrammingRequest extends ISenseOtapProgramRequest {

    private final DurationPlusUnit presenceDetectTimeout;
    private final DurationPlusUnit initTimeout;
    private final DurationPlusUnit programmingTimeout;
    
    private final short maxRerequests;
    private short timeoutMultiplier;

    public ISenseOtapAutomatedProgrammingRequest(Set<Integer> otapDevices, byte[] otapProgram,
            DurationPlusUnit presenceDetectTimeout, DurationPlusUnit initTimeout, DurationPlusUnit programmingTimeout,
            final short maxRerequests, final short timeoutMultiplier) {

        super(otapDevices, otapProgram);

        this.presenceDetectTimeout = presenceDetectTimeout;
        this.initTimeout = initTimeout;
        this.programmingTimeout = programmingTimeout;
        this.maxRerequests = maxRerequests;
        this.timeoutMultiplier = timeoutMultiplier;
    }

    /**
     * @return the presenceDetectTimeout
     */
    public DurationPlusUnit getPresenceDetectTimeout() {
        return presenceDetectTimeout;
    }

    /**
     * @return the initTimeout
     */
    public DurationPlusUnit getOtapInitTimeout() {
        return initTimeout;
    }

    /**
     * @return the programmingTimeout
     */
    public DurationPlusUnit getProgrammingTimeout() {
        return programmingTimeout;
    }

    /**
     * @return the maxRerequests
     */
    public short getMaxRerequests() {
        return maxRerequests;
    }

    /**
     * @return the timeoutMultiplier
     */
    public short getTimeoutMultiplier() {
        return timeoutMultiplier;
    }

}
