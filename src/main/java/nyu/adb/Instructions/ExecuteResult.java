package nyu.adb.Instructions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nyu.adb.Locks.LockAcquiredStatus;

import java.util.Map;

@Slf4j @AllArgsConstructor @NoArgsConstructor @Getter
public class ExecuteResult {
    private Map<Integer, Integer> siteNumberAndUpTime;
//    private Integer siteNumber;
    private Integer value;
//    DataItem dataItem;
    private Integer time;
    private LockAcquiredStatus lockAcquiredStatus;
}
