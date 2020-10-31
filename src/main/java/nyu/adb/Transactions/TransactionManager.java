package nyu.adb.Transactions;

import lombok.NoArgsConstructor;
import nyu.adb.Instructions.Instruction;

import java.util.Map;
import java.util.PriorityQueue;

/*
Singleton class of txnManager, to be called by instructionManager
 */
@NoArgsConstructor
public class TransactionManager {
    Map<String, Transaction> transactionList;
//  Create a queue of transactions based on the age, when a transaction comeplete remove it from the queue.
//    This is the abort queue to be used when in deadlock
//    Stack<Transaction> transactionsAgeQueue
    private static  final TransactionManager transactionManagerInstance = new TransactionManager();

    public TransactionManager getTransactionManagerInstance() {
        return transactionManagerInstance;
    }

    /*
    Creates a new transaction if instructionType is BEGIN/BEGIN_RO
    else add instruction to transaction from the map.
     */
    public void addInstruction(String transactionName, Instruction instruction) {

    }

    public Boolean checkDeadlock() {
        //check all transactions and create a graph for checking deadlock.
        //Abort the youngest transaction.

        return true;
    }


}
