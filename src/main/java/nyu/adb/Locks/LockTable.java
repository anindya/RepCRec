package nyu.adb.Locks;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.DataManager.DataItem;
import nyu.adb.Transactions.Transaction;

import java.util.*;

/**
 * LockTable class.
 * Used by DataManagerImpl to manage locks on dataItems present on the site.
 */
@Slf4j
public class LockTable {
    private static final String LOG_TAG = "LockTable";

    //If ordinal 1 in bitset is set, => write locked
    // ordinal 0 => read locked.
    Map<DataItem, Map<Transaction, BitSet>> dataItemLockTypeMap;
    Set<DataItem> isWriteLockedMap;
    Integer siteNumber;

    public LockTable(Integer siteNumber) {
        dataItemLockTypeMap = new HashMap<>();
        isWriteLockedMap = new HashSet<>();
        this.siteNumber = siteNumber;
    }

    /**
     * Resets lockTable data except the siteNumber.
     * This takes place on recovery call for the Site.
     */
    public void reset() {
        this.dataItemLockTypeMap = new HashMap<>();
        this.isWriteLockedMap = new HashSet<>();
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
            return isLockedByTxn(dataItem, txn, LockType.WRITE);
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

    /**
     * Unlock a data item from a given transaction.
     * If it is write locked, then update the writeLockedMap as well
     * @param dataItem The dataitem to unlock from the transaction
     * @param txn The transaction which on abort/commit is unlocking the dataItems
     * @return true, assumes this doesn't go down between request processing.
     */
    public Boolean unlockItem(DataItem dataItem, Transaction txn) {
        if (dataItemLockTypeMap.containsKey(dataItem)) {
            if (dataItemLockTypeMap.get(dataItem).containsKey(txn)) {
                //check if current lock is write type
                if (dataItemLockTypeMap.get(dataItem).get(txn).get(LockType.WRITE.ordinal())) {
                    isWriteLockedMap.remove(dataItem);
                }
                log.info("Unlock variable : {}, txb : {}", dataItem.getName(), txn.getTransactionName());
                Map<Transaction, BitSet> lockMap = dataItemLockTypeMap.get(dataItem);
                lockMap.remove(txn);
                dataItemLockTypeMap.put(dataItem, lockMap);
            }

        } else {
            log.info("{} : Item not locked at this site, dataitem : {}", LOG_TAG, dataItem);

        }
        return true;
    }

    /**
     *
     * @return lockData stored by the locktable for different dataItems
     */
    public Map<String, Map<Transaction, BitSet>> getLocksData() {
        Map<String, Map<Transaction, BitSet>> resultMap = new HashMap<>();
        dataItemLockTypeMap.entrySet().parallelStream().forEach( entry -> {
            resultMap.put(entry.getKey().getName(), Collections.unmodifiableMap(entry.getValue()));
        });
        return resultMap;
    }
}
