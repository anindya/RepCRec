package nyu.adb.DeadlockManager;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.Instructions.Instruction;
import nyu.adb.Transactions.Transaction;
import nyu.adb.Transactions.TransactionManager;

import java.util.*;

@Slf4j
public class DFSYoungestAbort implements DeadlockManager {
    private static final String LOG_TAG = "DeadlockManager";
    private TransactionManager transactionManager = TransactionManager.getInstance();

    /**
     * Makes a recommendation for the transaction to be aborted based on YoungestAbort technique.
     * @param lockTablesData : list of map containing all data items at a site and it's corresponding locks.
     * @param instructionsWaitingForVariable : contains a list of instructions waiting for each data item (by it's name)
     *                                       the list is based on queue where first instruction is the earliest instruction waiting.
     * @return
     */
    @Override
    public DeadlockRecommendation checkDeadlock(List<Map<String, Map<Transaction, BitSet>>> lockTablesData,
                                                Map<String, Queue<Instruction>> instructionsWaitingForVariable) {
        DeadlockDetection deadlockDetection = new DeadlockDetection();
        Set<Transaction> verticesInDeadlock = deadlockDetection.findTransactionsInDeadlock(lockTablesData, instructionsWaitingForVariable);
        if (verticesInDeadlock.size() == 0) {
            return new DeadlockRecommendation(false, ""); //TODO Update transaction to remove NoArgsConstructor.
        }

        else {
            String transactionName = "";
            Integer youngestTransactionStartTimeInCycle = -1;
            for (Transaction transaction : verticesInDeadlock) {
                if (transaction.getStartTick() > youngestTransactionStartTimeInCycle) {
                    youngestTransactionStartTimeInCycle = transaction.getStartTick();
                    transactionName = transaction.getTransactionName();
                }
            }
            return new DeadlockRecommendation(true, transactionName);
        }
    }


}
