package nyu.adb.Instructions;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nyu.adb.Locks.LockAcquiredStatus;

@Slf4j @AllArgsConstructor @NoArgsConstructor
public class ExecuteResult {
    Integer siteNumber;
    Integer value;
//    DataItem dataItem;
    Integer time;
    LockAcquiredStatus lockAcquiredStatus;
}
