package nyu.adb.Sites;

import lombok.extern.slf4j.Slf4j;
import nyu.adb.constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SiteManager {
    Map<Integer, Site> siteList;

    //Complete map which contains list of where the corresponding variable exists.
    Map<String, List<Site>> variableLocations;
    Boolean fail(Site s) {
        //erase locktable
        return true;
    }

    Boolean recover(Site s) {

        return true;
    }

    Boolean isUp(Site s) {

        return true;
    }

    public Site getSiteNumber(Integer siteNumber) {
        if (siteNumber  > constants.NUM_OF_SITES) {
            return null;
        } else {
            return siteList.get(siteNumber);
        }
    }

    public SiteManager() {
        log.info("Initializing SiteManager : Start.");
        siteList = new HashMap<>();
        variableLocations = new HashMap<>();
        String variableName;
        for (int siteNumber = 1; siteNumber <= constants.NUM_OF_SITES; siteNumber++) {
            Site s = new Site(siteNumber);
            siteList.put(siteNumber, s);

            //values start from 1.
            for (int variableNumber = 1; variableNumber < constants.variables.length; variableNumber++) {
                variableName = constants.variables[variableNumber];
                if (variableNumber%2 == 0) {
                    s.addDataItem(variableName, 10*variableNumber, true);
                    addVariableSiteMapping(variableName, s);
                } else {
                    if ((1 + variableNumber%10) == siteNumber) {
                        s.addDataItem(variableName, 10*variableNumber, false);
                        addVariableSiteMapping(variableName, s);
                    }
                }
            }
        }
        log.info("Initializing SiteManager : Complete.");
    }

    private void addVariableSiteMapping(String variableName, Site s) {
        List<Site> currentList = variableLocations.getOrDefault(variableName, new ArrayList<>());
        currentList.add(s);
        variableLocations.put(variableName, currentList);
    }

    public String getSitesDump(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Map.Entry entry : siteList.entrySet()) {
            sb.append(entry.getValue());
        }
        sb.append("\n");
        return sb.toString();
    }
}
