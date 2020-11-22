package nyu.adb.Instructions;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.Application;
import nyu.adb.Sites.SiteManager;
import nyu.adb.Transactions.Transaction;

@Slf4j
public class DumpInstruction extends Instruction{
    private Transaction transaction;

    public DumpInstruction(InstructionType instructionType) {
        super(instructionType);
    }

    @Override
    public ExecuteResult execute(SiteManager siteManager) {
        String dump = siteManager.getSitesDump();
        System.out.println(dump);
        return new ExecuteResult(); // TODO make this a success result.
    }
}
