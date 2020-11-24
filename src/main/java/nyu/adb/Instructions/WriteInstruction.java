package nyu.adb.Instructions;

import nyu.adb.Transactions.Transaction;

public class WriteInstruction extends Instruction{
    private Transaction transaction;
    private String variableName;
    private Integer readValue;
    private Integer writeValue; //To be used only if instructionType == WRITE

    public WriteInstruction(InstructionType instructionType, Transaction txn, String variableName, Integer writeValue,
                            String instructionLine) {
        super(instructionType, instructionLine);
        this.transaction = txn;
        this.variableName = variableName;
        this.writeValue = writeValue;
    }
}
