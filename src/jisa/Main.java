package jisa;

import javafx.application.Platform;
import jisa.addresses.Address;
import jisa.addresses.StrAddress;
import jisa.gui.DeviceShell;
import jisa.gui.Doc;
import jisa.gui.GUI;
import jisa.maths.Range;
import jisa.maths.functions.XYFunction;
import jisa.maths.interpolation.Interpolation;
import jisa.maths.matrices.RealMatrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

public class Main {

    private final static int CHOICE_SCAN = 0;
    private final static int CHOICE_ADDR = 1;
    private final static int CHOICE_HELP = 2;
    private final static int CHOICE_EXIT = 3;

    public static void main(String[] args) {

        RealMatrix x = RealMatrix.asColumn(1, 1, 1, 2, 2, 2, 3, 3, 3);
        RealMatrix y = RealMatrix.asColumn(1, 2, 3, 1, 2, 3, 1, 2, 3);
        RealMatrix v = RealMatrix.asColumn(1, 5, 3, 6, 2, 7, 2, 9, 0);

        XYFunction interpolated = Interpolation.interpolate2D(x, y, v);

        for (double yv : Range.linear(1, 3, 6)) {

            for (double xv : Range.linear(1, 3, 6)) {

                System.out.printf("%e", interpolated.value(xv, yv));
                System.out.print("\t");

            }

            System.out.println();

        }

        try {

            while (true) {

                // Ask the user if they want to perform a test
                int result = GUI.choiceWindow(
                    "JISA",
                    "JISA Library - William Wood - 2018-2020",
                    "What would you like to do?",
                    "Scan for Instruments",
                    "Enter Address Manually",
                    "Help",
                    "Exit"
                );

                switch (result) {

                    case CHOICE_SCAN:
                        Address address = GUI.browseVISA();

                        if (address == null) {
                            break;
                        }

                        // Create the device shell, connect to the device and show
                        DeviceShell shell = new DeviceShell(address);
                        shell.connect();
                        shell.showAndWait();
                        break;

                    case CHOICE_ADDR:
                        String[] values = GUI.inputWindow(
                            "JISA",
                            "Input Address",
                            "Please type the VISA address to connect to...",
                            "Address"
                        );

                        if (values == null) {
                            break;
                        }

                        DeviceShell conShell = new DeviceShell(new StrAddress(values[0]));
                        conShell.connect();
                        conShell.showAndWait();
                        break;

                    case CHOICE_HELP:

                        Doc doc = new Doc("Help");
                        doc.setIcon(new URL("https://i.imgur.com/DbXtrcM.png"));

                        doc.addImage("https://i.imgur.com/bBE3oK4.png")
                           .setAlignment(Doc.Align.CENTRE);
                        doc.addHeading("Testing Utility")
                           .setAlignment(Doc.Align.CENTRE);
                        doc.addText("This is the built-in testing utility for JISA. Using this utility, you can:");
                        doc.addList(false)
                           .addItem("Scan for instruments, to see what instruments JISA can detect")
                           .addItem("Enter address manually, to connect to an instrument with a known address")
                           .addItem("Exit, to exit this utility");
                        doc.addText("For more information regarding how to include and use this library in your project, take a look at the JISA wiki at:");
                        doc.addLink("https://github.com/OE-FET/JISA/wiki", "https://github.com/OE-FET/JISA/wiki")
                           .setAlignment(Doc.Align.CENTRE);

                        doc.showAndWait();
                        break;

                    case CHOICE_EXIT:
                        GUI.stopGUI();
                        System.exit(0);
                        break;


                }

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
