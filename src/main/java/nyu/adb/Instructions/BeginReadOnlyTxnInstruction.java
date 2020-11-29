package nyu.adb.Instructions;

import nyu.adb.Transactions.TransactionType;

public class BeginReadOnlyTxnInstruction extends Instruction{

    private final String txnNumber;
    private final TransactionType transactionType;

    public BeginReadOnlyTxnInstruction(InstructionType instructionType, String txnNumber, TransactionType transactionType,
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
