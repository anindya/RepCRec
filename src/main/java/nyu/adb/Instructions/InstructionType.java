package nyu.adb.Instructions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * The types of instructions supported by the system
 * CLEAN_UP is an internal type used to denote EOF has come.
 */
@Slf4j @Getter
public enum InstructionType {
    BEGIN("begin"),
    END("end"),
    DUMP("dump"),
    WRITE("W"),
    READ("R"),
    BEGIN_RO("beginRO"),
    FAIL("fail"),
    RECOVER("recover"),
    CLEAN_UP("clean_up")
    ;

    private final String value;

    InstructionType(String value) {
        this.value = value;
    }

    public static InstructionType getInstructionType(String value) {
        return Arrays.stream(InstructionType.class.getEnumConstants())
                .filter(e -> e.getValue().equals(value)).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
