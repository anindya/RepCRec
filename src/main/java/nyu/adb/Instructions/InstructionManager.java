package nyu.adb.Instructions;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nyu.adb.Transactions.TransactionManager;
import nyu.adb.Transactions.TransactionType;
import nyu.adb.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

@Slf4j @NoArgsConstructor
public class InstructionManager {
    private static final String LOG_TAG = "InstructionManager";
    private final IOUtils ioUtils = IOUtils.getInstance();
    private final TransactionManager transactionManager = TransactionManager.getInstance();

    public Instruction getNextInstruction() throws IOException {
        String instructionLine = ioUtils.getNextLine();
        Instruction nextInstruction = null;

        if (instructionLine == null) {
            log.info("{} Reached end of file", LOG_TAG);
            //start cleanup
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
                switch (instructionType){
                    case DUMP:
                        nextInstruction = new DumpInstruction(InstructionType.DUMP);
                        break;
                    case END:
                        nextInstruction = new EndTxnInstruction(InstructionType.END, transactionManager.getTransaction(params[0]));
                        break;
                    case WRITE:
                        nextInstruction = new WriteInstruction(InstructionType.WRITE, transactionManager.getTransaction(params[0]), params[1], Integer.valueOf(params[2]));
                        break;
                    case READ:
                        nextInstruction = new ReadInstruction(InstructionType.READ, transactionManager.getTransaction(params[0]), params[1]);
                        break;
                    case BEGIN:
                        nextInstruction = new BeginTxnInstruction(InstructionType.BEGIN, params[0], TransactionType.READ_WRITE);
                        break;
                    case BEGIN_RO:
                        nextInstruction = new BeginReadOnlyTxnInstruction(InstructionType.BEGIN_RO, params[0], TransactionType.READ_ONLY);
                        break;
                    case FAIL:
                        nextInstruction = new FailSiteInstruction(InstructionType.FAIL, params[0]);
                        break;
                    case RECOVER:
                        nextInstruction = new RecoverSiteInstruction(InstructionType.RECOVER, params[0]);
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
