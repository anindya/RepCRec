package nyu.adb.DataManager;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j @Getter
public class DataItem {

    public String name;
    public Integer value;
    public boolean isLocked;
    public boolean isReplicated;
//    public Site site;

    public DataItem(String name, Integer value, boolean isReplicated) {
        this.name = name;
        this.value = value;
        this.isLocked = false;
        this.isReplicated = isReplicated;
    }
};
