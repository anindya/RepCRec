package nyu.adb.Instructions;

import nyu.adb.Transactions.Transaction;

public class EndTxnInstruction extends Instruction{
    private Transaction transaction;

    public EndTxnInstruction(InstructionType instructionType, Transaction txn,
                             String instructionLine) {
        super(instructionType, instructionLine);
        this.transaction = txn;
    }
}
