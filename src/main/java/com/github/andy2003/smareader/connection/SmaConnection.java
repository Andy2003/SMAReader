package com.github.andy2003.smareader.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.github.andy2003.smareader.SmaReader;
import com.github.andy2003.smareader.connection.eth.PacketHeaderL1;
import com.github.andy2003.smareader.connection.eth.PacketHeaderL1L2;
import com.github.andy2003.smareader.inverter.InverterDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmaConnection implements AutoCloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(SmaConnection.class);
	private static final int COMMBUFSIZE = 1024;
	private static final short ANY_SU_SY_ID = (short) 0xFFFF;
	private static final long ANY_SERIAL = 0xFFFFFFFF;

	protected Ethernet ethernet;

	private String ip;
	private byte[] commBuf;

	public SmaConnection(Ethernet eth, String inverterIP) {
		this.ethernet = eth;
		this.ip = inverterIP;
		this.commBuf = new byte[COMMBUFSIZE];
	}

	protected void initConnection() throws IOException {
		ethernet.writePacketHeader();
		ethernet.writePacket((char) 0x09, (char) 0xA0, (short) 0, ANY_SU_SY_ID, ANY_SERIAL);
		ethernet.writeLong(0x00000200);
		ethernet.writeLong(0);
		ethernet.writeLong(0);
		ethernet.writeLong(0);
		ethernet.writePacketLength();

		// Send packet to first inverter
		ethernet.send(ip);
	}

	protected void smaLogin(long userGroup, char[] password) throws IOException {
		final int maxPwlength = 12;
		char pw[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		LOGGER.debug("SMALogin()");

		char encChar = (char) ((userGroup == SmaReader.UG_USER) ? 0x88 : 0xBB);
		// Encode password
		int idx;
		for (idx = 0; (password[idx] != 0) && (idx <= pw.length); idx++) {
			pw[idx] = (char) (password[idx] + encChar);
		}
		for (; idx < maxPwlength; idx++)
			pw[idx] = encChar;

		long now;

		// I believe the inverter times is using seconds instead of milliseconds.
		now = System.currentTimeMillis() / 1000L;
		ethernet.writePacketHeader();
		ethernet.writePacket((char) 0x0E, (char) 0xA0, (short) 0x0100, ANY_SU_SY_ID, ANY_SERIAL);
		ethernet.writeLong(0xFFFD040C);
		ethernet.writeLong(userGroup);
		ethernet.writeLong(0x00000384);
		ethernet.writeLong(now);
		ethernet.writeLong(0);
		ethernet.writeArray(pw, pw.length);
		ethernet.writePacketTrailer();
		ethernet.writePacketLength();

		ethernet.send(ip);
	}

	protected void smaLogoff() throws IOException {
		LOGGER.debug("SMALogoff()");

		ethernet.writePacketHeader();
		ethernet.writePacket((char) 0x08, (char) 0xA0, (short) 0x0300, ANY_SU_SY_ID, ANY_SERIAL);
		ethernet.writeLong(0xFFFD010E);
		ethernet.writeLong(0xFFFFFFFF);
		ethernet.writePacketTrailer();
		ethernet.writePacketLength();
		ethernet.send(ip);
	}

	@Override
	public void close() throws IOException {
		smaLogoff();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	protected void requestInverterData(InverterDataType dataType) throws IOException {
		ethernet.writePacketHeader();
		ethernet.writePacket((char) 0x09, (char) 0xA0, (short) 0, ANY_SU_SY_ID, ANY_SERIAL);
		// Get the command values stored in the enum.
		ethernet.writeLong(dataType.command);
		ethernet.writeLong(dataType.first);
		ethernet.writeLong(dataType.last);
		ethernet.writePacketTrailer();
		ethernet.writePacketLength();

		ethernet.send(ip);
	}

	protected boolean getPacket() {
		// commBuf = new byte[COMMBUFSIZE];
		boolean retry;
		boolean successful = true;

		do {
			retry = false;
			int bib = ethernet.read(commBuf);

			if (bib <= 0) {
				successful = false;
				continue;
			}
			PacketHeaderL1L2 pkHdr = new PacketHeaderL1L2(commBuf);
			int pkLen = ((pkHdr.pcktHdrL1.hiPacketLen << 8) + pkHdr.pcktHdrL1.loPacketLen) & 0xff; // 0xff to convert it
			// to unsigned?

			// More data after header?
			if (pkLen <= 0) {
				successful = false;
				continue;
			}

			if (intSwap(pkHdr.pcktHdrL2.magicNumber) == SBFNet.ETH_L2SIGNATURE) {
				// Copy commBuf to packetbuffer
				// Dummy byte to align with BTH (7E)
				ethernet.pcktBuf[0] = 0;
				// We need last 6 bytes of ethPacketHeader too
				System.arraycopy(commBuf, PacketHeaderL1.getSize(), ethernet.pcktBuf, 1,
						bib - PacketHeaderL1.getSize());

				// Point packetposition at last byte in our buffer
				// This is different from BTH
				ethernet.packetposition = bib - PacketHeaderL1.getSize();
			} else {
				retry = true;
			}
		} while (retry);
		return successful;
	}

	/**
	 * Returns the ip adress of this inverter.
	 *
	 * @return A string containing the IP adress of the inverter.
	 */
	public String getIp() {
		return ip;
	}

	protected static final int NAN_S32 = (int) 0x80000000L; // "Not a Number" representation for LONG (converted to 0 by
	// SBFspot)
	protected static final int NAN_U32 = (int) 0xFFFFFFFFL; // "Not a Number" representation for ULONG (converted to 0
	// by SBFspot)
	protected static final long NAN_S64 = 0x8000000000000000L; // "Not a Number" representation for LONGLONG (converted
	// to 0 by SBFspot)
	protected static final long NAN_U64 = 0xFFFFFFFFFFFFFFFFL; // "Not a Number" representation for ULONGLONG (converted
	// to 0 by SBFspot)

	private static int intSwap(int i) {
		// return i;
		return (i & 0xff) << 24 | (i & 0xff00) << 8 | (i & 0xff0000) >> 8 | (i >> 24) & 0xff;
	}

	protected static short shortSwap(short s) {
		// return s;
		int b1 = s & 0xff;
		int b2 = (s >> 8) & 0xff;

		return (short) (b1 << 8 | b2);
	}

	protected static long parseLong(byte[] buf, int pos) {
		ByteBuffer bb;
		bb = ByteBuffer.wrap(buf);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.position(pos);
		return bb.getLong();
	}

	protected static int parseInt(byte[] buf, int pos) {
		ByteBuffer bb;
		bb = ByteBuffer.wrap(buf);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.position(pos);
		return bb.getInt();
	}

	protected static short parseShort(byte[] buf, int pos) {
		ByteBuffer bb;
		bb = ByteBuffer.wrap(buf);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.position(pos);
		return bb.getShort();
	}
}
