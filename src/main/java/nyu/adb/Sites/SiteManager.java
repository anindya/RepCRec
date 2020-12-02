//Authors : Anindya Chakravarti, Rohan Mahadev

package nyu.adb.Sites;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.DataManager.DataItem;
import nyu.adb.Instructions.ExecuteResult;
import nyu.adb.Locks.LockAcquiredStatus;
import nyu.adb.Locks.LockType;
import nyu.adb.Tick;
import nyu.adb.Transactions.Transaction;
import nyu.adb.Transactions.TransactionStatus;
import nyu.adb.constants;

import java.util.*;

/**
 * The central class that talks to sites and gets request from instructions and transactionManger.
 * Acts as proxy line between sites and the outside world.
 * Is a singleton class. Assumption, this never fails.
 */
@Slf4j
public class SiteManager {
    private static final String LOG_TAG = "SiteManager";
    private static final SiteManager instance = new SiteManager();
    Map<Integer, Site> siteList;
    Map<Site, SiteStatus> sitesStatus;
    Map<Site, List<Integer>> siteDownTimeList;

    //Complete map which contains list of where the corresponding variable exists.
    //This can be replaced with a Hash function if the number of dataitems is large.
    Map<String, List<Site>> variableLocations;

    public static SiteManager getInstance() {
        return instance;
    }

    Boolean isUp(Site site) {
        return !sitesStatus.get(site).equals(SiteStatus.DOWN);
    }

    public Boolean isUp(Integer siteNumber) {
        return isUp(getSiteFromNumber(siteNumber));
    }

    public Site getSiteFromNumber(Integer siteNumber) {
        if (siteNumber  > constants.NUM_OF_SITES) {
            return null;
        } else {
            return siteList.get(siteNumber);
        }
    }

    private SiteManager() {
        log.info("Initializing SiteManager : Start.");
        this.siteList = new HashMap<>();
        this.sitesStatus = new HashMap<>();
        this.variableLocations = new HashMap<>();
        this.siteDownTimeList = new HashMap<>();
        String variableName;
        for (int siteNumber = 1; siteNumber <= constants.NUM_OF_SITES; siteNumber++) {
            Site s = new Site(siteNumber);

            sitesStatus.put(s, SiteStatus.UP);
            siteList.put(siteNumber, s);

            //values start from 1.
            for (int variableNumber = 1; variableNumber < constants.variables.length; variableNumber++) {
                variableName = constants.variables[variableNumber];
                if (variableNumber%2 == 0) {
                    s.addDataItem(variableName, 10*variableNumber, true);
                    addVariableSiteMapping(variableName, s);
                } else {
                    if ((1 + variableNumber%10) == siteNumber) {
                        s.addDataItem(variableName, 10*variableNumber, false);
                        addVariableSiteMapping(variableName, s);
                    }
                }
            }
        }
        log.info("Initializing SiteManager : Complete.");
    }

    private void addVariableSiteMapping(String variableName, Site s) {
        List<Site> currentList = variableLocations.getOrDefault(variableName, new ArrayList<>());
        currentList.add(s);
        variableLocations.put(variableName, currentList);
    }

