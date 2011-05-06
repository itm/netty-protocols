/**
 * Copyright (c) 2010, Daniel Bimschas and Dennis Pfisterer, Institute of Telematics, University of Luebeck
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uniluebeck.itm.netty.handlerstack.isenseotap;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.coalesenses.tools.iSenseAes128BitKey;

import de.uniluebeck.itm.netty.handlerstack.isenseotap.program.ISenseOtapProgramRequest;
import de.uniluebeck.itm.netty.handlerstack.util.DurationPlusUnit;

public class ISenseOtapAutomatedProgrammingRequest extends ISenseOtapProgramRequest {

    private DurationPlusUnit presenceDetectTimeout = new DurationPlusUnit(30, TimeUnit.SECONDS);
    private DurationPlusUnit otapInitTimeout = new DurationPlusUnit(30, TimeUnit.SECONDS);
    private DurationPlusUnit programmingTimeout = new DurationPlusUnit(15, TimeUnit.MINUTES);

    private short maxRerequests = 50;
    private short timeoutMultiplier = 15;
    private iSenseAes128BitKey aesKey = null;

    public ISenseOtapAutomatedProgrammingRequest(Set<Integer> otapDevices, byte[] otapProgram) {
        super(otapDevices, otapProgram);
    }

    /**
     * @return the presenceDetectTimeout
     */
    public DurationPlusUnit getPresenceDetectTimeout() {
        return presenceDetectTimeout;
    }

    /**
     * @param presenceDetectTimeout
     *            the presenceDetectTimeout to set
     */
    public void setPresenceDetectTimeout(DurationPlusUnit presenceDetectTimeout) {
        this.presenceDetectTimeout = presenceDetectTimeout;
    }

    /**
     * @return the otapInitTimeout
     */
    public DurationPlusUnit getOtapInitTimeout() {
        return otapInitTimeout;
    }

    /**
     * @param otapInitTimeout
     *            the otapInitTimeout to set
     */
    public void setOtapInitTimeout(DurationPlusUnit otapInitTimeout) {
        this.otapInitTimeout = otapInitTimeout;
    }

    /**
     * @return the programmingTimeout
     */
    public DurationPlusUnit getProgrammingTimeout() {
        return programmingTimeout;
    }

    /**
     * @param programmingTimeout
     *            the programmingTimeout to set
     */
    public void setProgrammingTimeout(DurationPlusUnit programmingTimeout) {
        this.programmingTimeout = programmingTimeout;
    }

    /**
     * @return the maxRerequests
     */
    public short getMaxRerequests() {
        return maxRerequests;
    }

    /**
     * @param maxRerequests
     *            the maxRerequests to set
     */
    public void setMaxRerequests(short maxRerequests) {
        this.maxRerequests = maxRerequests;
    }

    /**
     * @return the timeoutMultiplier
     */
    public short getTimeoutMultiplier() {
        return timeoutMultiplier;
    }

    /**
     * @param timeoutMultiplier
     *            the timeoutMultiplier to set
     */
    public void setTimeoutMultiplier(short timeoutMultiplier) {
        this.timeoutMultiplier = timeoutMultiplier;
    }

    /**
     * @return the aesKey
     */
    public iSenseAes128BitKey getAesKey() {
        return aesKey;
    }

    /**
     * @param aesKey
     *            the aesKey to set
     */
    public void setAesKey(iSenseAes128BitKey aesKey) {
        this.aesKey = aesKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ISenseOtapAutomatedProgrammingRequest [presenceDetectTimeout=");
        builder.append(presenceDetectTimeout);
        builder.append(", otapInitTimeout=");
        builder.append(otapInitTimeout);
        builder.append(", programmingTimeout=");
        builder.append(programmingTimeout);
        builder.append(", maxRerequests=");
        builder.append(maxRerequests);
        builder.append(", timeoutMultiplier=");
        builder.append(timeoutMultiplier);
        builder.append(", getDevicesToProgram()=");
        builder.append(getDevicesToProgram());
        builder.append(", AES payload encryption used: ");
        builder.append(aesKey != null ? "yes" : "no");
        builder.append(", getOtapProgram()=");
        builder.append(getOtapProgram().length);
        builder.append("bytes]");
        return builder.toString();
    }

}
