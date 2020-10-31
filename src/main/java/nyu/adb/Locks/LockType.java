package nyu.adb.Locks;

public enum LockType {
    READ("read"),
    WRITE("write");

    private final String value;

    LockType(String value) {
        this.value = value;
    }

    LockType getInstructionType(String value) {
        return LockType.valueOf(value);
    }
}
