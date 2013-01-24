/**********************************************************************************************************************
 * Copyright (c) 2010, coalesenses GmbH                                                                               *
 * All rights reserved.                                                                                               *
 *                                                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
 * following conditions are met:                                                                                      *
 *                                                                                                                    *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
 *   disclaimer.                                                                                                      *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
 *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
 * - Neither the name of the coalesenses GmbH nor the names of its contributors may be used to endorse or promote     *
 *   products derived from this software without specific prior written permission.                                   *
 *                                                                                                                    *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE      *
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,         *
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE *
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY   *
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                                *
 **********************************************************************************************************************/

package de.uniluebeck.itm.nettyprotocols.isense.otap;


/**
 * Enum of ChipTypes.
 *
 * @author Malte Legenhausen
 */
public enum ChipType {

	/**
	 * Mode for JN5121 platform.
	 */
	JN5121("JN5121", 0, 0x24, 0x20, 0x24),
	/**
	 * Mode for JN513X platform.
	 */
	JN513X("JN513x", 1, 0x24, 0x20, 0x24),
	/**
	 * Mode for JN513XR1 platform.
	 */
	JN513XR1("JN513xR1", 2, 0x30, 0x20, 0x30),
	/**
	 * Mode for JN5148 platform.
	 */
	JN5148("JN5148", 3, 0x30, 0x20, 0x30),
	/**
	 * Mode for Shawn simulator.
	 */
	Shawn("Shawn", 4),
	/**
	 * Mode for Telos Revision B (TelosB) platform.
	 */
	TelosB("Telos Rev B", 5),
	/**
	 * Mode for Pacemate LPC2136 platform.
	 */
	LPC2136("LPC2136 Pacemate", 6),
	/**
	 * Mode for unknown platform.
	 */
	UNKNOWN("Unknown", -1);

	/**
	 * Human readable representation of the chip type.
	 */
	private final String name;

	/**
	 * Chip type in short representation.
	 */
	private final short type;

	/**
	 * Start address of the header.
	 */
	private final int headerStart;

	/**
	 * Length of the header.
	 */
	private final int headerLength;

	/**
	 * Start address of the mac address.
	 */
	private final int macInFlashStart;

	/**
	 * Constructor.
	 *
	 * @param name
	 * 		The name of the chip.
	 * @param type
	 * 		The short representation of the chip.
	 */
	private ChipType(final String name, final int type) {
		this(name, type, -1, -1, -1);
	}

	/**
	 * Constructor.
	 *
	 * @param name
	 * 		The name of the chip.
	 * @param type
	 * 		The short representation of the chip.
	 * @param headerStart
	 * 		Offset of the header start.
	 * @param headerLength
	 * 		Length of the header.
	 * @param macInFlashStart
	 * 		The start address of the MAC address.
	 */
	private ChipType(String name, int type, int headerStart, int headerLength, int macInFlashStart) {
		this.name = name;
		this.type = (short) type;
		this.headerStart = headerStart;
		this.headerLength = headerLength;
		this.macInFlashStart = macInFlashStart;
	}

	/**
	 * Getter for the human readable name.
	 *
	 * @return The name as string.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for the short representation of the chip type.
	 *
	 * @return Short representation of the chip type.
	 */
	public short getType() {
		return type;
	}

	/**
	 * Getter for the header start address.
	 *
	 * @return The header start address.
	 */
	public int getHeaderStart() {
		return headerStart;
	}

	/**
	 * Getter for the header length.
	 *
	 * @return The header length.
	 */
	public int getHeaderLength() {
		return headerLength;
	}

	/**
	 * Getter for the start address of the mac address.
	 *
	 * @return The start address of the mac address.
	 */
	public int getMacInFlashStart() {
		return macInFlashStart;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Returns the ChipType for a given short.
	 *
	 * @param type
	 * 		Short of the ChipType that has to be returned.
	 *
	 * @return ChipType associated with the given type.
	 */
	public static ChipType getChipType(final short type) {
		for (ChipType chipType : ChipType.values()) {
			if (chipType.getType() == type) {
				return chipType;
			}
		}
		return ChipType.UNKNOWN;
	}
}
