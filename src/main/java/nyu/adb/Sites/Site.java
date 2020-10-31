package nyu.adb.Sites;

import lombok.Getter;
import lombok.Setter;
import nyu.adb.DataManager.DataManagerImpl;

@Getter @Setter
public class Site {
    Integer siteNumber;
    SiteStatus status;
    Integer lastDownTime; // when did the site go down, 0 if never. comes from Tick.getValue()
    DataManagerImpl dataManagerImpl;

    //DataItems
    public Boolean fail() {

        return true;
    }

    public Boolean recover() {

        return true;
    }

    //Return values of all the data items and status, etc.
    public String getDump() {
        StringBuilder sb = new StringBuilder();


        return sb.toString();
    }

    public Boolean addDataItem(String name, Integer value) {
        return dataManagerImpl.addDataItem(name, value);
    }
}
