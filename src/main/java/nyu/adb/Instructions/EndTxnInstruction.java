package nyu.adb.Instructions;

import nyu.adb.Transactions.Transaction;
import nyu.adb.Transactions.TransactionStatus;
import nyu.adb.Transactions.TransactionType;

import java.util.Map;

/**
 * Class to handle end instruction from the input.
 * Execution ends a transaction.
 * Right now, it assumes that no other waiting command would exist when end transaction comes in.
 * Follows format end(<Transaction Name>)
 */
public class EndTxnInstruction extends Instruction{
    private final Transaction transaction;

    public EndTxnInstruction(InstructionType instructionType, Transaction txn,
                             String instructionLine) {
        super(instructionType, instructionLine);
        this.transaction = txn;
    }

    @Override
    public Boolean execute() {
        if (this.transaction.getTransactionType().equals(TransactionType.READ_ONLY)) {
            System.out.format("%s commits\n", this.transaction.getTransactionName());
            this.transaction.setFinalStatus(TransactionStatus.COMMIT);
            return true;
        }
        if (checkAllAccessedSitesUpSinceFirstAccess()) {

            for (String variableName : this.transaction.getLocalCache().keySet()) {
                siteManager.cleanUpAtSites(variableName, this.transaction.getSitesAccessedForVariable().get(variableName),
                        this.transaction.getLocalCache().get(variableName), this.transaction, this.transaction.getDirtyBit().contains(variableName));
            }
            System.out.format("%s commits.\n", this.transaction.getTransactionName());
            this.transaction.setFinalStatus(TransactionStatus.COMMIT);
        } else {
            siteManager.cleanUpAtSitesAbort(this.transaction);
        }
        return true;
    }

    private Boolean checkAllAccessedSitesUpSinceFirstAccess() {
        Map<Integer, Integer> siteEarliestUpTimeWhenAccessingIt = this.transaction.getSiteEarliestUpTimeWhenAccessingIt();
        for (Map.Entry<Integer, Integer> entry : siteEarliestUpTimeWhenAccessingIt.entrySet()) {
            //If the site upSinceTime changed, it means that the site failed after the txn accessed it
            //Abort transaction
            //If the site is not up right now according to the siteManager, then too we need to abort.
            if (! siteManager.isUp(entry.getKey()) || !siteManager.getSiteUpTime(entry.getKey()).equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}
