package nyu.adb.Instructions;

import nyu.adb.Transactions.Transaction;
import nyu.adb.Transactions.TransactionStatus;
import nyu.adb.Transactions.TransactionType;

import java.util.Map;

public class EndTxnInstruction extends Instruction{
    private final Transaction transaction;

    public EndTxnInstruction(InstructionType instructionType, Transaction txn,
                             String instructionLine) {
        super(instructionType, instructionLine);
        this.transaction = txn;
    }

    @Override
    public Boolean execute() {
//        Map<Integer, Set<String>> sitesAccessed = this.transaction.getSitesAccessed();
//        for (Map.Entry<Integer, Set<String>> entry : sitesAccessed.entrySet()) {
//            //commit values and clear all locks. First commit or decide if abort in case some site went down from where access was done.
//        }
        if (this.transaction.getTransactionType().equals(TransactionType.READ_ONLY)) {
            this.transaction.setFinalStatus(TransactionStatus.COMMIT);
            return true;
        }
        if (checkAllAccessedSitesUpSinceFirstAccess()) {
//            for (String variableName : this.transaction.getDirtyBit()) {
//                siteManager.writeToSites(variableName, this.transaction.getSitesAccessedForVariable().get(variableName),
//                        this.transaction.getLocalCache().get(variableName), this.transaction);
//            }

            for (String variableName : this.transaction.getLocalCache().keySet()) {
                siteManager.cleanUpAtSites(variableName, this.transaction.getSitesAccessedForVariable().get(variableName),
                        this.transaction.getLocalCache().get(variableName), this.transaction, this.transaction.getDirtyBit().contains(variableName));
            }
            this.transaction.setFinalStatus(TransactionStatus.COMMIT);
        } else {
            for (String variableName : this.transaction.getLocalCache().keySet()) {
                siteManager.cleanUpAtSites(variableName, this.transaction.getSitesAccessedForVariable().get(variableName),
                        this.transaction.getLocalCache().get(variableName), this.transaction, false);
            }
            this.transaction.setFinalStatus(TransactionStatus.ABORT);
        }
        return true;
    }

    private Boolean checkAllAccessedSitesUpSinceFirstAccess() {
        Map<Integer, Integer> siteEarliestUpTimeWhenAccessingIt = this.transaction.getSiteEarliestUpTimeWhenAccessingIt();
        for (Map.Entry<Integer, Integer> entry : siteEarliestUpTimeWhenAccessingIt.entrySet()) {
            //If the site upSinceTime changed, it means that the site failed after the txn accessed it
            //Abort transaction
            if (!siteManager.getSiteUpTime(entry.getKey()).equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}
