package nyu.adb.DeadlockManager;

import lombok.Getter;

/**
 * return type used for recommending the transaction to deadlock based on any given deadlock abortion technique.
 * Returns isDeadlock = false, if no deadlock is found.
 */
@Getter
public class DeadlockRecommendation {
    Boolean isDeadlock;
    String txnToKill;

    DeadlockRecommendation(Boolean isDeadlock, String txnToKill) {
        this.isDeadlock = isDeadlock;
        this.txnToKill = txnToKill;
    }

}
