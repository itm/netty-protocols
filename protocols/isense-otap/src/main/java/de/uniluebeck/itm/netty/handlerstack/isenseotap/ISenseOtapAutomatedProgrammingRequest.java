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

import com.coalesenses.tools.iSenseAes128BitKey;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.program.ISenseOtapProgramRequest;
import de.uniluebeck.itm.netty.handlerstack.util.DurationPlusUnit;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Root
@Default(DefaultType.FIELD)
public class ISenseOtapAutomatedProgrammingRequest extends ISenseOtapProgramRequest {

    public static final String SERIALIZATION_HEADER = "ISenseOtapAutomatedProgrammingRequest-version1";

    private long presenceDetectTimeout = new DurationPlusUnit(10, TimeUnit.SECONDS).toMillis();

    private long otapInitTimeout = new DurationPlusUnit(10, TimeUnit.SECONDS).toMillis();

    private long programmingTimeout = new DurationPlusUnit(15, TimeUnit.MINUTES).toMillis();

    private short maxRerequests = 0x1e;

    private short timeoutMultiplier = 10;

    @Element(required = false)
    private byte[] aesKey = null;

    protected ISenseOtapAutomatedProgrammingRequest() {
    }

    public ISenseOtapAutomatedProgrammingRequest(Set<Integer> otapDevices, byte[] otapProgram) {
        super(otapDevices, otapProgram);
    }

    /**
     * @return the presenceDetectTimeout
     */
    public DurationPlusUnit getPresenceDetectTimeoutAsDurationPlusUnit() {
        return new DurationPlusUnit(presenceDetectTimeout, TimeUnit.MILLISECONDS);
    }

    /**
     * @param presenceDetectTimeout the presenceDetectTimeout to set
     */
    public void setPresenceDetectTimeoutFromDurationPlusUnit(DurationPlusUnit presenceDetectTimeout) {
        this.presenceDetectTimeout = presenceDetectTimeout.toMillis();
    }

    /**
     * @return the otapInitTimeout
     */
    public DurationPlusUnit getOtapInitTimeoutAsDurationPlusUnit() {
        return new DurationPlusUnit(otapInitTimeout, TimeUnit.MILLISECONDS);
    }

    /**
     * @param otapInitTimeout the otapInitTimeout to set
     */
    public void setOtapInitTimeoutFromDurationPlusUnit(DurationPlusUnit otapInitTimeout) {
        this.otapInitTimeout = otapInitTimeout.toMillis();
    }

    /**
     * @return the programmingTimeout
     */
    public DurationPlusUnit getProgrammingTimeoutAsDurationPlusUnit() {
        return new DurationPlusUnit(programmingTimeout, TimeUnit.MILLISECONDS);
    }

    /**
     * @param programmingTimeout the programmingTimeout to set
     */
    public void setProgrammingTimeoutFromDurationPlusUnit(DurationPlusUnit programmingTimeout) {
        this.programmingTimeout = programmingTimeout.toMillis();
    }

    /**
     * @return the maxRerequests
     */
    public short getMaxRerequests() {
        return maxRerequests;
    }

    /**
     * @param maxRerequests the maxRerequests to set
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
     * @param timeoutMultiplier the timeoutMultiplier to set
     */
    public void setTimeoutMultiplier(short timeoutMultiplier) {
        this.timeoutMultiplier = timeoutMultiplier;
    }

    /**
     * @return the aesKey
     */
    public iSenseAes128BitKey getAesKeyAsISenseAes128BitKey() {
        return aesKey == null ? null : new iSenseAes128BitKey(aesKey);
    }

    /**
     * @param aesKey the aesKey to set
     */
    public void setAesKeyFromISenseAes128BitKey(iSenseAes128BitKey aesKey) {
        this.aesKey = aesKey.getAes128BitKey();
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

    public long getOtapInitTimeout() {
        return otapInitTimeout;
    }

    public void setOtapInitTimeout(final long otapInitTimeout) {
        this.otapInitTimeout = otapInitTimeout;
    }

    public long getPresenceDetectTimeout() {
        return presenceDetectTimeout;
    }

    public void setPresenceDetectTimeout(final long presenceDetectTimeout) {
        this.presenceDetectTimeout = presenceDetectTimeout;
    }

    public long getProgrammingTimeout() {
        return programmingTimeout;
    }

    public void setProgrammingTimeout(final long programmingTimeout) {
        this.programmingTimeout = programmingTimeout;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final ISenseOtapAutomatedProgrammingRequest that = (ISenseOtapAutomatedProgrammingRequest) o;

        if (maxRerequests != that.maxRerequests) {
            return false;
        }
        if (otapInitTimeout != that.otapInitTimeout) {
            return false;
        }
        if (presenceDetectTimeout != that.presenceDetectTimeout) {
            return false;
        }
        if (programmingTimeout != that.programmingTimeout) {
            return false;
        }
        if (timeoutMultiplier != that.timeoutMultiplier) {
            return false;
        }
        if (!Arrays.equals(aesKey, that.aesKey)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (presenceDetectTimeout ^ (presenceDetectTimeout >>> 32));
        result = 31 * result + (int) (otapInitTimeout ^ (otapInitTimeout >>> 32));
        result = 31 * result + (int) (programmingTimeout ^ (programmingTimeout >>> 32));
        result = 31 * result + (int) maxRerequests;
        result = 31 * result + (int) timeoutMultiplier;
        result = 31 * result + (aesKey != null ? Arrays.hashCode(aesKey) : 0);
        return result;
    }

    public byte[] getAesKey() {
        return aesKey;
    }

    public void setAesKey(final byte[] aesKey) {
        this.aesKey = aesKey;
    }
}
