package com.github.andy2003.smareader.connection.eth;

import java.nio.ByteBuffer;

public class PacketHeaderL1 {
	public byte hiPacketLen; // Packet length stored as big endian
	public byte loPacketLen; // Packet length Low Byte

	PacketHeaderL1(ByteBuffer bb) {
		int magicNumber = bb.getInt();
		int unknown1 = bb.getInt();
		int unknown2 = bb.getInt();
		hiPacketLen = bb.get();
		loPacketLen = bb.get();
	}

	public static short getSize() {
		short size = 0;
		size += Integer.SIZE / 8;
		size += Integer.SIZE / 8;
		size += Integer.SIZE / 8;
		size += Byte.SIZE / 8;
		size += Byte.SIZE / 8;
		return size;
	}
}
