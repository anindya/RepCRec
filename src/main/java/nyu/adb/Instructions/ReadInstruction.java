package nyu.adb.Instructions;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.Locks.LockAcquiredStatus;
import nyu.adb.Locks.LockType;
import nyu.adb.Transactions.Transaction;
import nyu.adb.Transactions.TransactionStatus;
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

        if(transaction.getLocalCache().containsKey(variableName)) {
            log.info("{} read variable {} from local cache of transaction {}", LOG_TAG, variableName, transaction.getTransactionName());
            this.readValue = transaction.getLocalCache().get(variableName);
            transaction.cacheRead(variableName, instructionLine, this.readValue);
            System.out.format("%s Local Cache read: %d\n", instructionLine, this.readValue);
            return true;
        }
        if (transaction.getTransactionType().equals(TransactionType.READ_WRITE)) {
            ExecuteResult executeResult = siteManager.readVariable(variableName, transaction);
            if (executeResult.getLockAcquiredStatus().equals(LockAcquiredStatus.WAITING)) {
                transactionManager.addToWaitingQ(this, variableName);
            } else if (executeResult.getLockAcquiredStatus().equals(LockAcquiredStatus.IN_RECOVERY)) {
                transactionManager.addToWaitingQ(this, variableName);
            } else if (executeResult.getLockAcquiredStatus().equals(LockAcquiredStatus.ACQUIRED)) {
                return newRead(executeResult);
            }
        } else {
            //Read only reads.
            //TODO Check this for sanity.
            if (transaction.getFinalStatus() != null && transaction.getFinalStatus().equals(TransactionStatus.ABORT)) {
                log.info("{} Read-only transaction {} already aborted", LOG_TAG, transaction.getTransactionName());
                return true;
            }
            ExecuteResult executeResult = siteManager.readVariableVersion(variableName, transaction);
            log.info("status : {}", executeResult.getLockAcquiredStatus());
            switch (executeResult.getLockAcquiredStatus()) {
                case WAITING:
                case ALL_DOWN:
                    transactionManager.addToWaitingRoQ(this, variableName);
                    break;
                case ACQUIRED:
                    newRead(executeResult);
                    return true;
                case ALL_DOWN_FOR_RO:
                    log.info("{} read variable {} for transaction {} failed. No site has relevant version.", LOG_TAG, variableName, transaction.getTransactionName());
                    System.out.format("%s transaction %s abort\n", instructionLine, transaction.getTransactionName());
                    transaction.setFinalStatus(TransactionStatus.ABORT); //next time the instructions for this transaction come to execute, line 1 will return true and thus they will be removed.
                    return true;
            }
        }
        return false;
    }

    private Boolean newRead(ExecuteResult executeResult) {
        this.readValue = executeResult.getValue();
        transaction.newRead(executeResult, variableName, instructionLine);
        transactionManager.newLocksAcquired(transaction, variableName, LockType.READ);
        log.info("{} read variable {} from site {} for transaction {}", LOG_TAG, variableName, executeResult.getSiteNumberAndUpTime().keySet().stream().findFirst().get(), transaction.getTransactionName());
        System.out.format("%s read variable %s from site %d for transaction %s, value %s\n", instructionLine, variableName, executeResult.getSiteNumberAndUpTime().keySet().stream().findFirst().get(), transaction.getTransactionName(), this.readValue);
        return true;
    }
}
