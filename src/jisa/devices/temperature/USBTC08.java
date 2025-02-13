package jisa.devices.temperature;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.ShortByReference;
import jisa.addresses.Address;
import jisa.addresses.IDAddress;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.MSTMeter;
import jisa.enums.Thermocouple;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Driver class for Picotech USB-TC08 thermocouple data loggers. Requires proprietary usbtc08 library to be installed.
 */
public class USBTC08 extends NativeDevice<USBTC08.NativeInterface> implements MSTMeter {

    public static String getDescription() {
        return "PicoTech USB-TC08";
    }

    // Constants
    private static final String                         LIBRARY_NAME                 = "usbtc08";
    private static final Class<USBTC08.NativeInterface> LIBRARY_CLASS                = USBTC08.NativeInterface.class;
    private static final int                            SENSORS_PER_UNIT             = 9;
    private static final short                          ACTION_FAILED                = 0;
    private static final short                          ERROR_NONE                   = 0;
    private static final short                          ERROR_OS_NOT_SUPPORTED       = 1;
    private static final short                          ERROR_NO_CHANNELS_SET        = 2;
    private static final short                          ERROR_INVALID_PARAMETER      = 3;
    private static final short                          ERROR_VARIANT_NOT_SUPPORTED  = 4;
    private static final short                          ERROR_INCORRECT_MODE         = 5;
    private static final short                          ERROR_ENUMERATION_INCOMPLETE = 6;
    private static final short                          UNITS_KELVIN                 = 2;

    /** Static instance of loaded library */
    private static USBTC08.NativeInterface INSTANCE;

    static {

        try {
            INSTANCE = Native.loadLibrary(LIBRARY_NAME, LIBRARY_CLASS);
        } catch (Throwable e) {
            INSTANCE = null;
        }

    }

    private final short          handle;
    private final Thermocouple[] types = {
            Thermocouple.NONE,
            Thermocouple.NONE,
            Thermocouple.NONE,
            Thermocouple.NONE,
            Thermocouple.NONE,
            Thermocouple.NONE,
            Thermocouple.NONE,
            Thermocouple.NONE,
            Thermocouple.NONE
    };

    private float[]   lastValues    = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    private long      lastTime      = 0;
    private Frequency lineFrequency = Frequency.FIFTY_HERTZ;

    /**
     * Connects to the first USB-TC08 unit found connected to the system.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    public USBTC08() throws IOException, DeviceException {

        // Load native library
        super(LIBRARY_NAME, INSTANCE);

        if (INSTANCE == null) {
            throw new IOException("Error loading usbtc08 library!");
        }

        short handle = lib.usb_tc08_open_unit();

        if (handle > 0) {
            this.handle = handle;
        } else if (handle == ACTION_FAILED) {
            throw new IOException("No USB TC-08 unit found!");
        } else {
            throw new DeviceException(getLastError((short) 0));
        }

    }

    /**
     * Connects to the first USB-TC08 unit with matching serial number, given in the form of an IDAddress object.
     *
     * @param address Serial number, as IDAddress object
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    public USBTC08(Address address) throws IOException, DeviceException {

        // Load native library
        super(LIBRARY_NAME, INSTANCE);

        if (address.toIDAddress() == null) {
            throw new DeviceException("This driver requires a serial number address.");
        }

        String serial = address.toIDAddress().getID();

        if (INSTANCE == null) {
            throw new IOException("Error loading usbtc08 library!");
        }

        // Search for all connected units
        List<USBTC08> found = find();

        if (found.isEmpty()) {
            throw new IOException("No USB TC-08 unit found!");
        }

        Short value = null;

        for (USBTC08 unit : found) {

            // If it's the one we want, give this instance the handle, otherwise close the connection
            if (unit.getSerial().toLowerCase().equals(serial.toLowerCase().trim())) {
                value = unit.handle;
            } else {
                unit.close();
            }

        }

        // If nothing was found, then the serial number is wrong
        if (value == null) {
            throw new IOException(String.format("No USB TC-08 unit with serial number \"%s\" was found.", serial));
        }

        handle = value;

    }

    /**
     * Connects to the first USB-TC08 unit with matching serial number, given as a String.
     *
     * @param serial Serial number, as a String
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    public USBTC08(String serial) throws IOException, DeviceException {
        this(new IDAddress(serial));
    }

    /**
     * Returns a list of all USB-TC08 units found connected to this computer.
     *
     * @return List of USBTC08 objects representing the found units
     */
    public static List<USBTC08> find() {

        List<USBTC08> devices = new LinkedList<>();

        while (true) {

            try {
                devices.add(new USBTC08());
            } catch (Throwable e) {
                break;
            }

        }

        return devices;

    }

