package nyu.adb.Instructions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j @Getter
public enum InstructionType {
    BEGIN("begin"),
    END("end"),
    DUMP("dump"),
    WRITE("W"),
    READ("R"),
    BEGIN_RO("beginRO"),
    FAIL("fail"),
    RECOVER("recover")
    ;

    private final String value;

    InstructionType(String value) {
        this.value = value;
    }

    public static InstructionType getInstructionType(String value) {
        System.out.println(value);
        return Arrays.stream(InstructionType.class.getEnumConstants())
                .filter(e -> e.getValue().equals(value)).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
