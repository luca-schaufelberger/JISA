package JISA;

import JISA.Addresses.InstrumentAddress;
import JISA.Devices.LockIn;
import JISA.Devices.SMU;
import JISA.Devices.TController;
import JISA.GUI.*;
import JISA.VISA.VISA;
import javafx.application.Platform;

import java.io.*;

public class Main {

    public static void main(String[] args) {

        // Start the GUI thread
        GUI.startGUI();

        ConfigGrid                    configGrid = new ConfigGrid("Instrument Connection");
        InstrumentConfig<SMU>         smu1       = configGrid.addInstrument("SMU 1", SMU.class);
        InstrumentConfig<SMU>         smu2       = configGrid.addInstrument("SMU 2", SMU.class);
        InstrumentConfig<TController> tc         = configGrid.addInstrument("Temperature Control", TController.class);
        InstrumentConfig<LockIn>      lock       = configGrid.addInstrument("Lock-In Amplifier", LockIn.class);
        configGrid.setNumColumns(2);
        configGrid.show();


        try {
            System.in.read();

            // Ask the user if they want to perform a test
            boolean result = GUI.confirmWindow("JISA", "JISA Library", "JISA - William Wood - 2018\n\nPerform VISA test?");

            // If they press "Cancel", then exit.
            if (!result) {
                Platform.exit();
                return;
            }

            // Trigger VISA initialisation before we try browsing.
            VISA.init();

            // Keep going until they press cancel
            while (true) {

                InstrumentAddress address = GUI.browseVISA();

                if (address == null) {
                    Platform.exit();
                    System.exit(0);
                }

                // Create the device shell, connect to the device and show
                DeviceShell shell = new DeviceShell(address);
                shell.connect();
                shell.showAndWait();

            }

        } catch (Exception | Error e) {
            Util.sleep(500);
            StringWriter w = new StringWriter();
            w.append(e.getMessage());
            w.append("\n\n");
            e.printStackTrace(new PrintWriter(w));
            GUI.errorAlert("JISA Library", "Exception Encountered", w.toString(), 800);
            Platform.exit();
            System.exit(0);
        }

    }

}
