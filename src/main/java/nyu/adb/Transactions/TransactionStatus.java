package nyu.adb.Transactions;

public enum TransactionStatus {
    COMMIT("commit"),
    ABORT("abort"),
    RUNNING("running"),
    WAITING("waiting")
    ;

    private final String value;

    TransactionStatus(String value) {
        this.value = value;
    }

    TransactionStatus getInstructionType(String value) {
        return TransactionStatus.valueOf(value);
    }
}
