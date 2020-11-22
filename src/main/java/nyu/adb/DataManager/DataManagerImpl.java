package nyu.adb.DataManager;

import nyu.adb.Locks.LockTable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//Database simulator
public class DataManagerImpl {
    List<DataItem> dataItemList; //Table
    Map<String, DataItem> dataItemMap; // Index
    LockTable lockTable;

    public DataManagerImpl() {
        dataItemList = new ArrayList<>();
        dataItemMap = new LinkedHashMap<>();
        lockTable = new LockTable();
    }
    public Boolean addDataItem(String name, Integer value) {
        DataItem dataItem = new DataItem(name, value);
        dataItemList.add(dataItem);
        dataItemMap.put(name, dataItem);
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Map.Entry entry : dataItemMap.entrySet()) {

            sb.append(" ");
            sb.append(entry.getKey());
            sb.append(" = ");
            sb.append(((DataItem)entry.getValue()).getValue());
            sb.append(", ");
        }
        sb.append("\n");
        return sb.toString();
    }
}
