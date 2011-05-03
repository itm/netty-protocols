package de.uniluebeck.itm.netty.handlerstack.util;

import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.tr.util.TimeDiff;

public class ConfirmPendingOrTimeoutHelper {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ConfirmPendingOrTimeoutHelper.class);
    
    TimeDiff timeout;

    boolean confirmPending = false;

    public ConfirmPendingOrTimeoutHelper(int timeout, TimeUnit timeoutTimeUnit) {
        this.timeout = new TimeDiff(timeoutTimeUnit.toMillis(timeout));
    }

    public synchronized boolean isNextTransmitOk() {
        boolean isTimeout = timeout.isTimeout();
        log.trace("confirmPending: {}, timeout {}", confirmPending, isTimeout);
        return confirmPending == false || isTimeout;
    }

    public synchronized boolean isConfirmPending() {
        return !isNextTransmitOk();
    }

    public synchronized void setConfirmReceived() {
        confirmPending = false;
    }

    public synchronized void setConfirmPendingAndResetTimeout() {
        confirmPending = true;
        timeout.touch();
    }

}
