package nyu.adb.DeadlockManager;

import nyu.adb.Transactions.Transaction;


public class DeadlockRecommendation {
    Boolean isDeadlock;
    Transaction txnToKill;

    DeadlockRecommendation(Boolean isDeadlock, Transaction txnToKill) {
        this.isDeadlock = isDeadlock;
        this.txnToKill = txnToKill;
    }

}
