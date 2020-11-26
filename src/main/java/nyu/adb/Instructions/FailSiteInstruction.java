package nyu.adb.Instructions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FailSiteInstruction extends Instruction{
    private Integer siteNumber;

    public FailSiteInstruction(InstructionType instructionType, Integer siteNumber,
                               String instructionLine) {
        super(instructionType, instructionLine);
        this.siteNumber = siteNumber; //TODO get Site object from siteManager.
    }

    @Override
    public Boolean execute() {
        Boolean result = siteManager.failSite(siteNumber);
        if (result) {
            System.out.format("Site %d failed.\n",  this.siteNumber);
        } else {
            log.error("Could not complete fail operation for {}", this.siteNumber);
        }

        return result;
    }
}
