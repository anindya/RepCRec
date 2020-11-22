package nyu.adb.Transactions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nyu.adb.DataManager.DataItem;
import nyu.adb.Instructions.Instruction;
import nyu.adb.Tick;

import java.util.List;
import java.util.Map;

@Slf4j @Getter @Setter @NoArgsConstructor
public class Transaction {
    String transactionName;
    TransactionType transactionType;
    Integer startTick;
    List<Instruction> instructionsList;

    // Assumes only one instruction per txn at a time. If waiting, no new operation will come in
    Instruction currentInstruction;

    Map<DataItem, Integer> dataItemsMap; //dataitem to new value map, read/written by transaction

    TransactionStatus finalStatus;
    TransactionStatus currentStatus;

    Transaction(String transactionName, TransactionType transactionType) {
        this.transactionName = transactionName;
        this.transactionType = transactionType;

        startTick = Tick.getInstance().getTime();
        currentStatus = TransactionStatus.RUNNING;

        //TODO fill in dataItemMap
    }

    void insertNewInstruction(Instruction instruction) {
        if (currentInstruction.equals(TransactionStatus.WAITING)) {
            //TODO Return fail, no new instruction should come when waiting. Add logger.
        }
    }

    Boolean abort() {

        return true;
    }
}
