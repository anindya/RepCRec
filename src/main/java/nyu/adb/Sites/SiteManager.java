package nyu.adb.Sites;

import java.util.List;

public class SiteManager {
    List<Site> siteList;

    Boolean fail(Site s) {

        return true;
    }

    Boolean recover(Site s) {

        return true;
    }

    Boolean isUp(Site s) {

        return true;
    }

    public Site getSiteNumber(Integer siteNumber) {
        if (siteNumber - 1 > siteList.size()) {

            return null;
        } else {
            return siteList.get(siteNumber - 1);
        }
    }

}
