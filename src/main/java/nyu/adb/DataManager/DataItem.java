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
public class DataItem {

    public String name;
    public Integer value;
    public boolean isReplicated;
    private List<VersionedDataItem> versionedDataItems = new ArrayList<VersionedDataItem>();

//    public Site site;

    public DataItem(String name, Integer value, boolean isReplicated) {
        this.name = name;
        this.value = value;
        this.isReplicated = isReplicated;

        this.versionedDataItems = new ArrayList<>();
        versionedDataItems.add(new VersionedDataItem(value, Tick.getInstance().getTime()));
    }

    @AllArgsConstructor @Getter
    private class VersionedDataItem {
        private Integer value;
        private Integer timeWritten;
    }

    private class VersionedDataItemComparator implements Comparator<VersionedDataItem> {

        public int compare (VersionedDataItem dataItem1, VersionedDataItem dataItem2) {
            if (dataItem1.timeWritten > dataItem2.timeWritten) {
                return 1;
            } else if (dataItem1.timeWritten < dataItem2.timeWritten) {
                return -1;
            }
            return 0;
        }
    }

    public Integer getValue(Integer time) {
        Integer index = Collections.binarySearch(versionedDataItems, new VersionedDataItem(0, time), new VersionedDataItemComparator());
        //TODO test this for sanity.
        if (index < 0) {
            return versionedDataItems.get(-1*index - 1).getValue();
        } else {
            return versionedDataItems.get(index).getValue();
        }
    }

    //TODO do we need to check lock status here?
    public boolean writeValue(Integer value) {
        this.value = value;
        versionedDataItems.add(new VersionedDataItem(value, Tick.getInstance().getTime()));
        return true;
    }

};
