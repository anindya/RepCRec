//Authors : Anindya Chakravarti, Rohan Mahadev

package nyu.adb.Transactions;

import lombok.Getter;

import java.util.Arrays;

/**
 * Different status in which a transaction can go
 */
public enum TransactionStatus {
    COMMIT("commit"),
    ABORT("abort"),
    RUNNING("running")
    ;

    @Getter
    private final String value;

    TransactionStatus(String value) {
        this.value = value;
    }

    TransactionStatus getTransactionStatus(String value) {
        return Arrays.stream(TransactionStatus.class.getEnumConstants())
                .filter(e -> e.getValue().equals(value)).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
