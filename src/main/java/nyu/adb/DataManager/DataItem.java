package nyu.adb.DataManager;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j @Data
public class DataItem {

    public String name;
    public Integer value;
//    public Boolean isLocked;
//    public Site site;

    public DataItem(String name, Integer value, Boolean isLocked) {
        this.name = name;
        this.value = value;
//        this.isLocked = isLocked;
    }
};
