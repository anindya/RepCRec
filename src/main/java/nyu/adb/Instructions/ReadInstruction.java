package nyu.adb.Instructions;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.Locks.LockAcquiredStatus;
import nyu.adb.Transactions.Transaction;
import nyu.adb.Transactions.TransactionType;

@Slf4j
public class ReadInstruction extends Instruction{
    private static final String LOG_TAG = "ReadInstruction";
    private final Transaction transaction;
    private final String variableName;
    private Integer readValue;

    public ReadInstruction(InstructionType instructionType, Transaction txn, String variableName,
                           String instructionLine) {
        super(instructionType, instructionLine);
        this.transaction = txn;
        this.variableName = variableName;
    }

    @Override
    public void execute() {
        if (transaction.getTransactionType().equals(TransactionType.READ_WRITE)) {

            if(transaction.getLocalCache().containsKey(variableName)) {
                log.info("{} read variable {} from local cache of transaction {}", LOG_TAG, variableName, transaction.getTransactionName());
                this.readValue = transaction.getLocalCache().get(variableName);
                transaction.cacheRead(variableName, instructionLine, this.readValue);
                System.out.format("%s Local Cache read: %d\n", instructionLine, this.readValue);
            } else {
                ExecuteResult executeResult = siteManager.readVariable(variableName, transaction);
                if (executeResult.getLockAcquiredStatus().equals(LockAcquiredStatus.WAITING)) {

                } else if (executeResult.getLockAcquiredStatus().equals(LockAcquiredStatus.IN_RECOVERY)) {

                } else if (executeResult.getLockAcquiredStatus().equals(LockAcquiredStatus.ACQUIRED)) {
                    this.readValue = executeResult.getValue();
                    transaction.newRead(executeResult, variableName, instructionLine);
                    log.info("{} read variable {} from site {} for transaction {}", LOG_TAG, variableName, executeResult.getSiteNumber(), transaction.getTransactionName());
                    System.out.format("%s read variable %s from site %d for transaction %s, value %s\n", instructionLine, variableName, executeResult.getSiteNumber(), transaction.getTransactionName(), this.readValue);
                }
            }
        } else {

        }
    }
}
