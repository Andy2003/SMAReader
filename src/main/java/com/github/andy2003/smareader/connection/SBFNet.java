package com.github.andy2003.smareader.connection;

import com.github.andy2003.smareader.connection.eth.PacketHeaderL1L2;

public class SBFNet {
	static final int MAXPCKT_BUFSIZE = 520;
	public static final long ETH_L2SIGNATURE = 0x65601000;

	public int packetposition = 0;
	public byte[] pcktBuf = new byte[MAXPCKT_BUFSIZE];

	public short pcktID = 1;

	public short appSUSyID;
	public long appSerial;

	public void writePacketHeader() {
		packetposition = 0;
		// Ignore control and destaddress
		writeLong(0x00414D53); // SMA\0
		writeLong(0xA0020400);
		writeLong(0x01000000);
		writeByte((byte) 0);
		writeByte((byte) 0); // Placeholder for packet length
	}

	public void writePacketTrailer() {
		writeLong(0);
	}

	public void writePacketLength() {
		short dataLength = (short) (packetposition - PacketHeaderL1L2.getSize());
		pcktBuf[12] = (byte) ((dataLength >> 8) & 0xFF);
		pcktBuf[13] = (byte) (dataLength & 0xFF);
	}

	public void writeArray(char bytes[], int loopcount) {
		for (int i = 0; i < loopcount; i++) {
			writeByte((byte) bytes[i]);
		}
	}

	public void writePacket(char longwords, char ctrl, short ctrl2, short dstSUSyID, long dstSerial) {
		// Upping the packet id here so it doesn't have to be done manually.
		pcktID++;
		writeLong(ETH_L2SIGNATURE);

		writeByte((byte) longwords);
		writeByte((byte) ctrl);
		writeShort(dstSUSyID);
		writeLong(dstSerial);
		writeShort(ctrl2);
		writeShort(appSUSyID);
		writeLong(appSerial);
		writeShort(ctrl2);
		writeShort((short) 0);
		writeShort((short) 0);
		writeShort((short) (pcktID | 0x8000));
	}

	public void writeLong(long v) {
		writeByte((byte) ((v) & 0xFF));
		writeByte((byte) ((v >> 8) & 0xFF));
		writeByte((byte) ((v >> 16) & 0xFF));
		writeByte((byte) ((v >> 24) & 0xFF));
	}

	private void writeShort(short v) {
		writeByte((byte) ((v) & 0xFF));
		writeByte((byte) ((v >> 8) & 0xFF));
	}

	private void writeByte(byte v) {
		pcktBuf[packetposition++] = v;
	}
}
