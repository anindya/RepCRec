package nyu.adb.Transactions;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nyu.adb.DeadlockManager.DFSYoungestAbort;
import nyu.adb.DeadlockManager.DeadlockManagerImpl;
import nyu.adb.Instructions.Instruction;
import nyu.adb.Locks.LockTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
Singleton class of txnManager, to be called by instructionManager
 */
@NoArgsConstructor @Slf4j
public class TransactionManager {
    private static final String LOG_TAG = "TransactionManager";
    Map<String, Transaction> transactionList = new HashMap<>();
//  Create a queue of transactions based on the age, when a transaction complete remove it from the queue.
//    This is the abort queue to be used when in deadlock
//    Stack<Transaction> transactionsAgeQueue
    private static  final TransactionManager transactionManagerInstance = new TransactionManager();

    public static TransactionManager getInstance() {
        return transactionManagerInstance;
    }

    /*
    Creates a new transaction if instructionType is BEGIN/BEGIN_RO
    else add instruction to transaction from the map.
     */
    public void addInstruction(String transactionName, Instruction instruction) {

    }

    public Transaction createNewTransaction(String transactionName, TransactionType transactionType) {
        if (transactionList.containsKey(transactionName)) {
            log.error("{} transactionName already exists {}, details of existing transaction: {}",
                    LOG_TAG, transactionName, transactionList.get(transactionName));
            return null; //TODO change this to throw error if time permits.
        } else {
            Transaction txn = new Transaction(transactionName, transactionType);
            transactionList.put(transactionName, txn);
            return txn;
        }
    }

    public Boolean checkDeadlock() {
        //check all transactions and create a graph for checking deadlock.
        //Abort the youngest transaction.
        DeadlockManagerImpl deadlockManagerImpl = new DFSYoungestAbort();

        //TODO, update this to send all locktables from all sites and handle the recommendation created by the deadlock check algorithm
        deadlockManagerImpl.checkDeadlock(new ArrayList<LockTable>());
        return true;
    }

    /*
    Creates a new transaction if instructionType is BEGIN/BEGIN_RO
    else add instruction to transaction from the map.
     */
    public Transaction getTransaction(String transactionName) {
        if (transactionList.containsKey(transactionName)) {
            return transactionList.get(transactionName);
        } else {
            log.error("{} incorrect transaction name in instruction, transactionName : {}", LOG_TAG, transactionName);
            return null; //TODO Change to error if time permits.
        }
    }
}
