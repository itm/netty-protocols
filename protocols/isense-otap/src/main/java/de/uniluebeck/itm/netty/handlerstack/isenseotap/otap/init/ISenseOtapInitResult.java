package de.uniluebeck.itm.netty.handlerstack.isenseotap.otap.init;

import java.util.Set;

public class ISenseOtapInitResult {

    private final ISenseOtapInitStartCommand otapInitStartCommand;
    private final Set<Integer> initializedDevices;

    public ISenseOtapInitResult(ISenseOtapInitStartCommand otapInitStartCommand, Set<Integer> initializedDevices) {
        this.otapInitStartCommand = otapInitStartCommand;
        this.initializedDevices = initializedDevices;
    }

    /**
     * @return the otapInitStartCommand
     */
    public ISenseOtapInitStartCommand getOtapInitStartCommand() {
        return otapInitStartCommand;
    }

    /**
     * @return the initializedDevices
     */
    public Set<Integer> getInitializedDevices() {
        return initializedDevices;
    }

}
