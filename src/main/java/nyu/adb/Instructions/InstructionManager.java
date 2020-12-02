//Authors : Anindya Chakravarti, Rohan Mahadev

package nyu.adb.Instructions;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nyu.adb.Transactions.TransactionManager;
import nyu.adb.Transactions.TransactionType;
import nyu.adb.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Class to get and parse instruction from IO file.
 */
@Slf4j @NoArgsConstructor
public class InstructionManager {
    private static final String LOG_TAG = "InstructionManager";
    private final IOUtils ioUtils = IOUtils.getInstance();
    private final TransactionManager transactionManager = TransactionManager.getInstance();

    /**
     * Gets next line from IOUtil and creates the right Instruction based on the first keyword before "("
     * Assumes that the input files are clean and do not any input validation.
     * @return Instruction parsed from the input line read by IOUtils from input file
     * @throws IOException if file is not open or not found.
     */
    public Instruction getNextInstruction() throws IOException {
        String instructionLine = ioUtils.getNextLine();
        Instruction nextInstruction = null;
        if (instructionLine == null) {
            log.info("{} Reached end of file", LOG_TAG);
            return new Instruction(InstructionType.CLEAN_UP, null);
        }
        if (StringUtils.isEmpty(instructionLine)) {
            //TODO
        } else {
            String[] instructionNameAndParams = instructionLine.split("\\(");

            String[] params = null;
            if (instructionNameAndParams.length == 2) {
                params = instructionNameAndParams[1]
                        .replaceAll("\\)", "")
                        .replaceAll(" ", "")
                        .split(",");

            }
            InstructionType instructionType = InstructionType.getInstructionType(instructionNameAndParams[0].strip());

            // If length of params is 0, then it can only be a dump instruction.
            // If it's not, then either a parsing error occurred or the input is incorrect
            if (params == null || params.length == 0 && instructionType != InstructionType.DUMP) {
                log.error("{} Invalid input : {}", LOG_TAG, instructionLine);
                return null;
            } else {
                String txnNumber = params[0];
                if (transactionManager.isTerminated(txnNumber)) {
                    log.info("{} : Transaction {} already terminated", LOG_TAG, txnNumber);
                    return null;
                }
                switch (instructionType){
                    case DUMP:
                        nextInstruction = new DumpInstruction(InstructionType.DUMP, instructionLine);
                        break;
                    case END:
                        nextInstruction = new EndTxnInstruction(InstructionType.END, transactionManager.getTransaction(txnNumber)
                                , instructionLine);
                        break;
                    case WRITE:
                        nextInstruction = new WriteInstruction(InstructionType.WRITE, transactionManager.getTransaction(txnNumber),
                                params[1], Integer.valueOf(params[2]), instructionLine);
                        break;
                    case READ:
                        nextInstruction = new ReadInstruction(InstructionType.READ, transactionManager.getTransaction(txnNumber),
                                params[1], instructionLine);
                        break;
                    case BEGIN:
                        nextInstruction = new BeginTxnInstruction(InstructionType.BEGIN, txnNumber,
                                TransactionType.READ_WRITE, instructionLine);
                        break;
                    case BEGIN_RO:
                        nextInstruction = new BeginReadOnlyTxnInstruction(InstructionType.BEGIN_RO, txnNumber,
                                TransactionType.READ_ONLY, instructionLine);
                        break;
                    case FAIL:
                        nextInstruction = new FailSiteInstruction(InstructionType.FAIL,
                                Integer.valueOf(txnNumber), instructionLine);
                        break;
                    case RECOVER:
                        nextInstruction = new RecoverSiteInstruction(InstructionType.RECOVER,
                                Integer.valueOf(txnNumber), instructionLine);
                        break;
                    default:
                        log.error("{} : Invalid instructionType {}", LOG_TAG, instructionNameAndParams[0]);
                        return null; //TODO throw error if time permits.
                }
            }
        }
        log.info("{} Instruction : {}", LOG_TAG, nextInstruction);
        //TODO update Tick
        return nextInstruction;
    }

}
