package nyu.adb.Instructions;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.Locks.LockAcquiredStatus;
import nyu.adb.Locks.LockType;
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
    public Boolean execute() {
        if (transaction.getTransactionType().equals(TransactionType.READ_WRITE)) {

            if(transaction.getLocalCache().containsKey(variableName)) {
                log.info("{} read variable {} from local cache of transaction {}", LOG_TAG, variableName, transaction.getTransactionName());
                this.readValue = transaction.getLocalCache().get(variableName);
                transaction.cacheRead(variableName, instructionLine, this.readValue);
                System.out.format("%s Local Cache read: %d\n", instructionLine, this.readValue);
                return true;
            } else {
                ExecuteResult executeResult = siteManager.readVariable(variableName, transaction);
                if (executeResult.getLockAcquiredStatus().equals(LockAcquiredStatus.WAITING)) {
                    transactionManager.addToWaitingQ(this, variableName);
                } else if (executeResult.getLockAcquiredStatus().equals(LockAcquiredStatus.IN_RECOVERY)) {
                    transactionManager.addToWaitingQ(this, variableName);
                } else if (executeResult.getLockAcquiredStatus().equals(LockAcquiredStatus.ACQUIRED)) {
                    this.readValue = executeResult.getValue();
                    transaction.newRead(executeResult, variableName, instructionLine);
                    transactionManager.newLocksAcquired(transaction, variableName, LockType.READ);
                    log.info("{} read variable {} from site {} for transaction {}", LOG_TAG, variableName, executeResult.getSiteNumberAndUpTime().keySet().stream().findFirst(), transaction.getTransactionName());
                    System.out.format("%s read variable %s from site %d for transaction %s, value %s\n", instructionLine, variableName, executeResult.getSiteNumberAndUpTime().keySet().stream().findFirst().get(), transaction.getTransactionName(), this.readValue);
                    return true;
                }
            }
        } else {
            //Read only reads.
        }
        return false;
    }
}
