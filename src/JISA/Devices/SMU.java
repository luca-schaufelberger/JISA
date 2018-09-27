package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Experiment.IVPoint;
import JISA.Util;
import JISA.VISA.VISADevice;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.ArrayList;

public abstract class SMU extends VISADevice {

    public SMU(InstrumentAddress address) throws IOException {
        super(address);
    }

    public SMU() {

    }

    /**
     * Returns the voltage either being applied or measured by the SMU.
     *
     * @return Voltage value
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getVoltage() throws DeviceException, IOException;

    /**
     * Returns the current either being injected or measured by the SMU.
     *
     * @return Current value
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getCurrent() throws DeviceException, IOException;

    /**
     * Sets the voltage value to be applied by the SMU (switching to voltage source mode if not already)
     *
     * @param voltage Value to set
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setVoltage(double voltage) throws DeviceException, IOException;

    /**
     * Sets the current value to be applied by the SMU (switching to current source mode if not already)
     *
     * @param current Value to set
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setCurrent(double current) throws DeviceException, IOException;

    /**
     * Turns the output of the SMU on
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void turnOn() throws DeviceException, IOException;

    /**
     * Turns the output of the SMU off
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void turnOff() throws DeviceException, IOException;

    /**
     * Checks whether the output of the SMU is currently enabled
     *
     * @return Is the output on?
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract boolean isOn() throws DeviceException, IOException;

    /**
     * Sets the source mode of the SMU (VOLTAGE or CURRENT)
     *
     * @param source Source mode to set
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setSource(Source source) throws DeviceException, IOException;

    /**
     * Returns the current source mode of the SMU (VOLTAGE OR CURRENT)
     *
     * @return Source mode
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract Source getSource() throws DeviceException, IOException;

    /**
     * Sets the value for whichever parameter is currently being sourced
     *
     * @param level The level to set
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setBias(double level) throws DeviceException, IOException;

    /**
     * Returns the value of whichever parameter is set as source currently
     *
     * @return Value of source
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getSourceValue() throws DeviceException, IOException;

    /**
     * Returns the value of whichever parameter is set as measure currently
     *
     * @return Value of measure
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getMeasureValue() throws DeviceException, IOException;

    /**
     * Performs a linear sweep of either VOLTAGE or CURRENT, returning the V-I data points as an array of IVPoint objects
     *
     * @param source    VOLTAGE or CURRENT
     * @param min       Minimum source value
     * @param max       Maximum source value
     * @param numSteps  Number of steps in sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @return Array of IVPoint objects containing I-V data points
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doLinearSweep(Source source, double min, double max, int numSteps, long delay, boolean symmetric) throws DeviceException, IOException {
        return doLinearSweep(source, min, max, numSteps, delay, symmetric, (i, point) -> {
        });
    }

    /**
     * Performs a linear sweep of either VOLTAGE or CURRENT, returning the V-I data points as an array of IVPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param source    VOLTAGE or CURRENT
     * @param min       Minimum source value
     * @param max       Maximum source value
     * @param numSteps  Number of steps in sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @param onUpdate  Method to run each time a new measurement is completed
     * @return Array of IVPoint objects containing I-V data points
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doLinearSweep(Source source, double min, double max, int numSteps, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {

        return doSweep(
                source,
                Util.makeLinearArray(min, max, numSteps),
                delay,
                symmetric,
                onUpdate
        );

    }

    /**
     * Performs a logarithmic sweep of either VOLTAGE or CURRENT, returning V-I data points as an array of IVPoint objects.
     *
     * @param source    VOLTAGE or CURRENT
     * @param min       Minimum source value
     * @param max       Maximum source value
     * @param numSteps  Number of steps in sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @return Array of IVPoint objects containing V-I data points
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doLogarithmicSweep(Source source, double min, double max, int numSteps, long delay, boolean symmetric) throws DeviceException, IOException {

        return doLogarithmicSweep(source, min, max, numSteps, delay, symmetric, (i, p) -> {
        });

    }

    /**
     * Performs a logarithmic sweep of either VOLTAGE or CURRENT, returning V-I data points as an array of IVPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param source    VOLTAGE or CURRENT
     * @param min       Minimum source value
     * @param max       Maximum source value
     * @param numSteps  Number of steps in sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @param onUpdate  Method ot run each time a new measurement is completed
     * @return Array of IVPoint objects containing V-I data points
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doLogarithmicSweep(Source source, double min, double max, int numSteps, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {

        return doSweep(
                source,
                Util.makeLogarithmicArray(min, max, numSteps),
                delay,
                symmetric,
                onUpdate
        );

    }

    /**
     * Performs a logarithmic sweep of either VOLTAGE or CURRENT, returning V-I data points as an array of IVPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param source    VOLTAGE or CURRENT
     * @param values    Array of values to use in the sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @param onUpdate  Method ot run each time a new measurement is completed
     * @return Array of IVPoint objects containing V-I data points
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doSweep(Source source, double[] values, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {

        if (symmetric) {
            values = Util.symArray(values);
        }

        turnOff();
        setSource(source);
        setBias(values[0]);

        int                i      = 0;
        ArrayList<IVPoint> points = new ArrayList<>();

        turnOn();

        for (double b : values) {

            setBias(b);
            try {
                Thread.sleep(delay);
            } catch (Exception e) {
                throw new DeviceException("Couldn't sleep!");
            }

            IVPoint point = new IVPoint(getVoltage(), getCurrent());
            onUpdate.update(i, point);
            points.add(point);
            i++;

        }

        return points.toArray(new IVPoint[0]);

    }

    /**
     * Performs a logarithmic sweep of either VOLTAGE or CURRENT, returning V-I data points as an array of IVPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param source    VOLTAGE or CURRENT
     * @param values    Array of values to use in the sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @return Array of IVPoint objects containing V-I data points
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doSweep(Source source, double[] values, boolean symmetric, long delay) throws IOException, DeviceException {
        return doSweep(source, values, delay, symmetric, (i, p) -> {
        });
    }

    /**
     * Enumeration of source modes
     */
    public enum Source {
        VOLTAGE,
        CURRENT
    }


    /**
     * Structure for defining what to do on each update
     */
    public interface ProgressMonitor {

        public void update(int i, IVPoint point) throws IOException, DeviceException;

    }

}
