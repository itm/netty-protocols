package de.uniluebeck.itm.netty.handlerstack.isenseotap.otap.program;

import java.util.HashSet;
import java.util.Set;

public class ISenseOtapProgramResult {
    private final Set<Integer> devicesToBeProgrammed;
    private Set<Integer> failedDevices = new HashSet<Integer>();
    private Set<Integer> doneDevices = new HashSet<Integer>();;

    public ISenseOtapProgramResult(Set<Integer> devicesToBeProgrammed) {
        this.devicesToBeProgrammed = devicesToBeProgrammed;
    }

    public boolean isDone() {
        HashSet<Integer> doneAndFailed = new HashSet<Integer>(doneDevices);
        doneAndFailed.addAll(failedDevices);

        return devicesToBeProgrammed.containsAll(doneAndFailed);
    }

    void addFailedDevice(Integer deviceId) {
        failedDevices.add(deviceId);
    }

    void addDoneDevice(Integer deviceId) {
        doneDevices.add(deviceId);
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
