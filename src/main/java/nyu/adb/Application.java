package nyu.adb;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.Instructions.Instruction;
import nyu.adb.Instructions.InstructionManager;
import nyu.adb.Instructions.InstructionType;
import nyu.adb.Sites.SiteManager;
import nyu.adb.Transactions.TransactionManager;
import nyu.adb.utils.IOUtils;

import java.io.IOException;
import java.io.PrintStream;

@Slf4j
public class Application {

    /*Usage
        java Application.main() <inputFile> <outputFIle>
    **/

    public static void main(String[] args) throws IOException{

        if (args.length != 2) {
            System.out.println("Usage : java Application.main() <inputFilePath> <out-file>. ");
            return;
        }

        //Get all singleton classes in main, so that garbage collector doesn't remove them at any point.
        IOUtils ioUtils = IOUtils.getInstance();
        SiteManager siteManager = SiteManager.getInstance();
        TransactionManager transactionManager = TransactionManager.getInstance();
        Tick tick = Tick.getInstance();

        String inputFileName = args[0];
        String outputFile = args[1];

        if (ioUtils.setAndOpenInputFile(inputFileName)) {
           log.info("File {} open, starting execution.", inputFileName);
        }

        PrintStream fileOut = new PrintStream(outputFile);
        PrintStream originalOut = System.out;

        System.setOut(fileOut);

        run(transactionManager, tick);

        System.setOut(originalOut);
        fileOut.close();
    }

    private static void run(TransactionManager transactionManager, Tick tick) throws IOException{
        InstructionManager instructionManager = new InstructionManager();
        while(true) {
            //check for deadlock and clear any issues
            while(transactionManager.checkAndAbortIfDeadlock()) {

            };

            //look at all waiting instructions and see if something can be done for them
            transactionManager.tryWaitingInstructions();

            //run the current instruction after everything else is done.
            Instruction currentInstruction = instructionManager.getNextInstruction();
            if (currentInstruction == null) {
                continue;
            }
            if (currentInstruction.getInstructionType().equals(InstructionType.CLEAN_UP)) {
                tick.increaseTick();
                break;
            }
            else {
                currentInstruction.execute();
            }

            tick.increaseTick();
        }

        if (!transactionManager.tryWaitingInstructions()) {
            log.info("Trying any remaining waiting instructions.");
            System.out.format("Something went wrong. EOF was found before all waiting instructions ended. \n Please check input file and ensure end instructions come only if all waiting instructions are finished.");
        }
    }
}
