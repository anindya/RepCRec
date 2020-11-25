package nyu.adb.Locks;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.DataManager.DataItem;
import nyu.adb.Transactions.Transaction;

import java.util.*;

@Slf4j
public class LockTable {
    private static final String LOG_TAG = "LockTable";

    Map<DataItem, Map<Transaction, BitSet>> dataItemLockTypeMap;
    Set<DataItem> isWriteLockedMap;
    Integer siteNumber;

    public LockTable(Integer siteNumber) {
        dataItemLockTypeMap = new HashMap<>();
        isWriteLockedMap = new HashSet<>();
        this.siteNumber = siteNumber;
    }

    public Boolean isLocked(DataItem dataItem) {
        return dataItemLockTypeMap.containsKey(dataItem);
    }

    public Boolean isReadLockedOnly(DataItem dataItem) {
        return dataItemLockTypeMap.containsKey(dataItem) &&
                !isWriteLockedMap.contains(dataItem);
    }

    public Boolean isWriteLocked(DataItem dataItem) {
        return isWriteLockedMap.contains(dataItem);

    }

    //Remove returns null if key is not present or returns the last value.
    public Boolean free(DataItem dataItem) {
        return dataItemLockTypeMap.remove(dataItem) != null;
    }

    //Returns true if granular lock is set by the transaction.
    public Boolean isLockedByTxn(DataItem dataItem, Transaction txn, LockType lockType) {
        if (dataItemLockTypeMap.containsKey((dataItem))) {
            Map<Transaction, BitSet> itemLocks = dataItemLockTypeMap.get(dataItem);
            return itemLocks.containsKey(txn) && itemLocks.get(txn).get(lockType.ordinal());
        }
        return false;
    }

    //TODO Check with dataManager if the lock can be assigned or not.
    //Assumes that the call will only come here once the lock is assignable.
    public Boolean lockItem(DataItem dataItem, LockType lockType, Transaction txn) {
        if (isWriteLocked(dataItem)) {
            return false;
        } else {
            Map<Transaction, BitSet> itemLocks = dataItemLockTypeMap.getOrDefault(dataItem, new HashMap<>());
            if (lockType.equals(LockType.WRITE)) {
                if (itemLocks.size() > 1) { //If more than 1 transactions have locked this item, we cannot assign write lock.
                    return false;
                } else if (itemLocks.size() == 1 &&!itemLocks.containsKey(txn)) { //If 1 transaction has locked this item but it is not this, then too the lock cannot be acquired.
                    return false;
                } else {
                    isWriteLockedMap.add(dataItem);
                }
            }
            BitSet currentTxnBitSet;
            if (itemLocks.containsKey(txn)) {
                currentTxnBitSet = itemLocks.get(txn);
            } else {
                currentTxnBitSet = new BitSet(LockType.values().length);
            }
            currentTxnBitSet.set(lockType.ordinal());
            itemLocks.put(txn, currentTxnBitSet);
            dataItemLockTypeMap.put(dataItem, itemLocks);
            return true;
        }
    }

    public Boolean unlockItem(DataItem dataItem, Transaction txn) {
        if (dataItemLockTypeMap.containsKey(dataItem)) {
            BitSet locksHeldByTxn = null;
            if (dataItemLockTypeMap.get(dataItem).containsKey(txn)) {
                locksHeldByTxn = dataItemLockTypeMap.get(dataItem).get(txn);
                dataItemLockTypeMap.get(dataItem).remove(txn);
            }
            if (locksHeldByTxn.get(LockType.WRITE.ordinal())) {
                isWriteLockedMap.remove(dataItem);
            }
        } else {
            log.info("{} : Item not locked at this site, dataitem : {}", LOG_TAG, dataItem);

        }
        return true;
    }
}
