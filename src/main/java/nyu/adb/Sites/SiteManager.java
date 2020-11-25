package nyu.adb.Sites;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.Instructions.ExecuteResult;
import nyu.adb.Locks.LockAcquiredStatus;
import nyu.adb.Locks.LockType;
import nyu.adb.Tick;
import nyu.adb.Transactions.Transaction;
import nyu.adb.constants;

import java.util.*;

@Slf4j
public class SiteManager {
    private static final String LOG_TAG = "SiteManager";
    private static final SiteManager instance = new SiteManager();
    Map<Integer, Site> siteList;
    Map<Site, SiteStatus> sitesStatus;

    //Complete map which contains list of where the corresponding variable exists.
    Map<String, List<Site>> variableLocations;

    public static SiteManager getInstance() {
        return instance;
    }

    Boolean fail(Site s) {
        //erase locktable
        return true;
    }

    Boolean recover(Site s) {

        return true;
    }

    Boolean isUp(Site s) {

        return true;
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
        siteList = new HashMap<>();
        sitesStatus = new HashMap<>();
        variableLocations = new HashMap<>();
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

    public ExecuteResult readVariable(String variableName, Transaction txn) {
        if (!variableLocations.containsKey(variableName)) {
            log.error("{} : invalid variable read, variableName = {}", LOG_TAG, variableName);
            return null;
        } else {
            List<Site> siteList = variableLocations.get(variableName);
            Collections.shuffle(siteList); //randomize access to site x.
            boolean allSitesAreDown = true;
            for (Site site : siteList) {
                if (!sitesStatus.get(site).equals(SiteStatus.DOWN)) {
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

    public ExecuteResult writeVariableLock(String variableName, Transaction txn) {
        if (!variableLocations.containsKey(variableName)) {
            log.error("{} : invalid variable read, variableName = {}", LOG_TAG, variableName);
            return null;
        } else {
            List<Site> siteList = variableLocations.get(variableName);
            Collections.shuffle(siteList); //randomize access to site x.
            List<Site> lockedSites = new ArrayList<>();
            Boolean lockedAllUpSites = true;
            boolean allSitesAreDown = true;
            for (Site site : siteList) {
                if (!sitesStatus.get(site).equals(SiteStatus.DOWN)) {
                    allSitesAreDown = false;
                    if (site.acquireLock(variableName, LockType.WRITE, txn).equals(LockAcquiredStatus.ACQUIRED)) {
                        lockedSites.add(site);
//                        Integer val = site.readDataItem(variableName);
//                        return new ExecuteResult(site.getSiteNumber(), val, Tick.getInstance().getTime(), LockAcquiredStatus.ACQUIRED);
                    } else {
                        lockedAllUpSites = false;
                    }
                }
            }
            if (allSitesAreDown) {
                return new ExecuteResult(null, null, Tick.getInstance().getTime(), LockAcquiredStatus.ALL_DOWN);
            }
            if (lockedAllUpSites) {
                Map<Integer, Integer> siteNumberAndUpTime = new HashMap<>();
                for(Site s: siteList) {
                    siteNumberAndUpTime.put(s.getSiteNumber(), s.getUpSince());
                }
                return new ExecuteResult(siteNumberAndUpTime, null, Tick.getInstance().getTime(), LockAcquiredStatus.ACQUIRED);
            } else {
                //TODO Release locks acquired?
                return new ExecuteResult(null, null, Tick.getInstance().getTime(), LockAcquiredStatus.WAITING);
            }
        }
    }

    public Integer getSiteUpTime(Integer siteNumber) {
        return getSiteFromNumber(siteNumber).getUpSince();
    }

    //Cleanup at all sites that are up and were accessed by this transaction.
    public void cleanUpAtSites(String variableName, BitSet siteNumberSet, Integer newValue, Transaction transaction, Boolean isWrite) {
        siteNumberSet.stream().parallel().forEach(siteNumber -> {
            if (isUp(getSiteFromNumber(siteNumber))) {
                if (isWrite) { //Write where needed
                    getSiteFromNumber(siteNumber).updateDataItem(variableName, newValue);
                }
                //Unlock
                getSiteFromNumber(siteNumber).unlockItemForTransaction(variableName, transaction);
            }
        });
    }
}
