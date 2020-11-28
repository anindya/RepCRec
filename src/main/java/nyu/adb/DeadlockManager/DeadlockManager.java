package nyu.adb.DeadlockManager;

import nyu.adb.Locks.LockTable;
import nyu.adb.Transactions.Transaction;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

public interface DeadlockManager {
    public DeadlockRecommendation checkDeadlock(List<Map<String, Map<Transaction, BitSet>>> lockTablesData);
}
