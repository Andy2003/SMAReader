package com.github.andy2003.smareader.inverter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.github.andy2003.smareader.SmaReader;
import com.github.andy2003.smareader.connection.Ethernet;
import com.github.andy2003.smareader.connection.SmaConnection;
import com.github.andy2003.smareader.connection.eth.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Inverter extends SmaConnection {
	private static final Logger LOGGER = LoggerFactory.getLogger(Inverter.class);

	private Map<LriDef, TagValue> values = new HashMap<>();

	private short susyId;
	private long serial;
	private String deviceName;
	private String swVersion;
	private InvDeviceClass deviceClass;
	private String deviceClassName;
	private String deviceType;

	/**
	 * Creates a new inverter.
	 * NOTE: This method is used by the SMALogger and should not be uses manually.
	 * Use {@link SmaReader#createInverter(String)} to create an inverter instead.
	 *
	 * @param ip The ip address of the inverter.
	 * @param ethernet The Ethernet connection.
	 */
	public Inverter(String ip, Ethernet ethernet) {
		// Each inverters has his own connection but uses the same ethernet socket.
		// Sma connection constructor
		super(ethernet, ip);
	}

	/**
	 * Sends a logon request to the inverter which creates a connection.
	 * Use this before getting data from the inverter but after the main connection was created.
	 * NOTE: Only able to log on as a user now!
	 *
	 * @param password The password used to login to the inverter.
	 */
	public void logon(String password) throws LoginException, IOException {
		// The api needs the password in chararray form of a fixed size.
		// We convert it here so the user doesn't have to hassle with it and can just input a string.
		char[] passArray = new char[13];
		for (int ch = 0; ch < password.length(); ch++) {
			passArray[ch] = password.charAt(ch);
		}
		logon(SmaReader.UG_USER, passArray);
	}

	/**
	 * Sends a logon request to the inverter which creates a connection.
	 * Use this before getting data from the inverter but after the main connection was created.
	 *
	 * @param userGroup The usergroup: (USER or INSTALLER)
	 * @param password The password used to login to the inverter.
	 */
	private void logon(long userGroup, char[] password) throws LoginException, IOException {
		// First initialize the connection.
		initConnection();
		if (getPacket()) {
			Packet pckt = new Packet(ethernet.pcktBuf);
			susyId = pckt.source.susyid;
			serial = pckt.source.serial;
			// smaLogoff();
		} else {
			throw new LoginException("Connection to inverter failed! Is " + getIp() + " the correct IP?");
		}
		// Then login.
		smaLogin(userGroup, password);
		do {
			if (!getPacket()) {
				throw new LoginException("failed to login");
			}

			Packet pckt = new Packet(ethernet.pcktBuf);
			if (ethernet.pcktID == ((pckt.packetID) & 0x7FFF)) // Valid Packet ID
			{
				if (shortSwap(pckt.errorCode) == 0x0100) {
					smaLogoff();
					throw new WrongPasswordException("logon failed. Check '"
							+ (userGroup == SmaReader.UG_USER ? "USER" : "INSTALLER") + "' Password");
				} else {
					return;
				}
			} else {
				LOGGER.warn("Packet ID mismatch. Expected {}, received {}", ethernet.pcktID,
						(shortSwap(pckt.packetID) & 0x7FFF));
			}
		} while (true);
	}

	/**
	 * Requests data from the inverter which gets stored in it's data attribute.
	 * Uses data.(Name of the value you requested) to get the actual value this method requested.
	 *
	 * @param invDataType The type of data you want to retrieve from the inverter.
	 * @return Returns true if everything went ok.
	 */
	public boolean getInverterData(InverterDataType invDataType) throws IOException {
		LOGGER.trace("getInverterData({})", invDataType);

		requestInverterData(invDataType);

		do {
			if (!getPacket()) {
				return false;
			}

			short rcvpcktID = (short) (ethernet.pcktBuf[27] & 0x7FFF);
			if (ethernet.pcktID != rcvpcktID) {
				LOGGER.info("Packet ID mismatch. Expected {}, received {}", ethernet.pcktID, rcvpcktID);
				continue;
			}

			// Check if we received the package from the right inverter, not sure if
			// this works with multiple inverters.
			// We do this by checking if the susyd and serial is equal to this inverter object's susyd and serial.
			boolean rightOne = susyId == parseShort(ethernet.pcktBuf, 15) && serial == parseInt(ethernet.pcktBuf, 17);

			if (!rightOne) {
				LOGGER.info("We received data from the wrong inverter... Expected susyd: {}, received: {}", susyId,
						parseShort(ethernet.pcktBuf, 15));
				continue;
			}

			readData();
			return true;
		} while (true);
	}

	private void readData() {
		int recordsize = 0;
		int value = 0;
		for (int ix = 41; ix < ethernet.packetposition - 3; ix += recordsize) {
			int code = parseInt(ethernet.pcktBuf, ix);

			// Check this if something doesn't work, int to enum conversion. Should be good now
			LriDef lri = LriDef.intToEnum((code));

			char dataType = (char) (code >> 24);
			// Not sure if java uses same long date, well it doesn't
			// Multiply by 1000 cause java uses milliseconds and the inverter uses seconds since epoch.
			Date datetime = new Date(parseInt(ethernet.pcktBuf, ix + 4) * 1000L);

			if ((dataType != 0x10) && (dataType != 0x08)) // Not TEXT or STATUS, so it should be DWORD
			{
				// All data that needs an int value
				value = parseInt(ethernet.pcktBuf, ix + 8);
				if ((value == NAN_S32) || (value == NAN_U32)) {
					value = 0;
				}
			}
			// fix: We can't rely on dataType because it can be both 0x00 or 0x40 for DWORDs
			if ((lri == LriDef.SPOT_ETODAY) || (lri == LriDef.SPOT_ETOTAL) || (lri == LriDef.SPOT_FEEDTM)
					|| (lri == LriDef.SPOT_OPERTM)) // QWORD
			{
				// All data that needs a long value
				long value64 = parseLong(ethernet.pcktBuf, ix + 8);
				if ((value64 == NAN_S64) || (value64 == NAN_U64)) {
					value64 = 0;
				}

				recordsize = 16;
				values.put(lri, new TagValue(lri, value64, datetime.getTime()));
			} else if (lri == LriDef.NameplateLocation) {
				// INV_NAME
				recordsize = 40;
				StringBuilder sb = new StringBuilder(32);
				for (int i = 0; i < 32; i++) {
					char c = (char) ethernet.pcktBuf[ix + 8 + i];
					if (c != 0) {
						sb.append(c);
					}
				}
				deviceName = sb.toString();
			} else if (lri == LriDef.NameplatePkgRev) {
				// INV_SWVER
				recordsize = 40;
				char vtype = (char) ethernet.pcktBuf[ix + 24];
				String releaseType;
				if (vtype > 5) {
					releaseType = String.format("%c", vtype);
				} else {
					releaseType = String.format("%c",
							"NEABRS".charAt(vtype));// NOREV-EXPERIMENTAL-ALPHA-BETA-RELEASE-SPECIAL
				}
				short vbuild = (short) (ethernet.pcktBuf[ix + 25] & 0xFF);
				short vminor = (short) (ethernet.pcktBuf[ix + 26] & 0xFF);
				short vmajor = (short) (ethernet.pcktBuf[ix + 27] & 0xFF);
				// Vmajor and Vminor = 0x12 should be printed as '12' and not '18' (BCD)
				this.swVersion = String.format("%1d.%1d.%1d.%s", (vmajor >> 4) * 10 + (vmajor & 0x0F),
						(vminor >> 4) * 10 + (vminor & 0x0F), (int) vbuild, releaseType);
			} else if (lri == LriDef.INV_STATUS || lri == LriDef.OperationGriSwStt || lri == LriDef.NameplateMainModel
					|| lri == LriDef.NameplateModel)
			{
				// All cases which need the attribute value
				// INV_STATUS
				// INV_GRIDRELAY
				// INV_CLASS
				// INV_TYPE
				recordsize = 40;
				for (int idx = 8; idx < recordsize; idx += 4) {
					int attribute = parseInt(ethernet.pcktBuf, ix + idx) & 0x00FFFFFF;
					char attValue = (char) ethernet.pcktBuf[ix + idx + 3];
					if (attribute == 0xFFFFFE) {
						break; // End of attributes
					}
					if (attValue == 1) {
						setInverterDataAttribute(lri, attribute);
					}
				}
			} else if (lri == null) {
				if (recordsize == 0) {
					recordsize = 12;
				}
			} else {
				recordsize = 28;
				values.put(lri, new TagValue(lri, value, datetime.getTime()));
			}
		}
	}

	private void setInverterDataAttribute(LriDef lri, int attribute) {
		switch (lri) {
		case NameplateMainModel: // INV_CLASS
			this.deviceClass = InvDeviceClass.intToEnum(attribute);
			String devclass = TagDescriptions.getInstance().getDescription(attribute);
			if (!devclass.isEmpty()) {
				this.deviceClassName = devclass;
			} else {
				this.deviceClassName = "UNKNOWN CLASS";
			}
			break;

		case NameplateModel: // INV_TYPE
			String devtype = TagDescriptions.getInstance().getDescription(attribute);
			if (!devtype.isEmpty()) {
				this.deviceType = devtype;
			} else {
				this.deviceType = "UNKNOWN TYPE";
			}
			break;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> TagValue<T> getValue(LriDef lriDef) {
		return values.get(lriDef);
	}

	public short getSusyId() {
		return susyId;
	}

	public long getSerial() {
		return serial;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public String getSwVersion() {
		return swVersion;
	}

	public InvDeviceClass getDeviceClass() {
		return deviceClass;
	}

	public String getDeviceClassName() {
		return deviceClassName;
	}

	@Override
	public String toString() {
		return "Inverter(" + getIp() + ")";
	}

	public String getDeviceType() {
		return deviceType;
	}
}
