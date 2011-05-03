package de.uniluebeck.itm.netty.handlerstack.isenseotap;

import java.util.HashSet;
import java.util.Set;

public class ISenseOtapProgramState {
    private Set<Integer> devicesToBeProgrammed;
    private Set<Integer> failedDevices;
    private Set<Integer> doneDevices;

    public ISenseOtapProgramState(Set<Integer> devicesToBeProgrammed, Set<Integer> failedDevices,
            Set<Integer> doneDevices) {
        super();
        this.devicesToBeProgrammed = devicesToBeProgrammed;
        this.failedDevices = failedDevices;
        this.doneDevices = doneDevices;
    }

    public boolean isDone() {
        HashSet<Integer> doneAndFailed = new HashSet<Integer>(doneDevices);
        doneAndFailed.addAll(failedDevices);

        return devicesToBeProgrammed.containsAll(doneAndFailed);
    }

    /**
     * @return the devicesToBeProgrammed
     */
    public Set<Integer> getDevicesToBeProgrammed() {
        return devicesToBeProgrammed;
    }

    /**
     * @return the failedDevices
     */
    public Set<Integer> getFailedDevices() {
        return failedDevices;
    }

    /**
     * @return the doneDevices
     */
    public Set<Integer> getDoneDevices() {
        return doneDevices;
    }

}
