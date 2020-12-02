package nyu.adb.DataManager;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.Locks.LockAcquiredStatus;
import nyu.adb.Locks.LockTable;
import nyu.adb.Locks.LockType;
import nyu.adb.Transactions.Transaction;

import java.util.*;

@Slf4j
/**
 * Database simulator
 * The store for dataItems. Stores the DataItems and talks to the lockTable
 */
public class DataManagerImpl {
    private static final String LOG_TAG = "DataManagerImpl";

    List<DataItem> dataItemList; //Table
    Map<String, DataItem> dataItemMap; // Index
    LockTable lockTable;
    Set<String> inRecovery; //every dataitem which are replicated go here on fail.
    Integer siteNumber;

    public DataManagerImpl(Integer siteNumber) {
        dataItemList = new ArrayList<>();
        dataItemMap = new LinkedHashMap<>();
        lockTable = new LockTable(siteNumber);
        this.inRecovery = new HashSet<>();
        this.siteNumber = siteNumber;
    }

    /**
     * Starts the recovery of the database on this objects site.
     * This is called via the Site object from the SiteManager.
     */
    public void startRecovery() {
        log.info("{} Recover dataManager for site {} started,", LOG_TAG, siteNumber);
        this.lockTable.reset();
        dataItemList.forEach(dataItem -> {
            if (dataItem.isReplicated()) {
                inRecovery.add(dataItem.getName());
            }
        });
    }

    /**
     * Add a new dataItem to the database managed by this manager
     * @param name of the dataItem
     * @param value the value with which the item is initialized
     * @param isReplicated whether the dataItem is replicated or not. This is useful when starting recovery.
     * @return true always, assumes that this will not go down while the add takes place.
     */
    public Boolean addDataItem(String name, Integer value, boolean isReplicated) {
        DataItem dataItem = new DataItem(name, value, isReplicated);
        dataItemList.add(dataItem);
        dataItemMap.put(name, dataItem);
        return true;
    }

    /**
     * Update the dataItem and the remove it from inRecovery set as the item has been written to now.
     * @param name of the dataItem
     * @param value the value with which the item is initialized
     * @return false if dataItem with name doesn't exist on the site.
     * true otherwise
     */
    public Boolean updateDataItem(String name, Integer value) {
        if (!dataItemMap.containsKey(name)) {
            log.error("{} Invalid data item name : {}", LOG_TAG, name);
            return false;
        }

        //Remove this variable from in recovery
        inRecovery.remove(name);
        return dataItemMap.get(name).writeValue(value);
    }

    /**
     * Tries to lock the dataItem for a given transaction based on currentLocks on the dataItem
     * Delegates to lockTable to decide whether locking is possible
     *
     * @param variableName the dataItem by name that needs to be locked
     * @param lockType The lockType requested
     * @param txn the transaction that is requesting the lock
     * @return if the item is replicated and the site is recovery.
     *          Then a read cannot be given and thus, IN_RECOVERY status is sent back
     *        otherwise lockTable decides what happens to the request
     */
    public LockAcquiredStatus acquireLock(String variableName, LockType lockType, Transaction txn) {
        DataItem dataItem = dataItemMap.get(variableName);
        if (inRecovery.contains(dataItem.getName()) && lockType.equals(LockType.READ)) {
            return LockAcquiredStatus.IN_RECOVERY;
        } else {
            if(lockTable.lockItem(dataItem, lockType, txn)) {
                return LockAcquiredStatus.ACQUIRED;
            } else {
                return LockAcquiredStatus.WAITING;
            }
        }
    }

    /**
     * Unlocks the dataItem by name for the given transaction
     * @param variableName the name of the item being released.
     * @param txn that is releasing the lock on the given dataItem.
     */
    public void unlockItemForTransaction(String variableName, Transaction txn) {
        if (!dataItemMap.containsKey(variableName)) {
            log.error("{} Invalid data item name : {}", LOG_TAG, variableName);
            return ;
        }
        lockTable.unlockItem(dataItemMap.get(variableName), txn);
    }

    /**
     *
     * @param name Read the given dataitem by name
     * @return the value
     */
    public Integer readDataItem(String name) {
        return dataItemMap.get(name).getValue();
    }

    /**
     * For READ_ONLY transactions
     * @param name dataItem to read
     * @param time the version of the dataItem to read (same as transaction start time)
     * @return a versionedDataItem object based on the time in the request
     */
    public DataItem.VersionedDataItem readDataItemVersion(String name, Integer time) {
        return dataItemMap.get(name).getValue(time);
    }

    /**
     * @return if all replicated variables have been written to after recovery
     */
    public Boolean isRecovered() {
        return inRecovery.size() == 0;
    }

    /**
     * @return lockData from the lockTable for deadlock detection.
     */
    public Map<String, Map<Transaction, BitSet>> getLocksData() {
        return lockTable.getLocksData();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, DataItem> entry : dataItemMap.entrySet()) {
            sb.append(" ");
            sb.append(entry.getKey());
            sb.append(" = ");
            sb.append((entry.getValue()).getValue());
            sb.append(", ");
        }
        return sb.toString();
    }

}
