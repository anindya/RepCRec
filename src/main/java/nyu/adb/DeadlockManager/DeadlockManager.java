package nyu.adb.DeadlockManager;

import nyu.adb.Instructions.Instruction;
import nyu.adb.Locks.LockTable;
import nyu.adb.Transactions.Transaction;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public interface DeadlockManager {
    DeadlockRecommendation checkDeadlock(List<Map<String, Map<Transaction, BitSet>>> lockTablesData,
                                                Map<String, Queue<Instruction>> instructionsWaitingForVariable);
}
