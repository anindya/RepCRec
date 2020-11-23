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

    public Site(Integer siteNumber) {
        this.siteNumber = siteNumber;
        this.status = SiteStatus.UP;
        this.lastDownTime = 0;
        this.dataManagerImpl = new DataManagerImpl();
    }

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

    public Boolean addDataItem(String name, Integer value, boolean isReplicated) {
        return dataManagerImpl.addDataItem(name, value, isReplicated);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n----------------\nSite Number : ");
        sb.append(siteNumber);
        sb.append("\n Variables");
        sb.append(dataManagerImpl.toString());
        sb.append("\n-----------------");
        return sb.toString();
    }
}
