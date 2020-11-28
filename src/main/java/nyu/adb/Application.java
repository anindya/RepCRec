package nyu.adb;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.Instructions.Instruction;
import nyu.adb.Instructions.InstructionManager;
import nyu.adb.Instructions.InstructionType;
import nyu.adb.Sites.SiteManager;
import nyu.adb.Transactions.TransactionManager;
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

        //Get all singleton classes in main, so that garbage collector doesn't remove them at any point.
        IOUtils ioUtils = IOUtils.getInstance();
        SiteManager siteManager = SiteManager.getInstance();
        TransactionManager transactionManager = TransactionManager.getInstance();
        Tick tick = Tick.getInstance();

        String inputFileName = args[0];
        boolean stdIn = args[1].equals('1');

        if (stdIn) {
            ioUtils.setStdIn(true);
            System.out.println("Please enter input, one line at a time.");
        } else {
            if (ioUtils.setAndOpenInputFile(inputFileName)) {
               log.info("File {} open, starting execution.", inputFileName);
            }
        }

        run(siteManager, transactionManager, tick);


    }

    private static void run(SiteManager siteManager, TransactionManager transactionManager, Tick tick) throws IOException{
        InstructionManager instructionManager = new InstructionManager();
        while(true) {
            //check for deadlock and clear any issues


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

        while (!transactionManager.tryWaitingInstructions()) {
            log.info("Trying any remaining waiting instructions.");
        }

        //TODO Check waiting instructions are finished or aborted at transactionManager.
    }
}
