package JISA.Devices;

import JISA.Addresses.Address;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class K2400 extends SMU {

    private static final String C_MEASURE_VOLTAGE       = ":MEAS:VOLT?";
    private static final String C_MEASURE_CURRENT       = ":MEAS:CURR?";
    private static final String C_SET_AVG_COUNT         = "AVER:COUNT %d";
    private static final String C_QUERY_AVG_COUNT       = "VOLT:AVER:COUNT?";
    private static final String C_SET_AVG_MODE          = "AVER:TCON %s";
    private static final String C_QUERY_AVG_MODE        = "VOLT:AVER:TCON?";
    private static final String C_SET_AVG_STATE         = "AVER %s";
    private static final String C_QUERY_AVG_STATE       = "VOLT:AVER?";
    private static final String OUTPUT_ON               = "1";
    private static final String OUTPUT_OFF              = "0";
    private static final String C_SET_SRC_RANGE         = ":SOUR:%s:RANG %f";
    private static final String C_QUERY_SRC_RANGE       = ":SOUR:%s:RANG?";
    private static final String C_SET_SRC_AUTO_RANGE    = ":SOUR:%s:RANG:AUTO %s";
    private static final String C_QUERY_SRC_AUTO_RANGE  = ":SOUR:%s:RANG:AUTO?";
    private static final String C_SET_MEAS_RANGE        = ":SENS:%s:RANG %f";
    private static final String C_QUERY_MEAS_RANGE      = ":SENS:%s:RANG?";
    private static final String C_SET_MEAS_AUTO_RANGE   = ":SENS:%s:RANG:AUTO %s";
    private static final String C_QUERY_MEAS_AUTO_RANGE = ":SENS:%s:RANG:AUTO?";
    private static final String C_SET_LIMIT             = ":SOUR:%s:%sLIM %f";
    private static final String C_QUERY_LIMIT           = ":SOUR:%s:%sLIM?";
    private final        Model  MODEL;

    private enum Model {
        K2400,
        K2410,
        K2420,
        K2425,
        K2430,
        K2440
    }

    // == FILTERS ======================================================================================================
    private final MedianRepeatFilter MEDIAN_REPEAT_V = new MedianRepeatFilter(
            () -> queryDouble(C_MEASURE_VOLTAGE),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            }
    );

    private final MedianRepeatFilter MEDIAN_REPEAT_I = new MedianRepeatFilter(
            () -> queryDouble(C_MEASURE_CURRENT),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            });

    private final MedianMovingFilter MEDIAN_MOVING_V = new MedianMovingFilter(
            () -> queryDouble(C_MEASURE_VOLTAGE),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            }
    );

    private final MedianMovingFilter MEDIAN_MOVING_I = new MedianMovingFilter(
            () -> queryDouble(C_MEASURE_CURRENT),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            });

    private final BypassFilter MEAN_REPEAT_V = new BypassFilter(
            () -> queryDouble(C_MEASURE_VOLTAGE),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, c);
                write(C_SET_AVG_STATE, OUTPUT_ON);
            }
    );

    private final BypassFilter MEAN_REPEAT_I = new BypassFilter(
            () -> queryDouble(C_MEASURE_CURRENT),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, c);
                write(C_SET_AVG_STATE, OUTPUT_ON);
            }
    );

    private final BypassFilter MEAN_MOVING_V = new BypassFilter(
            () -> queryDouble(C_MEASURE_VOLTAGE),
            (c) -> {
                write(C_SET_AVG_MODE, "MOVING");
                write(C_SET_AVG_COUNT, c);
                write(C_SET_AVG_STATE, OUTPUT_ON);
            }
    );

    private final BypassFilter MEAN_MOVING_I = new BypassFilter(
            () -> queryDouble(C_MEASURE_CURRENT),
            (c) -> {
                write(C_SET_AVG_MODE, "MOVING");
                write(C_SET_AVG_COUNT, c);
                write(C_SET_AVG_STATE, OUTPUT_ON);
            }
    );

    private final BypassFilter NONE_V = new BypassFilter(
            () -> queryDouble(C_MEASURE_VOLTAGE),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            }
    );

    private final BypassFilter NONE_I = new BypassFilter(
            () -> queryDouble(C_MEASURE_CURRENT),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            }
    );

    // == INTERNAL VARIABLES ===========================================================================================
    private ReadFilter filterV     = NONE_V;
    private ReadFilter filterI     = NONE_I;
    private AMode      filterMode  = AMode.NONE;
    private int        filterCount = 1;

    public K2400(Address address) throws IOException, DeviceException {

        super(address);

        String  idn     = getIDN();
        Matcher matcher = Pattern.compile("MODEL (2400|2410|2420|2425|2430|2440)").matcher(idn.toUpperCase());

        if (!matcher.find()) {
            throw new DeviceException(
                    "The device at address \"%s\" is not a Keithley 2400 series SMU." +
                            "\nThe K2400 driver only works with models 2400, 2410, 2420, 2425, 2430, 2440." +
                            "\nFor model 2450, please use the K2450 driver.",
                    address.toString()
            );
        } else {
            MODEL = Model.valueOf("K" + matcher.group(1).trim());
        }

        setAverageMode(AMode.NONE);
    }

    @Override
    public double getVoltage() throws DeviceException, IOException {
        return filterV.getValue();
    }

    @Override
    public double getCurrent() throws DeviceException, IOException {
        return filterI.getValue();
    }

    @Override
    public void setVoltage(double voltage) throws DeviceException, IOException {
        write(":SOUR:VOLT %e", voltage);
        write(":SOUR:FUNC VOLT");
    }

    @Override
    public void setCurrent(double current) throws DeviceException, IOException {
        write(":SOUR:CURR %e", current);
        write(":SOUR:FUNC CURR");
    }

    @Override
    public void turnOn() throws IOException {
        write(":OUTP:STATE ON");
    }

    @Override
    public void turnOff() throws IOException {
        write(":OUTP:STATE OFF");
    }

    @Override
    public boolean isOn() throws IOException {
        return query(":OUTP:STATE?").equals(OUTPUT_ON);
    }

    @Override
    public void setSource(Source source) throws IOException {

        switch (source) {

            case VOLTAGE:
                write(":SOUR:FUNC VOLT");
                break;

            case CURRENT:
                write(":SOUR:FUNC CURR");
                break;

        }

    }

    @Override
    public Source getSource() throws IOException {

        String response = query(":SOUR:FUNC?").trim();

        if (response.equals("VOLT")) {
            return Source.VOLTAGE;
        } else if (response.equals("CURR")) {
            return Source.CURRENT;
        } else {
            throw new IOException("Invalid response from Keithley 2400");
        }

    }

    @Override
    public void setBias(double level) throws DeviceException, IOException {

        switch (getSource()) {

            case VOLTAGE:
                setVoltage(level);
                break;

            case CURRENT:
                setCurrent(level);
                break;

        }

    }

    @Override
    public double getSourceValue() throws DeviceException, IOException {

        switch (getSource()) {

            case VOLTAGE:
                return getVoltage();

            case CURRENT:
                return getCurrent();

            default:
                return getVoltage();

        }

    }

    @Override
    public double getMeasureValue() throws DeviceException, IOException {

        switch (getSource()) {

            case VOLTAGE:
                return getCurrent();

            case CURRENT:
                return getVoltage();

            default:
                return getCurrent();

        }

    }

    @Override
    public void useFourProbe(boolean fourProbes) throws IOException {
        write(":SENS:RSEN %d", fourProbes ? 1 : 0);
    }

    @Override
    public boolean isUsingFourProbe() throws IOException {
        return query(":SENS:RSEN?").trim().equals("1");
    }


    @Override
    public void setAverageMode(AMode mode) throws IOException, DeviceException {

        switch (mode) {

            case NONE:
                filterV = NONE_V;
                filterI = NONE_I;
                break;

            case MEAN_REPEAT:
                filterV = MEAN_REPEAT_V;
                filterI = MEAN_REPEAT_I;
                break;

            case MEAN_MOVING:
                filterV = MEAN_MOVING_V;
                filterI = MEAN_MOVING_I;
                break;

            case MEDIAN_REPEAT:
                filterV = MEDIAN_REPEAT_V;
                filterI = MEDIAN_REPEAT_I;
                break;

            case MEDIAN_MOVING:
                filterV = MEDIAN_MOVING_V;
                filterI = MEDIAN_MOVING_I;
                break;

        }

        filterMode = mode;
        resetFilters();

    }

    private void resetFilters() throws IOException, DeviceException {

        filterV.setCount(filterCount);
        filterI.setCount(filterCount);

        filterV.setUp();
        filterI.setUp();

        filterV.clear();
        filterI.clear();
    }

    @Override
    public void setAverageCount(int count) throws IOException, DeviceException {
        filterCount = count;
        resetFilters();
    }

    @Override
    public AMode getAverageMode() {
        return filterMode;

    }

    @Override
    public int getAverageCount() {
        return filterCount;
    }


    public Source getMeasureMode() throws IOException {

        switch (getSource()) {
            case VOLTAGE:
                return Source.CURRENT;

            case CURRENT:
                return Source.VOLTAGE;
        }

        return Source.CURRENT;

    }

    @Override
    public void setSourceRange(double value) throws IOException {

        switch (getSource()) {

            case VOLTAGE:
                setVoltageRange(value);
                break;

            case CURRENT:
                setCurrentRange(value);
        }

    }

    @Override
    public double getSourceRange() throws IOException {

        switch (getSource()) {

            case VOLTAGE:
                return getVoltageRange();

            case CURRENT:
                return getCurrentRange();

            default:
                return getVoltageRange();

        }

    }

    @Override
    public void useAutoSourceRange() throws IOException {

        switch (getSource()) {

            case VOLTAGE:
                useAutoVoltageRange();
                break;

            case CURRENT:
                useAutoSourceRange();
                break;

        }

    }

    @Override
    public boolean isSourceRangeAuto() throws IOException {

        switch (getSource()) {

            case VOLTAGE:
                return isVoltageRangeAuto();

            case CURRENT:
                return isCurrentRangeAuto();

            default:
                return isVoltageRangeAuto();

        }

    }

    @Override
    public void setMeasureRange(double value) throws IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                setVoltageRange(value);
                break;

            case CURRENT:
                setCurrentRange(value);
        }

    }

    @Override
    public double getMeasureRange() throws IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                return getVoltageRange();

            case CURRENT:
                return getCurrentRange();

            default:
                return getCurrentRange();

        }

    }

    @Override
    public void useAutoMeasureRange() throws IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                useAutoVoltageRange();
                break;

            case CURRENT:
                useAutoSourceRange();
                break;

        }

    }

    @Override
    public boolean isMeasureRangeAuto() throws IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                return isVoltageRangeAuto();

            case CURRENT:
                return isCurrentRangeAuto();

            default:
                return isCurrentRangeAuto();

        }

    }

    @Override
    public void setVoltageRange(double value) throws IOException {

    }

    @Override
    public double getVoltageRange() throws IOException {
        return 0;
    }

    @Override
    public void useAutoVoltageRange() throws IOException {

    }

    @Override
    public boolean isVoltageRangeAuto() throws IOException {
        return false;
    }

    @Override
    public void setCurrentRange(double value) throws IOException {

    }

    @Override
    public double getCurrentRange() throws IOException {
        return 0;
    }

    @Override
    public void useAutoCurrentRange() throws IOException {

    }

    @Override
    public boolean isCurrentRangeAuto() throws IOException {
        return false;
    }

    @Override
    public void setOutputLimit(double value) throws IOException {

    }

    @Override
    public double getOutputLimit() throws IOException {
        return 0;
    }

    @Override
    public void setVoltageLimit(double voltage) throws DeviceException, IOException {

    }

    @Override
    public double getVoltageLimit() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setCurrentLimit(double current) throws DeviceException, IOException {

    }

    @Override
    public double getCurrentLimit() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setIntegrationTime(double time) throws DeviceException, IOException {

    }

    @Override
    public double getIntegrationTime() throws DeviceException, IOException {
        return 0;
    }

    public TType getTerminalType(Terminals terminals) {
        return TType.BANANA;
    }

    @Override
    public void setTerminals(Terminals terminals) throws IOException {

        switch (terminals) {

            case FRONT:
                write(":ROUT:TERM FRONT");
                break;

            case REAR:
                write(":ROUT:TERM REAR");
                break;

        }

    }

    @Override
    public Terminals getTerminals() throws IOException {

        String response = query(":ROUT:TERM?");

        if (response.contains("FRON")) {
            return Terminals.FRONT;
        } else if (response.contains("REAR")) {
            return Terminals.REAR;
        } else {
            throw new IOException("Invalid response from Keithley 2400");
        }

    }

    @Override
    public void setOffMode(OffMode mode) throws DeviceException, IOException {

    }

    @Override
    public OffMode getOffMode() throws DeviceException, IOException {
        return null;
    }

}
