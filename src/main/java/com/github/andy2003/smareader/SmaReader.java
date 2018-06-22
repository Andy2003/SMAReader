package com.github.andy2003.smareader;

import java.io.IOException;
import java.net.SocketException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

import com.github.andy2003.smareader.connection.Ethernet;
import com.github.andy2003.smareader.inverter.Inverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmaReader implements AutoCloseable {
	public static final long UG_USER = 0x07L;
	public static final long UG_INSTALLER = 0x0AL;

	private static final Logger LOGGER = LoggerFactory.getLogger(SmaReader.class);
	private static final String BROADCAST_IP = "239.12.255.254";

	private Ethernet ethernet;

	/**
	 * Creates a new instance of the SMALogger and it's ethernet connection.
	 */
	public SmaReader() throws SocketException {
		ethernet = new Ethernet();
		initialize();
	}

	/**
	 * Initializes the SMALogger, also intializes and creates the ethernet
	 * connection.
	 */
	private void initialize() throws SocketException {
		// So the port was hardcoded in the config so why not hardcode it here, for now...
		ethernet.connect((short) 9522);

		// Generate a serial Number for application
		short appSUSyID = 125;
		Random r = new Random(System.nanoTime());
		long appSerial = 900000000 + ((r.nextInt() << 16) + r.nextInt()) % 100000000;
		ethernet.appSUSyID = appSUSyID;
		ethernet.appSerial = appSerial;
		LOGGER.debug("SUSyID: {} - SessionID: {} ({})", appSUSyID, appSerial, String.format("0x%08X", appSerial));
	}

	/**
	 * Shuts down the SMALogger and closes it's ethernet connection.
	 */
	@Override
	public void close() {
		ethernet.close();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	/**
	 * Used to manually create an inverter with a given ip adres, this method
	 * gives the inverter a socket connection used for communication.
	 *
	 * @param ip The ip adress of the inverter.
	 * @return An inverter with the ip adress and a socket connection.
	 */
	public Inverter createInverter(String ip) {
		// Create a new inverter and give it the socket connection.
		return new Inverter(ip, ethernet);
	}

	/**
	 * Sends a broadcast message over the network the detect inverters.
	 *
	 * @return A list of found inverters. If no inverters are found, the list is empty.
	 */
	public Collection<Inverter> detectDevices() throws IOException {
		HashMap<String, Inverter> inverters = new LinkedHashMap<>();

		// Start with UDP broadcast to check for SMA devices on the LAN
		sendBroadcastMessage();

		// SMA inverter announces it's presence in response to the discovery request packet
		int bytesRead = 1;
		byte[] commBuf = new byte[1024];

		// Untested, the idea is to keep listening if there are multiple inverters.
		while (bytesRead > 0) {
			// if bytesRead < 0, a timeout has occurred
			// if bytesRead == 0, no data was received
			bytesRead = ethernet.read(commBuf);

			// Only do this if we actually got some data
			if (bytesRead > 0) {
				// Retrieve the ip adress from the received package.
				String ip = String.format("%d.%d.%d.%d", (commBuf[38] & 0xFF), (commBuf[39] & 0xFF),
						(commBuf[40] & 0xFF), (commBuf[41] & 0xFF));

				LOGGER.debug("Inverter IP address: {} found via broadcastidentification", ip);

				inverters.computeIfAbsent(ip, this::createInverter);
			}
		}

		return inverters.values();
	}

	private void sendBroadcastMessage() throws IOException {
		// Clear the buffer and set packet position to 0.
		ethernet.clearBuffer();

		ethernet.writeLong(0x00414D53); // Start of SMA header
		ethernet.writeLong(0xA0020400); // Unknown
		ethernet.writeLong(0xFFFFFFFF); // Unknown
		ethernet.writeLong(0x20000000); // Unknown
		ethernet.writeLong(0x00000000); // Unknown

		ethernet.send(BROADCAST_IP);
	}
}
