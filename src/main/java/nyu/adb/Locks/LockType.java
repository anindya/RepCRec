//Authors : Anindya Chakravarti, Rohan Mahadev

package nyu.adb.Locks;

import lombok.Getter;

import java.util.Arrays;

/**
 * Lock types supported by the database/datamanager.
 */
public enum LockType {
    READ("read"),
    WRITE("write");

    @Getter
    private final String value;

    LockType(String value) {
        this.value = value;
    }

    LockType getLockType(String value) {
        return Arrays.stream(LockType.class.getEnumConstants())
                .filter(e -> e.getValue().equals(value)).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
