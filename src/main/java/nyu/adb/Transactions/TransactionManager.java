package nyu.adb.Transactions;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.DeadlockManager.DFSYoungestAbort;
import nyu.adb.DeadlockManager.DeadlockManagerImpl;
import nyu.adb.Instructions.Instruction;
import nyu.adb.Locks.LockTable;
import nyu.adb.Locks.LockType;

import java.util.*;

/*
Singleton class of txnManager, to be called by instructionManager
 */
@Slf4j
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

    //Stack of instructions waiting for variable
    Map<String, Queue<Instruction>> instructionsWaitingForVariable;
    Map<String, Set<Instruction>> ROInstructionsWaitingForVariable;

    Set<Instruction> waitingInstructions;
    //Set of transactions waiting for variable
    Map<String, Set<Transaction>> txnsWaitingForVariable;
    Map<String, Set<Transaction>> readLocksHeldByTxn; //TODO Think more on this and its use.
    Map<String, Transaction> writeLockHeldByTxn;

    private TransactionManager() {
        this.transactionList = new HashMap<>();
        this.instructionsWaitingForVariable = new HashMap<>();
        this.ROInstructionsWaitingForVariable = new HashMap<>();
        this.txnsWaitingForVariable = new HashMap<>();
        this.readLocksHeldByTxn = new HashMap<>();
        this.writeLockHeldByTxn = new HashMap<>();
        this.waitingInstructions = new HashSet<>();
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

    //True if terminated
    public Boolean isTerminated(String txnName) {
        return transactionList.containsKey(txnName) && getTransaction(txnName).getFinalStatus() != null;
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

    public void newLocksAcquired(Transaction transaction, String variableName, LockType lockType) {
        Set<Transaction> currentSet;
        if (lockType.equals(LockType.READ)) {
            currentSet = readLocksHeldByTxn.containsKey(variableName) ? readLocksHeldByTxn.get(variableName) : new HashSet<>();
            currentSet.add(transaction);
            readLocksHeldByTxn.put(variableName, currentSet);
        } else if (lockType.equals(LockType.WRITE)) {
            writeLockHeldByTxn.put(variableName, transaction);
        }
    }

    public void addToWaitingQ(Instruction instruction, String variableName) {
        if (waitingInstructions.contains(instruction)) {
            return;
        }
        log.info("{} Add instruction {} to waiting Queue for variable : {}", LOG_TAG, instruction.getInstructionLine(), variableName);
        Queue<Instruction> currentWaitQ = this.instructionsWaitingForVariable.containsKey(variableName) ?
                this.instructionsWaitingForVariable.get(variableName) : new LinkedList<>();
        currentWaitQ.add(instruction);
        waitingInstructions.add(instruction);
        this.instructionsWaitingForVariable.put(variableName, currentWaitQ);
//        this.instructionsWaitingForVariable.put(variableName, currentWaitQ); Update txnsWaitingForVariablSet, might be useful
    }

    public void addToWaitingRoQ(Instruction instruction, String variableName) {
        if (waitingInstructions.contains(instruction)) {
            return;
        }
        log.info("{} Add instruction {} to waiting Read-Only Queue for variable : {}", LOG_TAG, instruction.getInstructionLine(), variableName);
        Set<Instruction> currentWaitQ = this.ROInstructionsWaitingForVariable.containsKey(variableName) ?
                this.ROInstructionsWaitingForVariable.get(variableName) : new HashSet<>();
        currentWaitQ.add(instruction);
        waitingInstructions.add(instruction);
        this.ROInstructionsWaitingForVariable.put(variableName, currentWaitQ);
//        this.instructionsWaitingForVariable.put(variableName, currentWaitQ); Update txnsWaitingForVariablSet, might be useful
    }


    //returns true if all waiting instructions have finished
    //false if some instructions are still left
    public Boolean tryWaitingInstructions() {
        tryReadWriteInstructions();
        tryReadOnlyInstructions();

        return this.waitingInstructions.size() == 0;
    }

    private void tryReadWriteInstructions() {
        for (Map.Entry<String, Queue<Instruction>> entry : this.instructionsWaitingForVariable.entrySet()) {
//            String variableName = entry.getKey();
            Instruction instruction;
            while(entry.getValue().peek() != null) {
                instruction = entry.getValue().peek();
                if (instruction.execute()) {{
                    waitingInstructions.remove(instruction);
                    entry.getValue().remove();
                }} else {
                    break;
                }
            }
        }
    }

    private void tryReadOnlyInstructions() {
        for (Map.Entry<String, Set<Instruction>> entry : this.ROInstructionsWaitingForVariable.entrySet()) {
//            String variableName = entry.getKey();
            List<Instruction> removalSet = new ArrayList<>();
            entry.getValue().forEach(instruction -> {
                if (instruction.execute()) {
                    waitingInstructions.remove(instruction);
                    removalSet.add(instruction);
                }
            });
            entry.getValue().removeAll(removalSet);
        }
    }


}
