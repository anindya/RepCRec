package nyu.adb.Instructions;

public class FailSiteInstruction extends Instruction{
    private String siteNumber;

    public FailSiteInstruction(InstructionType instructionType, String siteNumber,
                               String instructionLine) {
        super(instructionType, instructionLine);
        this.siteNumber = siteNumber; //TODO get Site object from siteManager.
    }
}
