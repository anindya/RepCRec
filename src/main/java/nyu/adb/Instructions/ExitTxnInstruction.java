package nyu.adb.Instructions;

import nyu.adb.Transactions.Transaction;
import nyu.adb.Transactions.TransactionStatus;
import nyu.adb.Transactions.TransactionType;

import java.util.Map;

public class ExitTxnInstruction extends Instruction{
    private final Transaction transaction;

    public ExitTxnInstruction(InstructionType instructionType, Transaction txn,
                              String instructionLine) {
        super(instructionType, instructionLine);
        this.transaction = txn;
    }
}