    /**
     * Returns the serial number of this unit.
     *
     * @return Serial number
     *
     * @throws DeviceException Upon instrument error.
     */
    public String getSerial() throws DeviceException {

        byte[] read   = new byte[256];
        short  result = lib.usb_tc08_get_unit_info2(handle, read, (short) 256, NativeInterface.USBTC08LINE_BATCH_AND_SERIAL);

        if (result == ACTION_FAILED) {
            throw new DeviceException(getLastError(handle));
        }

        return new String(read).trim();

    }

    @Override
    public double getTemperature(int sensor) throws DeviceException, IOException {

        checkSensor(sensor);

        if ((System.currentTimeMillis() - lastTime) > lib.usb_tc08_get_minimum_interval_ms(handle)) {
            updateReadings();
        }

        return lastValues[sensor];

    }

    /**
     * Updates the currently held temperature readings for each sensor. This should be updated at most every minimum
     * measurement interval, as calculated by the USB-TC08 unit.
     *
     * @throws DeviceException Upon instrument error
     */
    private synchronized void updateReadings() throws DeviceException {

        // Need a pointer to some memory to store our returned values
        Memory tempPointer = new Memory(9 * Native.getNativeSize(Float.TYPE));

        int result = lib.usb_tc08_get_single(handle, tempPointer, new ShortByReference((short) 0), UNITS_KELVIN);

        // If zero, then something's gone wrong.
        if (result == ACTION_FAILED) {
            throw new DeviceException(getLastError(handle));
        }

        lastValues = tempPointer.getFloatArray(0, SENSORS_PER_UNIT);
        lastTime   = System.currentTimeMillis();

    }

    @Override
    public int getNumSensors() {
        return SENSORS_PER_UNIT;
    }

    @Override
    public String getSensorName(int sensorNumber) {
        return String.format("Channel %d", sensorNumber);
    }

    @Override
    public List<Double> getTemperatures() throws DeviceException {

        List<Double> temperatures = new ArrayList<>(lastValues.length);

        updateReadings();

        // Convert to list of doubles
        for (float value : lastValues) {
            temperatures.add((double) value);
        }

        return temperatures;

    }

    @Override
    public void setTemperatureRange(int sensor, double range) throws DeviceException, IOException {
        checkSensor(sensor);
    }

    @Override
    public double getTemperatureRange(int sensor) {
        return 999.999;
    }

    /**
     * Configures the sensor on the TC-08, specifying which type of thermocouple is installed.
     *
     * @param sensor Sensor number
     * @param type   Thermocouple type
     *
     * @throws DeviceException Upon instrument error
     */
    public void setSensorType(int sensor, Thermocouple type) throws DeviceException, IOException {

        checkSensor(sensor);

        int result = lib.usb_tc08_set_channel(handle, (short) sensor, type.getCode());

        if (result == ACTION_FAILED) {
            throw new DeviceException(getLastError(handle));
        } else {
            types[sensor] = type;
        }

    }

    /**
     * Returns the sensor type that the given channel is configured for.
     *
     * @param sensor Sensor number
     *
     * @return Sensor type
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    public Thermocouple getSensorType(int sensor) throws DeviceException, IOException {

        checkSensor(sensor);
        return types[sensor];

    }

    /**
     * Returns the line-frequency rejection mode currently in use.
     *
     * @return Frequency, 50 Hz or 60 Hz.
     */
    public Frequency getLineFrequency() {
        return lineFrequency;
    }

