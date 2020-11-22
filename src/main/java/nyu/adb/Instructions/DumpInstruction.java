package nyu.adb.Instructions;

import nyu.adb.Transactions.Transaction;

public class DumpInstruction extends Instruction{
    private Transaction transaction;

    public DumpInstruction(InstructionType instructionType) {
        super(instructionType);
    }
}
