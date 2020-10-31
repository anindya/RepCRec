package nyu.adb.Instructions;

import lombok.Getter;
import lombok.Setter;
import nyu.adb.Transactions.Transaction;

@Getter @Setter
public class Instruction {
    Transaction transaction;
    InstructionType instructionType;
    String name;
    Integer readValue;
    Integer writeValue; //To be used only if instructionType == WRITE

    public Instruction(Transaction txn, InstructionType instructionType, String name) {
        this.transaction = txn;
        this.instructionType = instructionType;
        this.name = name;
    }
}
