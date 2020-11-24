package nyu.adb.Instructions;

import nyu.adb.Transactions.Transaction;

public class ReadInstruction extends Instruction{
    private Transaction transaction;
    private String variableName;
    private Integer readValue;

    public ReadInstruction(InstructionType instructionType, Transaction txn, String variableName,
                           String instructionLine) {
        super(instructionType, instructionLine);
        this.transaction = txn;
        this.variableName = variableName;
    }
}
