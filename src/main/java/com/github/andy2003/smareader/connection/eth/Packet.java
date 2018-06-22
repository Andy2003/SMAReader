package com.github.andy2003.smareader.connection.eth;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Packet {
	private byte dummy0;
	private PacketHeaderL2 pcktHdrL2;
	private Endpoint destination;
	public Endpoint source;
	public short errorCode;
	private short fragmentID; // Count Down
	public short packetID; // Count Up

	public Packet(byte[] packet) {
		ByteBuffer bb = ByteBuffer.wrap(packet);
		bb.order(ByteOrder.LITTLE_ENDIAN);

		dummy0 = bb.get();
		pcktHdrL2 = new PacketHeaderL2(bb);
		destination = new Endpoint(bb);
		source = new Endpoint(bb);
		errorCode = bb.getShort();
		fragmentID = bb.getShort();
		packetID = bb.getShort();
	}

	public static class Endpoint {
		public short susyid;
		public int serial;
		private short ctrl;

		Endpoint(ByteBuffer bb) {
			susyid = bb.getShort();
			serial = bb.getInt();
			ctrl = bb.getShort();
		}
	}
}
