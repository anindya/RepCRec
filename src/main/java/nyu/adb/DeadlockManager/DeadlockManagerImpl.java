package nyu.adb.DeadlockManager;

import nyu.adb.Locks.LockTable;

import java.util.List;

public interface DeadlockManagerImpl {
    public DeadlockRecommendation checkDeadlock(List<LockTable> lockTableList);
}
