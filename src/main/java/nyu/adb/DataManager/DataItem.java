package nyu.adb.DataManager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nyu.adb.Tick;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j @Getter
/**
 * Representation of an individual dataItem on a site managed by the DataManagerImpl
 */
public class DataItem {

    private final String name;
    private Integer value;
    private final boolean isReplicated;
    private final List<VersionedDataItem> versionedDataItems;

//    public Site site;

    public DataItem(String name, Integer value, boolean isReplicated) {
        this.name = name;
        this.value = value;
        this.isReplicated = isReplicated;

        this.versionedDataItems = new ArrayList<>();
        versionedDataItems.add(new VersionedDataItem(value, Tick.getInstance().getTime()));
    }

    @AllArgsConstructor @Getter
    /**
     * The historical values of the dataItem maintained by the individual dataItem itself
     * contains the value and their time of write (commitTime).
     */
    public class VersionedDataItem {
        private Integer value;
        private Integer commitTime;
    }

    /**
     * Compares two {@link VersionedDataItem} based on their commitTimes
     */
    private class VersionedDataItemComparator implements Comparator<VersionedDataItem> {

        public int compare (VersionedDataItem dataItem1, VersionedDataItem dataItem2) {
            if (dataItem1.commitTime > dataItem2.commitTime) {
                return 1;
            } else if (dataItem1.commitTime < dataItem2.commitTime) {
                return -1;
            }
            return 0;
        }
    }

    /**
     *
     * @param time the time for which the committed value is requested
     * @return the version of the dataItem that was committed within the site before the t=time and closest to it.
     */
    public VersionedDataItem getValue(Integer time) {
        Integer index = Collections.binarySearch(versionedDataItems, new VersionedDataItem(0, time), new VersionedDataItemComparator());
        //TODO test this for sanity.
//        log.info("versionedDataItems {}, index {}", versionedDataItems.forEach(value -> {value.getValue().toString() + " : " + value.getCommitTime().toString()}).;, index);
        if (index < 0) {
            return versionedDataItems.get(-1*index - 2);
        } else {
            return versionedDataItems.get(index);
        }
    }

    /**
     * Writes value and creates a version of the dataItem based on current time
     * @param value
     * @return true.
     */
    public boolean writeValue(Integer value) {
        this.value = value;
        versionedDataItems.add(new VersionedDataItem(value, Tick.getInstance().getTime()));
        return true;
    }

};
