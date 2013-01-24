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
package de.uniluebeck.itm.netty.isense.otap.program;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Root;

import java.util.HashSet;
import java.util.Set;

@Root
@Default(DefaultType.FIELD)
public class ISenseOtapProgramResult {

    public static final String SERIALIZATION_HEADER = "ISenseOtapProgramResult-version1";;

    protected Set<Integer> devicesToBeProgrammed;

    protected Set<Integer> failedDevices = new HashSet<Integer>();
    protected Set<Integer> doneDevices = new HashSet<Integer>();

	protected ISenseOtapProgramResult() {
	}

	public ISenseOtapProgramResult(Set<Integer> devicesToBeProgrammed) {
        this.devicesToBeProgrammed = new HashSet<Integer>(devicesToBeProgrammed);
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

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final ISenseOtapProgramResult that = (ISenseOtapProgramResult) o;

		if (!devicesToBeProgrammed.equals(that.devicesToBeProgrammed)) {
			return false;
		}
		if (!doneDevices.equals(that.doneDevices)) {
			return false;
		}
		if (!failedDevices.equals(that.failedDevices)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = devicesToBeProgrammed.hashCode();
		result = 31 * result + failedDevices.hashCode();
		result = 31 * result + doneDevices.hashCode();
		return result;
	}
}
