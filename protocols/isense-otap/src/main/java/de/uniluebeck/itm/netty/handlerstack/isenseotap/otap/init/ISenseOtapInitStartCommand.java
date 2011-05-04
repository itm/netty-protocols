package de.uniluebeck.itm.netty.handlerstack.isenseotap.otap.init;

import java.util.Set;

import de.uniluebeck.itm.netty.handlerstack.util.DurationPlusUnit;

public class ISenseOtapInitStartCommand {

    private final Set<Integer> devicesToInitialize;
    private final DurationPlusUnit otapInitTimeout;
    private final short chunkCount;
    private final short maxRerequests;
    private short timeoutMultiplier;

    public ISenseOtapInitStartCommand(final Set<Integer> devicesToInitialize, final DurationPlusUnit otapInitTimeout,
            final short chunkCount, final short maxRerequests, final short timeoutMultiplier) {

        this.chunkCount = chunkCount;
        this.devicesToInitialize = devicesToInitialize;
        this.otapInitTimeout = otapInitTimeout;
        this.maxRerequests = maxRerequests;
        this.timeoutMultiplier = timeoutMultiplier;
    }

    /**
     * @return the devicesToInitialize
     */
    public Set<Integer> getDevicesToInitialize() {
        return devicesToInitialize;
    }

    /**
     * @return the otapInitTimeout
     */
    public DurationPlusUnit getOtapTimeout() {
        return otapInitTimeout;
    }

    /**
     * @return the chunkCount
     */
    public short getChunkCount() {
        return chunkCount;
    }

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
