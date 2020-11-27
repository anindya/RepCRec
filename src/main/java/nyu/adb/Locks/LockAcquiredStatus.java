package nyu.adb.Locks;

import lombok.Getter;

import java.util.Arrays;

public enum LockAcquiredStatus {
    IN_RECOVERY("in_recovery"),
    ACQUIRED("acquired"),
    LOCKED_ALREADY("locked_already"),
    ALL_DOWN("all_down"),
    WAITING("waiting"),
    ALL_DOWN_FOR_RO("all_down_for_ro");

    @Getter
    private final String value;

    LockAcquiredStatus(String value) {
        this.value = value;
    }

    LockAcquiredStatus getLockType(String value) {
        return Arrays.stream(LockAcquiredStatus.class.getEnumConstants())
                .filter(e -> e.getValue().equals(value)).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
