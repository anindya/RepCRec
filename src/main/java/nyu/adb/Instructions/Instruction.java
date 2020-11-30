package nyu.adb.Instructions;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nyu.adb.Sites.SiteManager;
import nyu.adb.Transactions.Transaction;
import nyu.adb.Transactions.TransactionManager;

/**
 * Base class for instructions which contains an abstraction of values used by other instruction types.
 * The individual values of instructions remain private to them for most cases.
 */
@Getter @Slf4j
public class Instruction {

    @NonNull
    protected InstructionType instructionType;
    @NonNull
    protected String instructionLine;

    protected Transaction transaction;

    public TransactionManager transactionManager = TransactionManager.getInstance();
    public SiteManager siteManager = SiteManager.getInstance();

    public Instruction(InstructionType instructionType,
                       String instructionLine) {
        this.instructionType = instructionType;
        this.instructionLine = instructionLine;
    }

    public Boolean execute(){
        return true;
    } //TODO update.

    @Override
    public String toString() {
        return this.instructionLine;
    }
}
