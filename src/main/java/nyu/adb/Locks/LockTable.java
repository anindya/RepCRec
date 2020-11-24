package nyu.adb.Locks;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.DataManager.DataItem;
import nyu.adb.Transactions.Transaction;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LockTable {
    private static final String LOG_TAG = "LockTable";

    Map<DataItem, Map<Transaction, LockType>> dataItemLockTypeMap;
    Map<DataItem, Boolean> isWriteLockedMap;
    Integer siteNumber;

    public LockTable(Integer siteNumber) {
        dataItemLockTypeMap = new HashMap<>();
        isWriteLockedMap = new HashMap<>();
        this.siteNumber = siteNumber;
    }

    public Boolean isLocked(DataItem dataItem) {
        return dataItemLockTypeMap.containsKey(dataItem);
    }

    public Boolean isReadLockedOnly(DataItem dataItem) {
        return dataItemLockTypeMap.containsKey(dataItem) &&
                !isWriteLockedMap.containsKey(dataItem);
    }

    public Boolean isWriteLocked(DataItem dataItem) {
        return isWriteLockedMap.containsKey(dataItem);

    }

    //Remove returns null if key is not present or returns the last value.
    public Boolean free(DataItem dataItem) {
        return dataItemLockTypeMap.remove(dataItem) != null;
    }

    //Returns true if granular lock is set by the transaction.
    public Boolean isLockedByTxn(DataItem dataItem, Transaction txn, LockType lockType) {
        if (dataItemLockTypeMap.containsKey((dataItem))) {
            Map<Transaction, LockType> itemLocks = dataItemLockTypeMap.get(dataItem);
            return itemLocks.containsKey(txn) && itemLocks.get(txn).equals(lockType);
        }
        return false;
    }

    //TODO Check with dataManager if the lock can be assigned or not.
    //Assumes that the call will only come here once the lock is assignable.
    public Boolean lockItem(DataItem dataItem, LockType lockType, Transaction txn) {
        if (isWriteLocked(dataItem)) {
            return false;
        } else {
            Map<Transaction, LockType> itemLocks = dataItemLockTypeMap.getOrDefault(dataItem, new HashMap<>());
            if (lockType.equals(LockType.WRITE)) {
                if (itemLocks.size() > 1) { //If more than 1 transactions have locked this item, we cannot assign write lock.
                    return false;
                } else if (itemLocks.size() == 1 &&!itemLocks.containsKey(txn)) { //If 1 transaction has locked this item but it is not this, then too the lock cannot be acquired.
                    return false;
                } else {
                    isWriteLockedMap.put(dataItem, true);
                }
            }
            itemLocks.put(txn, lockType);
            dataItemLockTypeMap.put(dataItem, itemLocks);
            return true;
        }
    }

    public Boolean unlockItem(DataItem dataItem, Transaction txn) {
        if (dataItemLockTypeMap.containsKey(dataItem)) {
            LockType lockTypeHeldByTxn = null;
            if (dataItemLockTypeMap.get(dataItem).containsKey(txn)) {
                lockTypeHeldByTxn = dataItemLockTypeMap.get(dataItem).get(txn);
                dataItemLockTypeMap.get(dataItem).remove(txn);
            }
            if (lockTypeHeldByTxn.equals(LockType.WRITE)) {
                isWriteLockedMap.remove(dataItem);
            }
        } else {
            log.info("{} : Item not locked at this site, dataitem : {}", LOG_TAG, dataItem);

        }
        return true;
    }
}
