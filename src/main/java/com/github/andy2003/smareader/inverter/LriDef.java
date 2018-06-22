package com.github.andy2003.smareader.inverter;

import java.util.function.Function;

public enum LriDef {
	INV_STATUS(0x00214800),
	CoolsysTmpNom(0x00237700, -2, "°C", "Operating condition temperatures"),
	SPOT_PDC1(0x00251E01), // *40* DC power input (aka SPOT_PDC1)
	SPOT_PDC2(0x00251E02), // *40* DC power input (aka SPOT_PDC2)
	SPOT_ETOTAL(0x00260100, 0, "Wh", "Total yield"), // *00* Total yield (aka SPOT_ETOTAL)
	SPOT_ETODAY(0x00262200, 0, "Wh", "Day yield"), // *00* Day yield (aka SPOT_ETODAY)
	SPOT_PACTOT(0x00263F00, -3, "W", "Power total"), // *40* (aka SPOT_PACTOT)
	BatChaStt(0x00295A00, 0, "%", "Current battery charge status"),
	INV_PACMAX1(0x00411E00, 0, "W", "Nominal power in OK Mode"), // *00* (aka INV_PACMAX1)
	INV_PACMAX2(0x00411F00, 0, "W", "Nominal power in Warning Mode"), // *00* (aka INV_PACMAX2)
	INV_PACMAX3(0x00412000, 0, "W", "Nominal power in Fault Mode"), // *00* (aka INV_PACMAX3)
	OperationGriSwStt(0x00416400), // *08* Grid relay/contactor (aka INV_GRIDRELAY)
	OperationRmgTms(0x00416600), // *00* Waiting time until feed-in
	SPOT_UDC1(0x00451F01), // *40* DC voltage input (aka SPOT_UDC1)
	SPOT_UDC2(0x00451F02), // *40* DC voltage input (aka SPOT_UDC2)
	SPOT_IDC1(0x00452101), // *40* DC current input (aka SPOT_IDC1)
	SPOT_IDC2(0x00452102), // *40* DC current input (aka SPOT_IDC2)
	MeteringPvMsTotWhOut(0x00462300), // *00* PV generation counter reading
	MeteringGridMsTotWhOut(0x00462400), // *00* Grid feed-in counter reading
	MeteringGridMsTotWhIn(0x00462500), // *00* Grid reference counter reading
	MeteringCsmpTotWhIn(0x00462600), // *00* Meter reading consumption meter
	MeteringGridMsDyWhOut(0x00462700), // *00* ?
	MeteringGridMsDyWhIn(0x00462800), // *00* ?
	SPOT_OPERTM(0x00462E00, 0, "ms", "Operating time"),
	SPOT_FEEDTM(0x00462F00, 0, "ms", "Feed-in time"),
	MeteringGriFailTms(0x00463100), // *00* Power outage
	MeteringWhIn(0x00463A00), // *00* Absorbed energy
	MeteringWhOut(0x00463B00), // *00* Released energy
	MeteringPvMsTotWOut(0x00463500), // *40* PV power generated
	MeteringGridMsTotWOut(0x00463600), // *40* Power grid feed-in
	MeteringGridMsTotWIn(0x00463700), // *40* Power grid reference
	MeteringCsmpTotWIn(0x00463900), // *40* Consumer power
	SPOT_PAC1(0x00464000, 0, "W", "Power L1"),
	SPOT_PAC2(0x00464100, 0, "W", "Power L2"),
	SPOT_PAC3(0x00464200, 0, "W", "Power L3"),
	SPOT_UAC1(0x00464800, -2, "V", "Grid voltage phase L1"),
	SPOT_UAC2(0x00464900, -2, "V", "Grid voltage phase L2"),
	SPOT_UAC3(0x00464A00, -2, "V", "Grid voltage phase L3"),
	SPOT_IAC1_1(0x00465000, -3, "A", "Grid current phase L1"),
	SPOT_IAC2_1(0x00465100, -3, "A", "Grid current phase L2"),
	SPOT_IAC3_1(0x00465200, -3, "A", "Grid current phase L3"),
	SPOT_IAC1(0x00465300, -3, "A", "Grid current phase L1"),
	SPOT_IAC2(0x00465400, -3, "A", "Grid current phase L2"),
	SPOT_IAC3(0x00465500, -3, "A", "Grid current phase L3"),
	SPOT_FREQ(0x00465700, -2, "HZ", "Grid frequency"), // *00* (aka SPOT_FREQ)
	MeteringSelfCsmpSelfCsmpWh(0x0046AA00), // *00* Energy consumed internally
	MeteringSelfCsmpActlSelfCsmp(0x0046AB00), // *00* Current self-consumption
	MeteringSelfCsmpSelfCsmpInc(0x0046AC00), // *00* Current rise in self-consumption
	MeteringSelfCsmpAbsSelfCsmpInc(0x0046AD00), // *00* Rise in self-consumption
	MeteringSelfCsmpDySelfCsmpInc(0x0046AE00), // *00* Rise in self-consumption today
	BatDiagCapacThrpCnt(0x00491E00, 0, "", "Number of battery charge throughputs"),
	BatDiagTotAhIn(0x00492600), // *00* Amp hours counter for battery charge
	BatDiagTotAhOut(0x00492700), // *00* Amp hours counter for battery discharge
	BatTmpVal(0x00495B00, -1, "°C", "Battery temperature"),
	BatVol(0x00495C00, -2, "V", "Battery voltage"),
	BatAmp(0x00495D00, -3, "A", "Battery current"),
	NameplateLocation(0x00821E00), // *10* Device name (aka INV_NAME)
	NameplateMainModel(0x00821F00), // *08* Device class (aka INV_CLASS)
	NameplateModel(0x00822000), // *08* Device type (aka INV_TYPE)
	NameplateAvalGrpUsr(0x00822100), // * * Unknown
	NameplatePkgRev(0x00823400), // *08* Software package (aka INV_SWVER)
	InverterWLim(0x00832A00), // *00* Maximum active power device (aka INV_PACMAX1_2) (Some inverters like
	// SB3300/SB1200)
	GridMsPhVphsA2B6100(0x00464B00),
	GridMsPhVphsB2C6100(0x00464C00),
	GridMsPhVphsC2A6100(0x00464D00);

	private int value;
	private String unit;
	private String description;
	private Function<Number, ?> conversion;

	LriDef(int value) {
		this.value = value;
	}

	LriDef(int value, int pot, String unit, String description) {
		this.value = value;
		this.unit = unit;
		this.description = description;
		double factor = Math.pow(10, pot);
		this.conversion = num -> num.longValue() * factor;
	}

	public static LriDef intToEnum(int code) {
		LriDef e = findEnum(code & 0x00FFFFFF);
		if (e == null) {
			e = findEnum((code & 0x00FFFF00));
		}
		return e;
	}

	private static LriDef findEnum(int value) {
		for (LriDef enumValue : values()) {
			if (enumValue.value == value) {
				return enumValue;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T convert(Number value) {
		if (conversion != null) {
			return (T) conversion.apply(value);
		}
		return (T) value;
	}

	public String getUnit() {
		return unit;
	}

	public String getDescription() {
		return description;
	}
}
