package nyu.adb.Instructions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nyu.adb.Sites.SiteManager;
import nyu.adb.Transactions.TransactionManager;

@Getter
public class Instruction {

    @NonNull
    private InstructionType instructionType;
    @NonNull
    private String instructionLine;

    public TransactionManager transactionManager = TransactionManager.getInstance();

    public Instruction(InstructionType instructionType,
                       String instructionLine) {
        this.instructionType = instructionType;
        this.instructionLine = instructionLine;
    }

    public ExecuteResult execute(SiteManager s){
        return new ExecuteResult();
    } //TODO update.

    @Override
    public String toString() {
        return this.instructionLine;
    }
}
