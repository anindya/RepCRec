package nyu.adb.Instructions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nyu.adb.Sites.SiteManager;
import nyu.adb.Transactions.TransactionManager;

@Getter @NoArgsConstructor
public class Instruction {

    @NonNull
    private InstructionType instructionType;
    public TransactionManager transactionManager = TransactionManager.getInstance();

    public Instruction(InstructionType instructionType) {
        this.instructionType = instructionType;
    }

    public ExecuteResult execute(SiteManager s){
        return new ExecuteResult();
    }
}
