package nyu.adb.Instructions;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.Transactions.Transaction;

@Slf4j
public class DumpInstruction extends Instruction{
    private Transaction transaction;

    public DumpInstruction(InstructionType instructionType,
                           String instructionLine) {
        super(instructionType, instructionLine);
    }

    @Override
    public void execute() {
        String dump = siteManager.getSitesDump();
        System.out.println(dump);
    }
}
