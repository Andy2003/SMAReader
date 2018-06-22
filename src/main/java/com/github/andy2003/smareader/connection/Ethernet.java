package com.github.andy2003.smareader.connection;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ethernet extends SBFNet implements AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger(Ethernet.class);

	private int maxCommbuf = 0;

	private DatagramSocket sock;
	private short port;

	/***
	 * Connects the socket to the port
	 *
	 * @param port The port to connect to.
	 */
	public void connect(short port) throws SocketException {
		LOGGER.debug("Initialising Socket");
		sock = new DatagramSocket();
		// set up parameters for UDP
		this.port = port;
		sock.setBroadcast(true);
	}

	/**
	 * Disconnects and closes the socket connection.
	 */
	@Override
	public void close() {
		sock.disconnect();
		sock.close();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	/***
	 * Clears the buffer and sets packetposition to 0.
	 */
	public void clearBuffer() {
		this.packetposition = 0;
		pcktBuf = new byte[MAXPCKT_BUFSIZE];
	}

	/***
	 * Reads incoming data from the socket
	 *
	 * @param buf The buffer that holds the incoming data.
	 * @return Number of bytes read.
	 */
	public int read(byte[] buf) {
		boolean keepReading = true;
		int bytesRead = 0;
		short timeout = 5; // 5 seconds

		while (keepReading) {
			DatagramPacket recv = new DatagramPacket(buf, buf.length);
			try {
				sock.setSoTimeout((int) TimeUnit.SECONDS.toMillis(timeout));
			} catch (SocketException e) {
				LOGGER.warn("Error setting timeout socket {}", e.getMessage());
				return -1;
			}
			try {
				sock.receive(recv);
				bytesRead = recv.getLength();
			} catch (SocketTimeoutException e1) {
				LOGGER.trace("Timeout reading socket");
				return -1;
			} catch (IOException e) {
				LOGGER.trace("Error reading socket {}", e.getMessage());
				return -1;
			}

			if (bytesRead > 0) {
				if (bytesRead > maxCommbuf) {
					maxCommbuf = bytesRead;
				}
			}

			if (bytesRead == 600) {
				timeout--; // decrease timeout if the packet received within the timeout is an energymeter packet
			} else {
				keepReading = false;
			}
		}
		return bytesRead;
	}

	/***
	 * Sends what's currently stored in the buffer.
	 *
	 * @param toIP The ip addres to send it to.
	 */
	public void send(String toIP) throws IOException {
		DatagramPacket p = new DatagramPacket(pcktBuf, packetposition, new InetSocketAddress(toIP, port));
		sock.send(p);
	}
}
