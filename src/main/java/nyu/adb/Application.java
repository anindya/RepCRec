package nyu.adb;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.utils.IOUtils;

import java.io.IOException;

@Slf4j
public class Application {

    /*Usage
        java Application.main() <inputFile> <stdIn>
    **/

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage : java Application.main() <inputFilePath> <stdIn = 0/1>. \n" +
                                "If stdIn = 1, put anything in inputFile.");
        }
        String inputFileName = args[0];
        boolean stdIn = args[1].equals('1');
        IOUtils ioUtils = IOUtils.getInstance();
        if (stdIn) {
            IOUtils.getInstance().setStdIn(true);
            System.out.println("Please enter input, one line at a time.");
        } else {
            if (IOUtils.getInstance().setAndOpenInputFile(inputFileName)) {
               log.info("File {} open, starting execution.", inputFileName);
            }
            try {
                ioUtils.getNextLine();
                ioUtils.getNextLine();
            } catch (IOException ignored) {

            }
            //Make proper calls;
        }

    }
}
