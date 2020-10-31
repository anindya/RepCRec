package nyu.adb.Locks;

import nyu.adb.DataManager.DataItem;
import nyu.adb.Transactions.Transaction;

import java.util.HashMap;
import java.util.Map;

public class LockTable {
    Map<DataItem, Lock> dataItemLockTypeMap;

    LockTable() {
        dataItemLockTypeMap = new HashMap<>();
    }

    public Boolean isLocked(DataItem dataItem) {
        return dataItemLockTypeMap.containsKey(dataItem);
    }

    public Boolean isReadLocked(DataItem dataItem) {
        return dataItemLockTypeMap.containsKey(dataItem) &&
                dataItemLockTypeMap.get(dataItem)
                    .getLockType()
                    .equals(LockType.READ);
    }

    public Boolean isWriteLocked(DataItem dataItem) {
        return dataItemLockTypeMap.containsKey(dataItem) &&
                dataItemLockTypeMap.get(dataItem)
                    .getLockType()
                    .equals(LockType.WRITE);
    }

    //Remove returns null if key is not present or returns the last value.
    public Boolean free(DataItem dataItem) {
        return dataItemLockTypeMap.remove(dataItem) != null;
    }

    //Returns true if granular lock is set by the transaction.
    public Boolean isLockedByTxn(DataItem dataItem, Transaction txn, LockType lockType) {
        if (dataItemLockTypeMap.containsKey((dataItem))) {
            Lock lockData = dataItemLockTypeMap.get(dataItem);
            return lockData.getLockType().equals(lockType) && lockData.getTransaction().equals(txn);
        }
        return false;
    }

    //TODO Check with dataManager if the lock can be assigned or not.
    public Boolean lockItem(DataItem item, LockType lockType, Transaction txn) {
        if (isLocked(item)) {
            return false;
        } else {
            dataItemLockTypeMap.put(item, new Lock(lockType, txn));
        }
        return true;
    }
}
