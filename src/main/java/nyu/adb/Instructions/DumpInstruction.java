package nyu.adb.Instructions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DumpInstruction extends Instruction{

    public DumpInstruction(InstructionType instructionType,
                           String instructionLine) {
        super(instructionType, instructionLine);
    }

    @Override
    public Boolean execute() {
        String dump = siteManager.getSitesDump();
        System.out.println(dump);
        return true;
    }
}