    /**
     * Sets the line-frequency rejection mode to be used.
     *
     * @param frequency 50 Hz or 60 Hz
     *
     * @throws DeviceException Upon instrument error
     */
    public void setLineFrequency(Frequency frequency) throws DeviceException {


        int result = lib.usb_tc08_set_mains(handle, (short) frequency.ordinal());

        if (result == ACTION_FAILED) {
            throw new DeviceException(getLastError(handle));
        } else {
            lineFrequency = frequency;
        }


    }

    @Override
    public String getIDN() throws DeviceException {
        return String.format("PICO TC-08, S/N: \"%s\"", getSerial());
    }

    @Override
    public void close() throws DeviceException {

        int result = lib.usb_tc08_close_unit(handle);

        if (result == ACTION_FAILED) {
            throw new DeviceException(getLastError(handle));
        }

    }

    @Override
    public Address getAddress() {

        try {
            return new IDAddress(getSerial());
        } catch (Exception e) {
            return null;
        }

    }

    private String getLastError(short handle) {

        int error = lib.usb_tc08_get_last_error(handle);

        switch (error) {

            case ERROR_NONE:
                return "None";

            case ERROR_OS_NOT_SUPPORTED:
                return "OS not supported";

            case ERROR_NO_CHANNELS_SET:
                return "No channels set";

            case ERROR_INVALID_PARAMETER:
                return "Invalid parameter";

            case ERROR_VARIANT_NOT_SUPPORTED:
                return "Variant not supported";

            case ERROR_INCORRECT_MODE:
                return "Incorrect mode";

            case ERROR_ENUMERATION_INCOMPLETE:
                return "Enumeration incomplete";

            default:
                return "Unknown error";

        }

    }

    @Override
    public String getSensorName() {
        return getSensorName(0);
    }

    /**
     * Enumeration of line-frequency rejection modes.
     */
    public enum Frequency {
        FIFTY_HERTZ,
        SIXTY_HERTZ
    }

    /**
     * Interface corresponding to native usbtc08 library methods.
     */
    protected interface NativeInterface extends Library {

        int   USBTC08_MAX_CHANNELS         = 8;
        short USBTC08LINE_BATCH_AND_SERIAL = 4;
        byte  USB_TC08_THERMOCOUPLE_TYPE_B = (byte) 'B';
        byte  USB_TC08_THERMOCOUPLE_TYPE_E = (byte) 'E';
        byte  USB_TC08_THERMOCOUPLE_TYPE_J = (byte) 'J';
        byte  USB_TC08_THERMOCOUPLE_TYPE_K = (byte) 'K';
        byte  USB_TC08_THERMOCOUPLE_TYPE_N = (byte) 'N';
        byte  USB_TC08_THERMOCOUPLE_TYPE_R = (byte) 'R';
        byte  USB_TC08_THERMOCOUPLE_TYPE_S = (byte) 'S';
        byte  USB_TC08_THERMOCOUPLE_TYPE_T = (byte) 'T';
        byte  USB_TC08_VOLTAGE_READINGS    = (byte) 'X';
        byte  USB_TC08_DISABLE_CHANNEL     = (byte) ' ';

        short usb_tc08_open_unit();

        short usb_tc08_close_unit(short handle);

        short usb_tc08_set_mains(short handle, short sixty_hertz);

        short usb_tc08_set_channel(short handle, short channel, byte tc_type);

        int usb_tc08_get_minimum_interval_ms(short handle);

        short usb_tc08_get_formatted_info(short handle, byte[] unitInfo, short stringLength);

        short usb_tc08_get_unit_info2(short handle, byte[] unitInfo, short stringLength, short line);

        short usb_tc08_get_single(short handle, Memory temp, ShortByReference overflowFlags, short units);

        short usb_tc08_get_last_error(short handle);

    }

}
