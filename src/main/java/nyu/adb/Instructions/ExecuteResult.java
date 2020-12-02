//Authors : Anindya Chakravarti, Rohan Mahadev

package nyu.adb.Instructions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nyu.adb.Locks.LockAcquiredStatus;

import java.util.Map;

/**
 * Output of execution of requests.
 * Used for sending output of Read and Write requests only.
 * siteNumberAndUpTime : map of sites accessed for the execution and their up time.
 */
@Slf4j @AllArgsConstructor @NoArgsConstructor @Getter
public class ExecuteResult {
    private Map<Integer, Integer> siteNumberAndUpTime;
//    private Integer siteNumber;
    private Integer value;
//    DataItem dataItem;
    private Integer time;
    private LockAcquiredStatus lockAcquiredStatus;
}
