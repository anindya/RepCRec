package nyu.adb.DeadlockManager;

import nyu.adb.Locks.LockTable;
import nyu.adb.Transactions.Transaction;

import java.util.List;

public class DFSYoungestAbort implements DeadlockManagerImpl {
    @Override
    public DeadlockRecommendation checkDeadlock(List<LockTable> lockTableList) {

        return new DeadlockRecommendation(false, new Transaction()); //TODO Update transaction to remove NoArgsConstructor.
    }
}
