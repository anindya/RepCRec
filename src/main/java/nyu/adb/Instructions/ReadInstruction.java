package nyu.adb.Instructions;

import nyu.adb.Transactions.Transaction;

public class ReadInstruction extends Instruction{
    private Transaction transaction;
    private String variableName;
    private Integer readValue;
    private Integer writeValue; //To be used only if instructionType == WRITE

    public ReadInstruction(InstructionType instructionType, Transaction txn, String variableName) {
        super(instructionType);
        this.transaction = txn;
        this.variableName = variableName;
    }
}
