package nyu.adb.Instructions;

public class RecoverSiteInstruction extends Instruction{
    private String siteNumber;

    public RecoverSiteInstruction(InstructionType instructionType, String siteNumber) {
        super(instructionType);
        this.siteNumber = siteNumber; //TODO get Site object from siteManager.
    }
}
