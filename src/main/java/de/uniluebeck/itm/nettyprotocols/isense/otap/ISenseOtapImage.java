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

import de.uniluebeck.itm.util.files.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Set;
import java.util.TreeSet;

public class ISenseOtapImage {

	private static final Logger log = LoggerFactory.getLogger(ISenseOtapImage.class);

	/**
	 * Number of OtapPackets in one chunk
	 */
	private static final int MAX_CHUNK_SIZE = 64;

	/**
	 * Number of payload bytes in one OtapPacket
	 */
	private static final int MAX_PACKET_CODE_SIZE = 64;

	private Set<ISenseOtapChunk> chunks = new TreeSet<ISenseOtapChunk>();

	private ChipType chipType = ChipType.UNKNOWN;

	private byte[] bytes = null;

	public ISenseOtapImage(File imageFile) throws FileNotFoundException, IOException {
		this(new FileInputStream(imageFile));
	}

	public ISenseOtapImage(InputStream imageStream) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		FileUtils.copy(imageStream, buffer);
		init(buffer.toByteArray());
	}

	public ISenseOtapImage(byte[] otapProgram) {
		init(otapProgram);
	}

	private void init(byte[] otapProgram) {
		bytes = otapProgram;

		// Determine chip type of the file
		chipType = determineFileType(getBytes());

		byte[] packet = new byte[MAX_PACKET_CODE_SIZE];
		int packetLen;
		int address = 0;

		ISenseOtapChunk currentChunk = new ISenseOtapChunk((short) 0);
		chunks.add(currentChunk);

		ByteArrayInputStream in = new ByteArrayInputStream(getBytes());

		while ((packetLen = in.read(packet, 0, MAX_PACKET_CODE_SIZE)) > 0) {

			// Check if the current chunk is full
			if (currentChunk.getPacketCount() >= MAX_CHUNK_SIZE) {
				log.trace("Current chunk #{} full ({} packets), creating new one", currentChunk.getChunkNumber(),
						currentChunk.getPacketCount()
				);
				currentChunk = new ISenseOtapChunk((short) (currentChunk.getChunkNumber() + 1));
				chunks.add(currentChunk);
			}

			// Create the next packet
			ISenseOtapPacket p = new ISenseOtapPacket(address, packet, 0, packetLen);
			currentChunk.addPacket(p);

			log.trace("Added otap packet: " + p);

			address += packetLen;
		}

		log.info("Done loading file, got " + chunks.size() + " chunks.");
	}

	public ISenseOtapChunk getChunk(int number) {
		for (ISenseOtapChunk c : chunks) {
			if (c.getChunkNumber() == number) {
				return c;
			}
		}
		return null;
	}

	public int getPacketCount() {
		int p = 0;
		for (ISenseOtapChunk c : chunks) {
			p += c.getPacketCount();
		}
		return p;
	}

	public int getPacketCount(int minChunkIncl, int maxChunkIncl) {
		int p = 0;

		if (minChunkIncl > maxChunkIncl) {
			return 0;
		}

		for (int i = minChunkIncl; i <= maxChunkIncl; ++i) {
			ISenseOtapChunk c = getChunk(i);
			if (c == null) {
				continue;
			}
			p += c.getPacketCount();
		}

		return p;
	}

	public short getChunkCount() {
		return (short) chunks.size();
	}

	private ChipType determineFileType(byte[] bytes) {

		if (bytes[0] == (byte) 0xE1) {
			log.debug("Chip type is JN5121");
			return ChipType.JN5121;

		} else if (hasRepeatedPattern(bytes, 0, 4, (byte) 0xE0)) {
			log.trace("Start matches 4 x 0xE0 -> Could be JN513XR1 or JN513XR1");

			{// JN513XR1
				// OAD
				int start = 0x24, count = 8;
				boolean ok = hasRepeatedPattern(bytes, start, count, (byte) 0xFF);
				start += count;
				count = 4;
				ok = ok && hasRepeatedPattern(bytes, start, count, (byte) 0xF0);

				if (ok) {
					log.trace("OAD Section found (8 x 0xFF, 4 x 0xF0)");
				} else {
					log.trace("No OAD Section found -> not a JN513XR1");
				}

				// MAC address
				if (ok) {
					start += count;
					count = 32;
					ok = hasRepeatedPattern(bytes, start, count, (byte) 0xFF);

					if (ok) {
						log.trace("MAC Section found (32 x 0xFF)");
						log.debug("Chip type of binary file is JN513XR1");
						return ChipType.JN513XR1;
					}
				}
			}

			{// JN513X
				// MAC
				int start = 0x24, count = 32;
				boolean ok = hasRepeatedPattern(bytes, start, count, (byte) 0xFF);

				if (ok) {
					log.debug("Chip type of binary file is JN513X");
					return ChipType.JN513X;
				}
			}

			// TODO Add missing chip types
		} else if ((bytes[0] == 0x00) && (bytes[1] == 0) && (bytes[2] == (byte) 0xe0) && (bytes[3] == (byte) 0xe0)) {
			log.debug("File type is JN5148");
			return ChipType.JN5148;
		}
		log.error("Chip type of the given file is UNKNOWN");
		return ChipType.UNKNOWN;
	}

	private boolean hasRepeatedPattern(byte b[], int offset, int repeat, byte pattern) {

		for (int i = 0; i < repeat; ++i) {
			if (b[offset + i] != pattern) {
				return false;
			}
		}

		return true;
	}

	public ChipType getChipType() {
		return chipType;
	}

	public byte[] getBytes() {
		return bytes;
	}

}
