package nyu.adb.Transactions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nyu.adb.Instructions.ExecuteResult;
import nyu.adb.Instructions.Instruction;
import nyu.adb.Locks.LockType;
import nyu.adb.Tick;

import java.util.*;

@Slf4j @Getter @Setter @NoArgsConstructor
public class Transaction {
    String transactionName;
    TransactionType transactionType;
    Integer startTick;
    Map<String, ExecuteResult> instructionsList;
    Map<String, ExecuteResult> localCache;
    Set<Integer> sitesAccessed;
    Map<String, Boolean> dirtyBit; //set if data item has been updated by the transaction.
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
        this.dirtyBit = new HashMap<>();
        this.sitesAccessed = new HashSet<>();
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
        localCache.put(variableName, executeResult);
        instructionsList.put(instructionLine, executeResult);
    }

    public void cacheRead(String variableName, String instructionLine, Integer val) {
        instructionsList.put(instructionLine, new ExecuteResult(0, val, Tick.getInstance().getTime(), null));
    }

    Boolean abort() {

        return true;
    }
}
