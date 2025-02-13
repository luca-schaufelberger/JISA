package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;

public class AgilentB1500A extends Agilent415XX {

    public static String getDescription() {
        return "Agilent B1500A Series SPA";
    }

    public AgilentB1500A(Address address) throws IOException, DeviceException {

        super(address, false);

        try {
            if (!getIDN().split(",")[1].contains("B1500A")) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new DeviceException("Device at %s is not an Agilent B1500A.", address.toString());
        }

    }

    protected double measureVoltage(int channel) throws DeviceException, IOException {

        checkChannel(channel);

        if (isOn(channel)) {
            return queryDouble("TV %d,%d", channel + 1, voltageRange[channel].toInt());
        } else {
            return 0.0;
        }

    }

    protected double measureCurrent(int channel) throws DeviceException, IOException {

        checkChannel(channel);

        if (isOn(channel)) {
            return queryDouble("TI %d,%d", channel + 1, currentRange[channel].toInt());
        } else {
            return 0.0;
        }

    }

    @Override
    protected AgilentRange rangeFromVoltage(int channel, double voltage) {
        return VoltRange.fromVoltage(UnitType.SMU, voltage);
    }

    @Override
    protected AgilentRange rangeFromCurrent(int channel, double current) {
        return CurrRange.fromCurrent(UnitType.SMU, current);
    }

    @Override
    public boolean isLineFilterEnabled(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return false;
    }

    @Override
    public void setLineFilterEnabled(int channel, boolean enabled) throws DeviceException, IOException {
        checkChannel(channel);
    }

    protected enum VoltRange implements AgilentRange {

        // Ranges for normal SMU units
        SMU_2_V(UnitType.SMU, 11, 2.0, 100e-3),
        SMU_20_V(UnitType.SMU, 12, 20.0, 100e-3),
        SMU_40_V(UnitType.SMU, 13, 40.0, 50e-3),
        SMU_100_V(UnitType.SMU, 14, 100.0, 20e-3),

        // Ranges for High-Power SMU units
        HPSMU_2_V(UnitType.HPSMU, 11, 20.0, 1.0),
        HPSMU_20_V(UnitType.HPSMU, 12, 20.0, 1.0),
        HPSMU_40_V(UnitType.HPSMU, 13, 40.0, 500e-3),
        HPSMU_100_V(UnitType.HPSMU, 14, 100.0, 125e-3),
        HPSMU_200_V(UnitType.HPSMU, 15, 200.0, 50e-3);

        private final UnitType type;
        private final int      code;
        private final double   range;
        private final double   iComp;

        VoltRange(UnitType type, int code, double range, double iComp) {
            this.type  = type;
            this.code  = code;
            this.range = range;
            this.iComp = iComp;
        }

        public static AgilentRange fromVoltage(UnitType type, double value) {

            for (VoltRange range : values()) {

                if (range.getRange() >= Math.abs(value) && range.type == type) {
                    return range;
                }

            }

            return AUTO_RANGING;

        }

        public int toInt() {
            return code;
        }

        public double getRange() {
            return range;
        }

        public double getCompliance() { return iComp; }

    }

    protected enum CurrRange implements AgilentRange {

        // Ranges for normal SMU units
        SMU_1_pA(UnitType.SMU, 9, 1e-12, 100),
        SMU_10_pA(UnitType.SMU, 9, 10e-12, 100),
        SMU_100_pA(UnitType.SMU, 10, 100e-12, 100),
        SMU_1_nA(UnitType.SMU, 11, 1e-9, 100),
        SMU_10_nA(UnitType.SMU, 12, 10e-9, 100),
        SMU_100_nA(UnitType.SMU, 13, 100e-9, 100),
        SMU_1_uA(UnitType.SMU, 14, 1e-6, 100),
        SMU_10_uA(UnitType.SMU, 15, 10e-6, 100),
        SMU_100_uA(UnitType.SMU, 16, 100e-6, 100),
        SMU_1_mA(UnitType.SMU, 17, 1e-3, 100),
        SMU_10_mA(UnitType.SMU, 18, 10e-3, 100),
        SMU_100_mA(UnitType.SMU, 19, 100e-3, 20),

        // Ranges for High-Power SMU units
        HPSMU_1_nA(UnitType.HPSMU, 11, 1e-9, 200),
        HPSMU_10_nA(UnitType.HPSMU, 12, 10e-9, 200),
        HPSMU_100_nA(UnitType.HPSMU, 13, 100e-9, 200),
        HPSMU_1_uA(UnitType.HPSMU, 14, 1e-6, 200),
        HPSMU_10_uA(UnitType.HPSMU, 15, 10e-6, 200),
        HPSMU_100_uA(UnitType.HPSMU, 16, 100e-6, 200),
        HPSMU_1_mA(UnitType.HPSMU, 17, 1e-3, 200),
        HPSMU_10_mA(UnitType.HPSMU, 18, 10e-3, 200),
        HPSMU_100_mA(UnitType.HPSMU, 19, 100e-3, 100),
        HPSMU_1_A(UnitType.HPSMU, 20, 1.0, 20);

        private final UnitType type;
        private final int      code;
        private final double   range;
        private final double   vComp;

        CurrRange(UnitType type, int code, double range, double vComp) {
            this.type  = type;
            this.code  = code;
            this.range = range;
            this.vComp = vComp;
        }

        public static AgilentRange fromCurrent(UnitType type, double value) {

            for (CurrRange range : values()) {

                if (range.getRange() >= Math.abs(value) && range.type == type) {
                    return range;
                }

            }

            return AUTO_RANGING;

        }

        public int toInt() {
            return code;
        }

        public double getRange() {
            return range;
        }

        public double getCompliance() { return vComp; }

    }


}
