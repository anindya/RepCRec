package nyu.adb;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.Instructions.Instruction;
import nyu.adb.Instructions.InstructionManager;
import nyu.adb.utils.IOUtils;

import java.io.IOException;

@Slf4j
public class Application {

    /*Usage
        java Application.main() <inputFile> <stdIn>
    **/

    public static void main(String[] args) throws IOException{

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
            //Make proper calls;
            InstructionManager instructionManager = new InstructionManager();
            Instruction currentInstruction = instructionManager.getNextInstruction();
            while(currentInstruction != null) {
                currentInstruction.execute();
                currentInstruction = instructionManager.getNextInstruction();
            }
        }

    }
}
