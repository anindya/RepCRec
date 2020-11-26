package nyu.adb.Instructions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecoverSiteInstruction extends Instruction{
    private final Integer siteNumber;

    public RecoverSiteInstruction(InstructionType instructionType, Integer siteNumber,
                                  String instructionLine) {
        super(instructionType, instructionLine);
        this.siteNumber = siteNumber; //TODO get Site object from siteManager.
    }

    @Override
    public Boolean execute() {
        Boolean result = siteManager.startRecovery(siteNumber);
        if (result) {
            System.out.format("Site %d in recovery.\n",  this.siteNumber);
        } else {
            log.error("Could not complete recovery operation for {}", this.siteNumber);
        }

        return result;
    }
}
