package nyu.adb.Instructions;

import nyu.adb.Transactions.Transaction;
import nyu.adb.Transactions.TransactionType;

/**
 * Class to handle begin instruction from the input. execution creates a new read-write type transaction.
 * Follows format begin(<Transaction name>)
 */
public class BeginTxnInstruction extends Instruction{
    private Transaction transaction;
    private final String txnNumber;
    private final TransactionType transactionType;

    public BeginTxnInstruction(InstructionType instructionType, String txnNumber, TransactionType transactionType,
                               String instructionLine) {
        super(instructionType, instructionLine);
        this.txnNumber = txnNumber;
        this.transactionType = transactionType;

    }

    @Override
    public Boolean execute() {
        this.transaction = transactionManager.createNewTransaction(this.txnNumber, this.transactionType);
        return true;
    }
}
