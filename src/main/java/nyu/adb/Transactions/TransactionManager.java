package nyu.adb.Transactions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nyu.adb.DeadlockManager.DFSYoungestAbort;
import nyu.adb.DeadlockManager.DeadlockManager;
import nyu.adb.DeadlockManager.DeadlockRecommendation;
import nyu.adb.Instructions.Instruction;
import nyu.adb.Sites.SiteManager;

import java.util.*;

/*
Singleton class of txnManager, to be called by instructionManager
 */
@Slf4j @Getter
public class TransactionManager {
    private static final String LOG_TAG = "TransactionManager";
    Map<String, Transaction> transactionList = new HashMap<>();
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
//        this.instructionsWaitingForVariable.put(variableName, currentWaitQ); Update txnsWaitingForVariableSet, might be useful
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
