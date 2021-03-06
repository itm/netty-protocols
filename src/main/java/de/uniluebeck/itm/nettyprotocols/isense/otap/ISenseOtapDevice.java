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
package de.uniluebeck.itm.nettyprotocols.isense.otap;

import de.uniluebeck.itm.util.TimeDiff;

/**
 * @author Dennis Pfisterer
 */
public class ISenseOtapDevice {

	private int id;

	private ChipType chipType = ChipType.UNKNOWN;

	private int lqi;

	private TimeDiff lastReception = new TimeDiff();

	private TimeDiff lastMessageTransmitted = new TimeDiff();

	private boolean chunkComplete = false;

	private int chunkNo = -1;

	private long applicationID = 0;

	private int softwareRevision = 0;

	private int protocolVersion = 0;

	public ISenseOtapDevice() {
	}

	public ISenseOtapDevice(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public TimeDiff getLastReception() {
		return lastReception;
	}

	public TimeDiff getLastMessageTransmitted() {
		return lastMessageTransmitted;
	}

	public void setLastReception() {
		this.lastReception.touch();
	}

	public void setLastMessageTransmitted() {
		this.lastMessageTransmitted.touch();
	}

	public int getLqi() {
		return lqi;
	}

	public void setLqi(int lqi) {
		this.lqi = lqi;
	}

	public boolean isChunkComplete() {
		return chunkComplete;
	}

	public void setChunkComplete(boolean chunkComplete) {
		this.chunkComplete = chunkComplete;
	}

	public ChipType getChipType() {
		return chipType;
	}

	public void setChipType(ChipType chipType) {
		this.chipType = chipType;
	}

	public int getChunkNo() {
		return chunkNo;
	}

	public void setChunkNo(int chunkNo) {
		this.chunkNo = chunkNo;
	}

	public long getApplicationID() {
		return applicationID;
	}

	public void setApplicationID(long applicationID) {
		this.applicationID = applicationID;
	}

	public int getSoftwareRevision() {
		return softwareRevision;
	}

	public void setSoftwareRevision(int softwareRevision) {
		this.softwareRevision = softwareRevision;
	}

	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#toString()
		 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ISenseOtapDevice [id=");
		builder.append(id);
		builder.append(", chipType=");
		builder.append(chipType);
		builder.append(", lqi=");
		builder.append(lqi);
		builder.append(", lastReception=");
		builder.append(lastReception);
		builder.append(", lastMessageTransmitted=");
		builder.append(lastMessageTransmitted);
		builder.append(", chunkComplete=");
		builder.append(chunkComplete);
		builder.append(", chunkNo=");
		builder.append(chunkNo);
		builder.append(", applicationID=");
		builder.append(applicationID);
		builder.append(", softwareRevision=");
		builder.append(softwareRevision);
		builder.append(", protocolVersion=");
		builder.append(protocolVersion);
		builder.append("]");
		return builder.toString();
	}

	/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#hashCode()
		 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ISenseOtapDevice other = (ISenseOtapDevice) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

}
