package nyu.adb.Transactions;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nyu.adb.Instructions.ExecuteResult;
import nyu.adb.Instructions.Instruction;
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
        this.sitesAccessedForVariable = new HashMap<>();
        this.siteEarliestUpTimeWhenAccessingIt = new HashMap<>();
        startTick = Tick.getInstance().getTime();
        currentStatus = TransactionStatus.RUNNING;
    }

    /**
     * Updates transaction based on a new read made from the database sites
     * @param executeResult the result of read query
     * @param variableName the variable that was read by the instruction
     * @param instructionLine the raw instruction that lead to this read
     */
    public void newRead(ExecuteResult executeResult, String variableName, String instructionLine) {
        localCache.put(variableName, executeResult.getValue());
        instructionsList.put(instructionLine, executeResult);
        updateSiteAccessRecord(executeResult, variableName);
    }

    /**
     * Updates transaction to store a read made from the local cache of the transaction
     * @param instructionLine the raw instruction that lead to this read
     * @param val the value that was read for the instruction
     */
    public void cacheRead(String instructionLine, Integer val) {
        instructionsList.put(instructionLine, new ExecuteResult(null, val, Tick.getInstance().getTime(), null));
    }

    /**
     * Update based on a write instruction. Comes here only if all locks for a given write are acquired
     * @param variableName the variable being written by the instruction
     * @param instructionLine the raw instruction that lead to this acquiring of write lock
     * @param writeVal the value being written by the instruction
     * @param executeResult the result of the acquireLock request
     */
    public void writeToLocalCache(String variableName, String instructionLine, Integer writeVal, ExecuteResult executeResult) {
        dirtyBit.add(variableName);
        localCache.put(variableName, writeVal);
        instructionsList.put(instructionLine, Objects.requireNonNullElseGet(executeResult,
                () -> new ExecuteResult(null, writeVal, Tick.getInstance().getTime(), null)));
        if (executeResult != null) {
            updateSiteAccessRecord(executeResult, variableName);
        }
    }

    /**
     * Updates transaction to have a record of all sites that it accessed for a variable.
     * This is used to check if all sites were up b/w first access and commit at time of endTxnInstruction
     * @param executeResult the result of read/write request. Contains list of sites accessed for the instruction and their up time
     * @param variableName the variable being locked by the instruction.
     */
    private void updateSiteAccessRecord(ExecuteResult executeResult, String variableName) {
        BitSet siteAccessRecord = sitesAccessedForVariable.getOrDefault(variableName, new BitSet(constants.NUM_OF_SITES+1));
        for (Map.Entry<Integer, Integer> entry : executeResult.getSiteNumberAndUpTime().entrySet()) {
            Integer siteNumber = entry.getKey();
            siteAccessRecord.set(siteNumber);
            log.info("Site locked : {}, variable : {}", siteNumber, variableName);
            //If site was already accessed, this means we already have the earliest access time in the map.
            if (!siteEarliestUpTimeWhenAccessingIt.containsKey(siteNumber)) {
                siteEarliestUpTimeWhenAccessingIt.put(siteNumber, entry.getValue());
            }
        }

        sitesAccessedForVariable.put(variableName, siteAccessRecord);
    }
}
