package nyu.adb.Instructions;

import nyu.adb.Transactions.Transaction;
import nyu.adb.Transactions.TransactionType;

public class BeginReadOnlyTxnInstruction extends Instruction{
    private Transaction transaction;

    public BeginReadOnlyTxnInstruction(InstructionType instructionType, String txnNumber, TransactionType transactionType) {
        super(instructionType);
        this.transaction = transactionManager.createNewTransaction(txnNumber, transactionType); //make new transaction maybe?
    }
}
