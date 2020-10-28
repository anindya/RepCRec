package nyu.adb.Transactions;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j @AllArgsConstructor
public class Transaction {
    TransactionType transactionType;
    Integer startTick;
}
