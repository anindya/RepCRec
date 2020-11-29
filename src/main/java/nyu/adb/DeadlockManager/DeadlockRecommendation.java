package nyu.adb.DeadlockManager;

import lombok.Getter;

@Getter
public class DeadlockRecommendation {
    Boolean isDeadlock;
    String txnToKill;

    DeadlockRecommendation(Boolean isDeadlock, String txnToKill) {
        this.isDeadlock = isDeadlock;
        this.txnToKill = txnToKill;
    }

}
