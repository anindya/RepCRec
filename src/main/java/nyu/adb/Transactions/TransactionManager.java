//Authors : Anindya Chakravarti, Rohan Mahadev

package nyu.adb.Transactions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nyu.adb.DeadlockManager.DFSYoungestAbort;
import nyu.adb.DeadlockManager.DeadlockManager;
import nyu.adb.DeadlockManager.DeadlockRecommendation;
import nyu.adb.Instructions.Instruction;
import nyu.adb.Sites.SiteManager;

import java.util.*;

/**
 * Singleton class of txnManager, to be called by instructionManager
 * Central proxy to talk to transactions, manage deadlocks, retry waiting instructions.
 */
@Slf4j @Getter
public class TransactionManager {
    private static final String LOG_TAG = "TransactionManager";
    Map<String, Transaction> transactionList;
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

    private TransactionManager() {
        this.transactionList = new HashMap<>();
        this.instructionsWaitingForVariable = new HashMap<>();
        this.ROInstructionsWaitingForVariable = new HashMap<>();
        this.txnsWaitingForVariable = new HashMap<>();
        this.waitingInstructions = new HashSet<>();
    }

    /**
     * Creates a new transactions and adds it to transactionList.
     * Called after begin/beginRO input command
     * @param transactionName the name of new transaction
     * @param transactionType
     * @return the new Transaction object created
     */
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

    /**
     * deadlock check and abort based on Youngest Abort technique
     * @return true if a deadlock was found
     *         false otherwise
     */
    public Boolean checkAndAbortIfDeadlock() {
        //check all transactions and create a graph for checking deadlock.
        //Abort the youngest transaction.
        SiteManager siteManager = SiteManager.getInstance();
        DeadlockManager deadlockManager = new DFSYoungestAbort();
        List<Map<String, Map<Transaction, BitSet>>> lockTablesData = siteManager.getAllLockTables();

        DeadlockRecommendation deadlockRecommendation = deadlockManager.checkDeadlock(lockTablesData, instructionsWaitingForVariable);
        if (deadlockRecommendation.getIsDeadlock()) {
            log.info("{} Found deadlock, aborting : {}", LOG_TAG, deadlockRecommendation.getTxnToKill());
            System.out.format("Found deadlock, aborting transaction : %s\n", deadlockRecommendation.getTxnToKill());
            Transaction txnToKill = transactionList.get(deadlockRecommendation.getTxnToKill());
            siteManager.cleanUpAtSitesAbort(txnToKill);
            return true;
        } else {
            return false;
        }
    }

    //True if terminated
    public Boolean isTerminated(String txnName) {
        return transactionList.containsKey(txnName) && getTransaction(txnName).getFinalStatus() != null;
    }

    /**
     * Finds a transaction from the transactionManager List based on transaction name
     * @param transactionName
     * @return null if transaction with name not found, else returns corresponding transaction object
     */
    public Transaction getTransaction(String transactionName) {
        if (transactionList.containsKey(transactionName)) {
            return transactionList.get(transactionName);
        } else {
            log.error("{} incorrect transaction name in instruction, transactionName : {}", LOG_TAG, transactionName);
            return null; //TODO Change to error if time permits.
        }
    }

    /**
     * Adds instruction of a READ_WRITE type transaction to waiting queue
     * @param instruction Instruction to add to the queue
     * @param variableName variable for which the instruction is responsible
     */
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
    }

    /**
     * Adds instruction of a READ_ONLY type transaction to waiting queue
     * @param instruction Instruction to add to the queue
     * @param variableName variable for which the instruction is responsible
     */
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
    }


    /**
     * @return true if all waiting instructions have finished
     *          false if some instructions are still left
     */
    public Boolean tryWaitingInstructions() {
        tryReadWriteInstructions();
        tryReadOnlyInstructions();

        return this.waitingInstructions.size() == 0;
    }

    /**
     * Retry for instructions form read-write queue
     * Runs for each of the variable in the waitQ
     * If one instruction runs, it's removed from the Q and the next instruction is tried till an execution doesn't go through
     */
    private void tryReadWriteInstructions() {
        for (Map.Entry<String, Queue<Instruction>> entry : this.instructionsWaitingForVariable.entrySet()) {
            Instruction instruction;
            while(entry.getValue().peek() != null) {
                instruction = entry.getValue().peek();
                log.info("Trying {} ", instruction.getInstructionLine());
                if (instruction.getTransaction().getFinalStatus() != null || instruction.execute()) {{
                    waitingInstructions.remove(instruction);
                    entry.getValue().remove();
                }} else {
                    break;
                }
            }
        }
    }

    /**
     * Retry for instructions form read-write queue
     * Runs for all of the waiting Read-Only instructions waiting in the Q
     */
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
