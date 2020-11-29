package nyu.adb.Transactions;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nyu.adb.Instructions.ExecuteResult;
import nyu.adb.Instructions.Instruction;
import nyu.adb.Locks.LockType;
import nyu.adb.Tick;
import nyu.adb.constants;

import java.util.*;

@Slf4j @Getter @Setter
public class Transaction {
    String transactionName;
    TransactionType transactionType;
    Integer startTick;
    Map<String, ExecuteResult> instructionsList;
    Map<String, Integer> localCache;
    Map<String, BitSet> sitesAccessedForVariable;
    Map<Integer, Integer> siteEarliestUpTimeWhenAccessingIt;
    Set<String> dirtyBit; //set if data item has been updated by the transaction.
    Map<String, LockType> locksHeld; //what are the lock-types held by the transaction.

    // Assumes only one instruction per txn at a time. If waiting, no new operation will come in
    Instruction currentInstruction;

    TransactionStatus finalStatus;
    TransactionStatus currentStatus;

    Transaction(String transactionName, TransactionType transactionType) {
        this.transactionName = transactionName;
        this.transactionType = transactionType;
        this.instructionsList = new HashMap<>();
        this.localCache = new HashMap<>();
        this.dirtyBit = new HashSet<>();
        this.sitesAccessedForVariable = new HashMap<>(); //TODO
        this.siteEarliestUpTimeWhenAccessingIt = new HashMap<>();
        startTick = Tick.getInstance().getTime();
        currentStatus = TransactionStatus.RUNNING;

        //TODO fill in dataItemMap
    }

//    void insertNewInstruction(Instruction instruction) {
//        if (currentInstruction.equals(TransactionStatus.WAITING)) {
//            //TODO Return fail, no new instruction should come when waiting. Add logger.
//        }
//    }

    public void newRead(ExecuteResult executeResult, String variableName, String instructionLine) {
        localCache.put(variableName, executeResult.getValue());
        instructionsList.put(instructionLine, executeResult);
        updateSiteAccessRecord(executeResult, variableName);
    }

    public void cacheRead(String variableName, String instructionLine, Integer val) {
        instructionsList.put(instructionLine, new ExecuteResult(null, val, Tick.getInstance().getTime(), null));
    }

    public void writeToLocalCache(String variableName, String instructionLine, Integer writeVal, ExecuteResult executeResult) {
        dirtyBit.add(variableName);
        localCache.put(variableName, writeVal);
        instructionsList.put(instructionLine, Objects.requireNonNullElseGet(executeResult,
                () -> new ExecuteResult(null, writeVal, Tick.getInstance().getTime(), null)));
        if (executeResult != null) {
            updateSiteAccessRecord(executeResult, variableName);
        }
    }

    private void updateSiteAccessRecord(ExecuteResult executeResult, String variableName) {
//        if (this.transactionType.equals(TransactionType.READ_ONLY)) {
//            return;
//        }
        BitSet siteAccessRecord = sitesAccessedForVariable.getOrDefault(variableName, new BitSet(constants.NUM_OF_SITES+1));
        for (Map.Entry<Integer, Integer> entry : executeResult.getSiteNumberAndUpTime().entrySet()) {
            Integer siteNumber = entry.getKey();
            siteAccessRecord.set(siteNumber);
            log.error("Site locked : {}, variable : {}", siteNumber, variableName);
            //If site was already accessed, this means we already have the earliest access time in the map.
            if (!siteEarliestUpTimeWhenAccessingIt.containsKey(siteNumber)) {
                siteEarliestUpTimeWhenAccessingIt.put(siteNumber, entry.getValue());
            }
        }

        sitesAccessedForVariable.put(variableName, siteAccessRecord);
    }

    Boolean abort() {

        return true;
    }
}