    public String getSitesDump(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Map.Entry entry : siteList.entrySet()) {
            sb.append(entry.getValue());
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Acquires a read lock from one of the sites where the variable is present
     * And then read the corresponding value with site details.
     * @param variableName : dataItem name that is to be read by a read-write transaction
     * @param txn : read-write type transaction
     * @return ExecuteResult type object which contains site information from which(if) the variable is read and the read value.
     * return LockAcquiredStatus = ALL_DOWN if all sites containing a variable are down
     * else returns LockAcquiredStatus = WAITING
     *
     * returns null if variable is not present at any site
     */
    public ExecuteResult readVariable(String variableName, Transaction txn) {
        if (!variableLocations.containsKey(variableName)) {
            log.error("{} : invalid variable read, variableName = {}", LOG_TAG, variableName);
            return null;
        } else {
            List<Site> siteList = variableLocations.get(variableName);
            Collections.shuffle(siteList); //randomize access to site x.
            boolean allSitesAreDown = true;
            for (Site site : siteList) {
                if ( isUp(site)) {
                    allSitesAreDown = false;
                    if (site.acquireLock(variableName, LockType.READ, txn).equals(LockAcquiredStatus.ACQUIRED)) {
                        Integer val = site.readDataItem(variableName);
                        Map<Integer, Integer> siteNumberAndUpSince = new HashMap<>();
                        siteNumberAndUpSince.put(site.getSiteNumber(), site.getUpSince());
                        return new ExecuteResult(siteNumberAndUpSince, val, Tick.getInstance().getTime(), LockAcquiredStatus.ACQUIRED);
                    }
                }
            }
            if (allSitesAreDown) {
                return new ExecuteResult(null, null, Tick.getInstance().getTime(), LockAcquiredStatus.ALL_DOWN);
            }
            //Get locks
            //If all sites are down, change the correct txn to waiting
            //When does a transaction go into blocking stage
            //if lock acquired,
        }
        return new ExecuteResult(null, null, Tick.getInstance().getTime(), LockAcquiredStatus.WAITING); //TODO Change this.
    }

    /**
     * function to acquire lock at all available sites for the given data item
     * @param variableName : dataItem name that is to be written by a read-write transaction
     * @param txn : read-write type transaction
     * @return ExecuteResult type object which contains site information from which(if) the variable is read and the read value.
     * return LockAcquiredStatus = ALL_DOWN if all sites containing a variable are down
     * else returns LockAcquiredStatus = WAITING having acquired some of the locks. This might lead to a deadlock down the line but
     * aggressive deadlock abortion is done and ensures that transactions go through anyway.
     * returns null if variable is not present at any site
     */
    public ExecuteResult writeVariableLock(String variableName, Transaction txn) {
        if (!variableLocations.containsKey(variableName)) {
            log.error("{} : invalid variable read, variableName = {}", LOG_TAG, variableName);
            return null;
        } else {
            List<Site> siteList = variableLocations.get(variableName);
            List<Site> lockedSites = new ArrayList<>();
            Boolean lockedAllUpSites = true;
            Boolean allSitesAreDown = true;
            for (Site site : siteList) {
                if (!sitesStatus.get(site).equals(SiteStatus.DOWN)) {
                    allSitesAreDown = false;
                    if (site.acquireLock(variableName, LockType.WRITE, txn).equals(LockAcquiredStatus.ACQUIRED)) {
                        lockedSites.add(site);
//                        Integer val = site.readDataItem(variableName);
//                        return new ExecuteResult(site.getSiteNumber(), val, Tick.getInstance().getTime(), LockAcquiredStatus.ACQUIRED);
                    } else {
                        log.error("{} Could not get lock for {} from site {}", LOG_TAG, variableName, site.getSiteNumber());
                        lockedAllUpSites = false;
                    }
                }
            }
            if (allSitesAreDown) {
                return new ExecuteResult(null, null, Tick.getInstance().getTime(), LockAcquiredStatus.ALL_DOWN);
            }
            if (lockedAllUpSites) {
                Map<Integer, Integer> siteNumberAndUpTime = new HashMap<>();
                for(Site s: lockedSites) {
//                    log.error("s : {},  variable : {}", s, variableName);
                    siteNumberAndUpTime.put(s.getSiteNumber(), s.getUpSince());
                }
                return new ExecuteResult(siteNumberAndUpTime, null, Tick.getInstance().getTime(), LockAcquiredStatus.ACQUIRED);
            } else {
                //TODO Release locks acquired?
                return new ExecuteResult(null, null, Tick.getInstance().getTime(), LockAcquiredStatus.WAITING);
            }
        }
    }

    /**
     * Reads a particular version of the dataItem based on the transaction start time
     * @param variableName : dataItem name that is to be read by a read-only transaction
     * @param transaction : read-only type transaction
     * @return if all sites are down, sends the ExecuteResult.LockAcquiredStatus = WAITING
     * if all sites are up but none have the correct version committed based on time of start of read-only transaction,
     *              then ExecuteResult.LockAcquiredStatus = ALL_DOWN_FOR_RO, leads to aggressive abort of read-only transaction
     * returns value under ExecuteResult if some site has the relevant information
     * if some sites are down and the information is not present at any of the up sites, ExecuteResult.LockAcquiredStatus = WAITING is returned
     */
    public ExecuteResult readVariableVersion(String variableName, Transaction transaction) {
        List<Site> siteList = variableLocations.get(variableName);

        boolean allSitesAreDown = true;
        boolean someSitesDown = false;
        Map<Integer, Integer> siteNumberAndUpTime = new HashMap<>();
        for (Site site : siteList) {
            if (!sitesStatus.get(site).equals(SiteStatus.DOWN)) {
                allSitesAreDown = false;
                DataItem.VersionedDataItem dataItemVersion = site.readDataItemVersion(variableName, transaction);

                //find down time such that the downtime is right before or on transaction start time
                if (siteDownTimeList.containsKey(site)) {
                    List<Integer> thisSiteDownTimeList = this.siteDownTimeList.get(site);
                    Integer downTimeIndex = Collections.binarySearch(thisSiteDownTimeList, transaction.getStartTick());
                    if (downTimeIndex < 0) {
                        downTimeIndex = -1 * downTimeIndex - 2;
                    }

                    //if site down after commit or on commit time, then ignore this site
                    if (thisSiteDownTimeList.get(downTimeIndex) >= dataItemVersion.getCommitTime()) {
                        log.info("{} Site {} was down between commit time {} for variable: {} and Readonly start time : {} for transaction : {}",
                                LOG_TAG, site.getSiteNumber(), dataItemVersion.getCommitTime(), variableName, transaction.getStartTick(), transaction.getTransactionName());
                        continue;
                    } else {
                        siteNumberAndUpTime.put(site.getSiteNumber(), site.getUpSince());
                        return new ExecuteResult(siteNumberAndUpTime, dataItemVersion.getValue(), Tick.getInstance().getTime(), LockAcquiredStatus.ACQUIRED);
                    }
                } else {
                    siteNumberAndUpTime.put(site.getSiteNumber(), site.getUpSince());
                    return new ExecuteResult(siteNumberAndUpTime, dataItemVersion.getValue(), Tick.getInstance().getTime(), LockAcquiredStatus.ACQUIRED);
                }
            } else {
                someSitesDown = true;
            }
        }
        if (allSitesAreDown) {
            return new ExecuteResult(null, null, Tick.getInstance().getTime(), LockAcquiredStatus.ALL_DOWN);
        } else if (someSitesDown){
            return new ExecuteResult(null, null, Tick.getInstance().getTime(), LockAcquiredStatus.WAITING);
        } else {
            return new ExecuteResult(null, null, Tick.getInstance().getTime(), LockAcquiredStatus.ALL_DOWN_FOR_RO);
        }
    }

    public Integer getSiteUpTime(Integer siteNumber) {
        return getSiteFromNumber(siteNumber).getUpSince();
    }


    //TODO Consider consolidating updates for a site and sending them in a batch
    /**
     * Cleanup at all sites that are up and were accessed by this transaction.
     * Goes to each site which was accessed by the transaction and unlocks the dataItems. Writes any new value, if required, before unlocking
     * This can run in parallel because for each variable a site can be hit exactly once.
     * @param variableName the dataItem name to be unlocked by the transaction
     * @param siteNumberSet A bitset of the site numbers accessed by the transaction for the variable in question
     * @param newValue the new value of the variable that is being written (used only if isWrite = true)
     * @param transaction transaction that is being committed/aborted
     * @param isWrite : flag to allow write when cleaning up
     */
    public void cleanUpAtSites(String variableName, BitSet siteNumberSet, Integer newValue, Transaction transaction, Boolean isWrite) {
        siteNumberSet.stream().forEach(siteNumber -> {
            Site site = getSiteFromNumber(siteNumber);
            if (isUp(site)) {
                if (isWrite) { //Write where needed
                    site.updateDataItem(variableName, newValue);
                    this.sitesStatus.put(site, site.getStatus()); //update siteStatus after write to maintain correct values.
                }
                //Unlock
                site.unlockItemForTransaction(variableName, transaction);
            }
        });
    }

    /**
     * Cleanup at all sites that are up and were accessed by this transaction.
     * Uses the function cleanUpAtSites with isWrite set to false because ABORT is taking place
     */
    public void cleanUpAtSitesAbort(Transaction transaction) {
        for (String variableName : transaction.getLocalCache().keySet()) {
            cleanUpAtSites(variableName, transaction.getSitesAccessedForVariable().get(variableName),
                    transaction.getLocalCache().get(variableName), transaction, false);
        }
        System.out.format("%s aborts.\n", transaction.getTransactionName());
        transaction.setFinalStatus(TransactionStatus.ABORT);
    }

    /**
     * Used to fail a given site,
     * the only update made is at the siteManager because the site would not know that it has failed.
     * @param siteNumber the site that is going to fail
     * @return true. Assumption : siteManager never fails and is a singleton so no one else can fail the Site
     */
    public Boolean failSite(Integer siteNumber) {
        if (!siteList.containsKey(siteNumber)) {
            log.error("{} Invalid siteNumber {} for fail operation", LOG_TAG, siteNumber);
            return false;
        }
        Site site = getSiteFromNumber(siteNumber);
        List<Integer> currentDownTimeList = siteDownTimeList.getOrDefault(site, new ArrayList<>());
        currentDownTimeList.add(Tick.getInstance().getTime());
        siteDownTimeList.put(site, currentDownTimeList);
        sitesStatus.put(site, SiteStatus.DOWN);
        return true;
    }

    /**
     * Used to recover a given site,
     * SiteManager updates it's own records and lets the site know that it needs to reset it's LockTable.
     * @param siteNumber the site that is going to fail
     * @return true. Assumption : siteManager never fails and is a singleton so no one else can fail the Site
     */
    public Boolean startRecovery(Integer siteNumber) {
        if (!siteList.containsKey(siteNumber)) {
            log.error("{} Invalid siteNumber {} for recovery operation", LOG_TAG, siteNumber);
            return false;
        }
        sitesStatus.put(getSiteFromNumber(siteNumber), SiteStatus.IN_RECOVERY);
        getSiteFromNumber(siteNumber).recover();
        return true;
    }

    /**
     * Utility function for deadlock detection
     * @return a list of each sites lock table
     */
    public List<Map<String, Map<Transaction, BitSet>>> getAllLockTables() {
        List<Map<String, Map<Transaction, BitSet>>> lockTablesData = new ArrayList<>();
        siteList.values().forEach(site -> {
            if (isUp(site)) {
                lockTablesData.add(site.getLocksData());
            }
        });
        return lockTablesData;
    }
}
