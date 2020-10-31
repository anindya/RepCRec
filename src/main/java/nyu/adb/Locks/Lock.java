package nyu.adb.Locks;

import lombok.Getter;
import nyu.adb.Transactions.Transaction;

@Getter
public class Lock {
    LockType lockType;
    Transaction transaction;

    Lock(LockType lockType, Transaction transaction) {
        this.lockType = lockType;
        this.transaction = transaction;
    }
}
