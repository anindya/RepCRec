package nyu.adb.Sites;

import lombok.Getter;
import lombok.Setter;
import nyu.adb.DataManager.DataItem;
import nyu.adb.DataManager.DataManagerImpl;
import nyu.adb.Locks.LockAcquiredStatus;
import nyu.adb.Locks.LockType;
import nyu.adb.Tick;
import nyu.adb.Transactions.Transaction;

@Getter @Setter
public class Site {
    private Integer siteNumber;
    private SiteStatus status;
    private Integer lastDownTime; // when did the site go down, 0 if never. comes from Tick.getValue()
    private Integer upSince; //updated on recovery. set to -1 when down.
    private DataManagerImpl dataManagerImpl;

    public Site(Integer siteNumber) {
        this.siteNumber = siteNumber;
        this.status = SiteStatus.UP;
        this.lastDownTime = 0;
        this.dataManagerImpl = new DataManagerImpl(siteNumber);
        this.upSince = 0;
    }

    public LockAcquiredStatus acquireLock(String variableName, LockType lockType, Transaction txn) {
        return dataManagerImpl.acquireLock(variableName, lockType, txn);
    }

    public void unlockItemForTransaction(String variableName, Transaction txn) {
        dataManagerImpl.unlockItemForTransaction(variableName, txn);
    }

    //DataItems
//    public Boolean fail() {
//        this.status = SiteStatus.DOWN;
//        this.upSince = -1;
////        dataManagerImpl.fail();
//        return true;
//    }

    public Boolean recover() {
        this.status = SiteStatus.IN_RECOVERY;
        this.upSince = Tick.getInstance().getTime();
        this.dataManagerImpl.startRecovery();
        return true;
    }

    //Return values of all the data items and status, etc.
    public String getDump() {
        StringBuilder sb = new StringBuilder();


        return sb.toString();
    }

    public Boolean addDataItem(String name, Integer value, boolean isReplicated) {
        return dataManagerImpl.addDataItem(name, value, isReplicated);
    }

    public Boolean updateDataItem(String name, Integer value) {
        Boolean updateStatus = dataManagerImpl.updateDataItem(name, value);
        if (this.status.equals(SiteStatus.IN_RECOVERY) && dataManagerImpl.isRecovered()) {
            this.status = SiteStatus.UP;
        }
        return updateStatus;
    }

    public Integer readDataItem(String name) {
        return dataManagerImpl.readDataItem(name);
    }

    public DataItem.VersionedDataItem readDataItemVersion(String name, Transaction transaction) {
        return dataManagerImpl.readDataItemVersion(name, transaction.getStartTick());
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n----------------\nSite Number : ");
        sb.append(siteNumber);
        sb.append("\n Variables");
        sb.append(dataManagerImpl.toString());
        sb.append("\n-----------------");
        return sb.toString();
    }
}
