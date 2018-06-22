package com.github.andy2003.smareader;

import java.io.IOException;
import java.util.Collection;

import com.github.andy2003.smareader.inverter.Inverter;
import com.github.andy2003.smareader.inverter.InverterDataType;
import com.github.andy2003.smareader.inverter.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmaReaderTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(SmaReaderTest.class);

	public static void main(String[] args) throws IOException {
		try (SmaReader smaReader = new SmaReader()) {
			Collection<Inverter> inverter = smaReader.detectDevices();
			for (Inverter i : inverter) {
				try {
					i.logon("0000");
					if (i.getInverterData(InverterDataType.SoftwareVersion)
							&& i.getInverterData(InverterDataType.TypeLabel))
					{
						LOGGER.info("Name: {} {} {}: Version {}",
								i.getDeviceName(),
								i.getDeviceType(),
								i.getDeviceClass(),
								i.getSwVersion());
					}
					i.close();
				} catch (LoginException e) {
					LOGGER.warn("failed to login: {}", e.getMessage());
				}
			}
		}
	}
}
