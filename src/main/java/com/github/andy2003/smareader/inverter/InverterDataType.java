package com.github.andy2003.smareader.inverter;

public enum InverterDataType {
	/**
	 * SPOT_ETODAY, SPOT_ETOTAL
	 */
	EnergyProduction(0x54000200, 0x00260100, 0x002622FF),
	/**
	 * SPOT_PDC1, SPOT_PDC2
	 */
	SpotDCPower(0x53800200, 0x00251E00, 0x00251EFF),
	/**
	 * SPOT_UDC1, SPOT_UDC2, SPOT_IDC1, SPOT_IDC2
	 */
	SpotDCVoltage(0x53800200, 0x00451F00, 0x004521FF),
	/**
	 * SPOT_PAC1, SPOT_PAC2, SPOT_PAC3
	 */
	SpotACPower(0x51000200, 0x00464000, 0x004642FF),
	/**
	 * SPOT_UAC1, SPOT_UAC2, SPOT_UAC3, SPOT_IAC1, SPOT_IAC2, SPOT_IAC3
	 */
	SpotACVoltage(0x51000200, 0x00464800, 0x004655FF),
	/**
	 * SPOT_FREQ
	 */
	SpotGridFrequency(0x51000200, 0x00465700, 0x004657FF),
	/**
	 * INV_PACMAX1, INV_PACMAX2, INV_PACMAX3
	 */
	MaxACPower(0x51000200, 0x00411E00, 0x004120FF),
	/**
	 * INV_PACMAX1_2
	 */
	MaxACPower2(0x51000200, 0x00832A00, 0x00832AFF),
	/**
	 * SPOT_PACTOT
	 */
	SpotACTotalPower(0x51000200, 0x00263F00, 0x00263FFF),
	/**
	 * INV_NAME, INV_TYPE, INV_CLASS
	 */
	TypeLabel(0x58000200, 0x00821E00, 0x008220FF),
	/**
	 * SPOT_OPERTM, SPOT_FEEDTM
	 */
	OperationTime(0x54000200, 0x00462E00, 0x00462FFF),
	/**
	 * INV_SWVERSION
	 */
	SoftwareVersion(0x58000200, 0x00823400, 0x008234FF),
	/**
	 * INV_STATUS
	 */
	DeviceStatus(0x51800200, 0x00214800, 0x002148FF),
	/**
	 * INV_GRIDRELAY
	 */
	GridRelayStatus(0x51800200, 0x00416400, 0x004164FF),

	BatteryChargeStatus(0x51000200, 0x00295A00, 0x00295AFF),
	BatteryInfo(0x51000200, 0x00491E00, 0x00495DFF),
	InverterTemperature(0x52000200, 0x00237700, 0x002377FF);

	/**
	 * Value used for requesting this data type from the inverter.
	 */
	public final long command;
	/**
	 * Value used for requesting this data type from the inverter.
	 */
	public final long first;
	/**
	 * Value used for requesting this data type from the inverter.
	 */
	public final long last;

	InverterDataType(long command, long first, long last) {
		this.command = command;
		this.first = first;
		this.last = last;
	}
}
