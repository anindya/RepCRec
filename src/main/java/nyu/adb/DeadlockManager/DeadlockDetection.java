//Authors : Anindya Chakravarti, Rohan Mahadev

package nyu.adb.DeadlockManager;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.Instructions.Instruction;
import nyu.adb.Instructions.InstructionType;
import nyu.adb.Locks.LockType;
import nyu.adb.Transactions.Transaction;
import nyu.adb.Transactions.TransactionManager;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

/**
 * Utility class for deadlock detection
 */
@Slf4j
public class DeadlockDetection {
    private static final String LOG_TAG = "DeadlockDetection";

    /**
     *
     * @param lockTablesData : list of map containing all data items at a site and it's corresponding locks.
     * @param instructionsWaitingForVariable : contains a list of instructions waiting for each data item (by it's name)
     *                                       the list is based on queue where first instruction is the earliest instruction waiting.
     * @return Set of all transactions that participate in at least one deadlock cycle.
     */
    public Set<Transaction> findTransactionsInDeadlock(List<Map<String, Map<Transaction, BitSet>>> lockTablesData,
                                                       Map<String, Queue<Instruction>> instructionsWaitingForVariable) {
        TransactionManager transactionManager = TransactionManager.getInstance();
        Graph<Transaction, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        //add all transactions as vertices to the graph
        transactionManager.getTransactionList().values().forEach(transaction -> {
            if (transaction.getFinalStatus() == null) { //Only add the transactions that have not yet committed/aborted.
                graph.addVertex(transaction);
            }
        });

        Instruction currentWaitingInstruction;

        for (String variableName : instructionsWaitingForVariable.keySet()) {
            currentWaitingInstruction = instructionsWaitingForVariable.get(variableName).peek();
            if (currentWaitingInstruction == null || currentWaitingInstruction.getTransaction().getFinalStatus() != null) {
                continue;
            }
            LockType lockToAcquire = lockRequiredBasedOnInstructionType(currentWaitingInstruction);

            //TODO This could be a little quicker by thinking of an alternate data structure, not sure about this though.
            for (Map<String, Map<Transaction, BitSet>> siteLockTableData : lockTablesData) {
                //if there is at least someone who has this site-variable locked, then only this site could contribute to a deadlock.
                if (siteLockTableData.get(variableName) != null && siteLockTableData.get(variableName).size() != 0) {
                    for (Map.Entry<Transaction, BitSet> entry : siteLockTableData.get(variableName).entrySet()) {
                        if (hasConflict(entry.getValue(), lockToAcquire) &&
                                !currentWaitingInstruction.getTransaction().getTransactionName().equals(entry.getKey().getTransactionName())) {
//                            if (entry.getKey().getStartTick())
                            graph.addEdge(currentWaitingInstruction.getTransaction(), entry.getKey());
                        }
                    }
                }
            }
//        });
        }


        for (String variableName : instructionsWaitingForVariable.keySet()) {
            List<Instruction> waitingInstructions = new ArrayList<>(instructionsWaitingForVariable.get(variableName));
            for (int i = 0; i < waitingInstructions.size(); i++) {
                Instruction currentInstruction = waitingInstructions.get(i);

                for (int j = i+1; j < waitingInstructions.size(); j++) {
                    if (hasConflict(currentInstruction, waitingInstructions.get(j)) &&
                            !waitingInstructions.get(j).getTransaction().getTransactionName()
                                    .equals(currentInstruction.getTransaction().getTransactionName())) {
                        graph.addEdge(waitingInstructions.get(j).getTransaction(), currentInstruction.getTransaction());
                    }
                }

            }
        }

        CycleDetector<Transaction, DefaultEdge> cycleDetector = new CycleDetector<>(graph);
        return cycleDetector.findCycles();
    }

    /**
     * The following are utility functions for checking locks and their conflicts.
     * Could be moved to their respective classes.
     */
    private LockType lockRequiredBasedOnInstructionType(Instruction instruction) {
        if (instruction.getInstructionType().equals(InstructionType.READ)) {
            return LockType.READ;
        } else if (instruction.getInstructionType().equals(InstructionType.WRITE)) {
            return LockType.WRITE;
        } else {
            log.error("{} : invalid waiting instruction : {}", LOG_TAG, instruction.getInstructionLine());
            return null;
        }
    }

    private Boolean hasConflict(BitSet lockedBitset, LockType lockToAcquire) {
        if (lockToAcquire.equals(LockType.READ)) {
            return lockedBitset.get(LockType.WRITE.ordinal());
        } else if (lockToAcquire.equals(LockType.WRITE)) {
            return lockedBitset.get(LockType.WRITE.ordinal()) || lockedBitset.get(LockType.READ.ordinal());
        }
        return false;
    }

    private Boolean hasConflict(Instruction currentInstruction, Instruction waitingInstruction) {
        return lockRequiredBasedOnInstructionType(currentInstruction).equals(LockType.WRITE) ||
                lockRequiredBasedOnInstructionType(waitingInstruction).equals(LockType.WRITE);
    }
}
