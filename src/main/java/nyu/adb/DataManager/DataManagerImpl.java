package nyu.adb.DataManager;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.Locks.LockAcquiredStatus;
import nyu.adb.Locks.LockTable;
import nyu.adb.Locks.LockType;
import nyu.adb.Transactions.Transaction;

import java.util.*;

//Database simulator
@Slf4j
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
    public Boolean addDataItem(String name, Integer value, boolean isReplicated) {
        DataItem dataItem = new DataItem(name, value, isReplicated);
        dataItemList.add(dataItem);
        dataItemMap.put(name, dataItem);
        return true;
    }

    public Boolean updateDataItem(String name, Integer value) {
        if (!dataItemMap.containsKey(name)) {
            log.error("{} Invalid data item name : {}", LOG_TAG, name);
            return false;
        }

        //Remove this variable from inrecovery
        inRecovery.remove(name);
        return dataItemMap.get(name).writeValue(value);
    }

    public LockAcquiredStatus acquireLock(String variableName, LockType lockType, Transaction txn) {
        DataItem dataItem = dataItemMap.get(variableName);
        if (inRecovery.contains(dataItem.getName()) && lockType.equals(LockType.READ)) {
            return LockAcquiredStatus.IN_RECOVERY;
        } else {
            if(lockTable.lockItem(dataItem, lockType, txn)) {
                return LockAcquiredStatus.ACQUIRED;
            } else {
                return LockAcquiredStatus.WAITING; //TODO correct status would be blocked or wait?
            }
        }
    }

    public void unlockItemForTransaction(String variableName, Transaction txn) {
        if (!dataItemMap.containsKey(variableName)) {
            log.error("{} Invalid data item name : {}", LOG_TAG, variableName);
            return ;
        }
        lockTable.unlockItem(dataItemMap.get(variableName), txn);
    }

    public Integer readDataItem(String name) {
        return dataItemMap.get(name).getValue();
    }

    public Boolean isRecovered() {
        return inRecovery.size() == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Map.Entry<String, DataItem> entry : dataItemMap.entrySet()) {
            sb.append(" ");
            sb.append(entry.getKey());
            sb.append(" = ");
            sb.append((entry.getValue()).getValue());
            sb.append(", ");
        }
        sb.append("\n");
        return sb.toString();
    }
}
