package nyu.adb.Instructions;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.Locks.LockAcquiredStatus;
import nyu.adb.Transactions.Transaction;

/**
 * Class to handle write instruction from the input
 * Follows format W(<Transaction name> , <variableName>, <writeValue>)
 */
@Slf4j
public class WriteInstruction extends Instruction{
    private static final String LOG_TAG = "WriteInstruction";
    private String variableName;
//    private Integer readValue;
    private Integer writeValue; //To be used only if instructionType == WRITE

    public WriteInstruction(InstructionType instructionType, Transaction txn, String variableName, Integer writeValue,
                            String instructionLine) {
        super(instructionType, instructionLine);
        this.transaction = txn;
        this.variableName = variableName;
        this.writeValue = writeValue;
    }

    @Override
    public Boolean execute() {
        if(transaction.getDirtyBit().contains(variableName)) {
            log.info("{} : Variable {} already locked by transaction {}, write new value: {}", LOG_TAG, variableName, transaction.getTransactionName(), writeValue);
            transaction.writeToLocalCache(variableName, instructionLine, this.writeValue, null);
            System.out.format("%s : Variable %s already locked by transaction %s, write new value: %d\n", LOG_TAG, variableName, transaction.getTransactionName(), writeValue);
            return true;
        } else {
            ExecuteResult executeResult = siteManager.writeVariableLock(variableName, transaction);
            if (executeResult.getLockAcquiredStatus().equals(LockAcquiredStatus.WAITING)) {
                transactionManager.addToWaitingQ(this, variableName);
            } else if (executeResult.getLockAcquiredStatus().equals(LockAcquiredStatus.IN_RECOVERY)) {
                transactionManager.addToWaitingQ(this, variableName);
            } else if (executeResult.getLockAcquiredStatus().equals(LockAcquiredStatus.ACQUIRED)) {
//                this.readValue = executeResult.getValue();
                log.info("{} : Variable {} locked by transaction {}, write new value: {}", LOG_TAG, variableName, transaction.getTransactionName(), writeValue);
                transaction.writeToLocalCache(variableName, instructionLine, this.writeValue, executeResult);
                System.out.format("%s : Variable %s locked by transaction %s, write new value: %d\n", instructionLine, variableName, transaction.getTransactionName(), writeValue);
                return true;
            }
        }
        return false;
    }
}
