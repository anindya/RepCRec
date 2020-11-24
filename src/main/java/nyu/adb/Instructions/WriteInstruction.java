package nyu.adb.Instructions;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.Locks.LockAcquiredStatus;
import nyu.adb.Transactions.Transaction;

@Slf4j
public class WriteInstruction extends Instruction{
    private static final String LOG_TAG = "WriteInstruction";
    private Transaction transaction;
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
    public void execute() {
        if(transaction.getDirtyBit().containsKey(variableName)) {
            log.info("{} : Variable {} already locked by transaction {}, write new value: {}", LOG_TAG, variableName, transaction.getTransactionName(), writeValue);
            transaction.writeToLocalCache(variableName, instructionLine, this.writeValue, null);
            System.out.format("%s : Variable %s already locked by transaction %s, write new value: %d\n", LOG_TAG, variableName, transaction.getTransactionName(), writeValue);
        } else {
            ExecuteResult executeResult = siteManager.writeVariableLock(variableName, transaction);
            if (executeResult.getLockAcquiredStatus().equals(LockAcquiredStatus.WAITING)) {

            } else if (executeResult.getLockAcquiredStatus().equals(LockAcquiredStatus.IN_RECOVERY)) {

            } else if (executeResult.getLockAcquiredStatus().equals(LockAcquiredStatus.ACQUIRED)) {
//                this.readValue = executeResult.getValue();
                log.info("{} : Variable {} locked by transaction {}, write new value: {}", LOG_TAG, variableName, transaction.getTransactionName(), writeValue);
                transaction.writeToLocalCache(variableName, instructionLine, this.writeValue, executeResult);
                System.out.format("%s : Variable %s locked by transaction %s, write new value: %d\n", instructionLine, variableName, transaction.getTransactionName(), writeValue);
            }
        }
    }
}
