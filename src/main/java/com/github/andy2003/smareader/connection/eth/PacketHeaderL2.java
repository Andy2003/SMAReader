package com.github.andy2003.smareader.connection.eth;

import java.nio.ByteBuffer;

public class PacketHeaderL2 {
	public int magicNumber; // Level 2 packet signature 00 10 60 65

	public PacketHeaderL2(ByteBuffer bb) {
		magicNumber = bb.getInt();
		int longWords = bb.get();
		int ctrl = bb.get();
	}

	public static short getSize() {
		short size = 0;
		size += Integer.SIZE / 8;
		size += Byte.SIZE / 8;
		size += Byte.SIZE / 8;
		return size;
	}
}
