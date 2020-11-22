package nyu.adb.Instructions;

import nyu.adb.Transactions.Transaction;
import nyu.adb.Transactions.TransactionType;

public class BeginTxnInstruction extends Instruction{
    private Transaction transaction;

    public BeginTxnInstruction(InstructionType instructionType, String txnNumber, TransactionType transactionType) {
        super(instructionType);
        this.transaction = transactionManager.createNewTransaction(txnNumber, transactionType); //make new transaction maybe?
    }
}
