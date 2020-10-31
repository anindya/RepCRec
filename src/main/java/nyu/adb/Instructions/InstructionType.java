package nyu.adb.Instructions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum InstructionType {
    BEGIN("begin"),
    END("end"),
    DUMP("dump"),
    WRITE("W"),
    READ("R"),
    BEGIN_RO("beginRO")
    ;

    private final String value;

    InstructionType(String value) {
        this.value = value;
    }

    InstructionType getInstructionType(String value) {
        return InstructionType.valueOf(value);
    }
}
