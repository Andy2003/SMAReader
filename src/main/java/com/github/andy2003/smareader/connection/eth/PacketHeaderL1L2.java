package com.github.andy2003.smareader.connection.eth;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketHeaderL1L2 {

	public PacketHeaderL1 pcktHdrL1;
	public PacketHeaderL2 pcktHdrL2;

	public PacketHeaderL1L2(byte[] packet) {
		ByteBuffer bb = ByteBuffer.wrap(packet);
		bb.order(ByteOrder.BIG_ENDIAN); // or LITTLE_ENDIAN
		pcktHdrL1 = new PacketHeaderL1(bb);
		pcktHdrL2 = new PacketHeaderL2(bb);
	}

	public static short getSize() {
		return (short) (PacketHeaderL1.getSize() + PacketHeaderL2.getSize());
	}
}
