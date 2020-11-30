package nyu.adb.Instructions;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles fail instructions from the input.
 * Execution fails the given site by changing status @SiteManager
 * Follows format : fail(<sitaName>)
 */
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
