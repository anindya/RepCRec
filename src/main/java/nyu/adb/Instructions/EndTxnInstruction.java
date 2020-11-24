package nyu.adb.Instructions;

import nyu.adb.Transactions.Transaction;

import java.util.Map;
import java.util.Set;

public class EndTxnInstruction extends Instruction{
    private Transaction transaction;

    public EndTxnInstruction(InstructionType instructionType, Transaction txn,
                             String instructionLine) {
        super(instructionType, instructionLine);
        this.transaction = txn;
    }

    @Override
    public Boolean execute() {
        Map<Integer, Set<String>> sitesAccessed = this.transaction.getSitesAccessed();
        for (Map.Entry<Integer, Set<String>> entry : sitesAccessed.entrySet()) {
            //commit values and clear all locks. First commit or decide if abort in case some site went down from where access was done.
        }

        return true;
    }
}
