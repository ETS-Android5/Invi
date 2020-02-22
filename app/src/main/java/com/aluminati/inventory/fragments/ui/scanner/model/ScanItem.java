package com.aluminati.inventory.fragments.ui.scanner.model;

import java.util.HashMap;
import java.util.Map;

public class ScanItem {
    private String sid;
    private String iid;
    private String idx;

    public ScanItem() {}

    public ScanItem(String sid, String iid, String idx) {
        this.sid = sid;
        this.iid = iid;
        this.idx = idx;
    }

    public String getsid() {
        return sid;
    }

    public void setsid(String sid) {
        this.sid = sid;
    }

    public String getiid() {
        return iid;
    }

    public void setiid(String iid) {
        this.iid = iid;
    }

    public String getidx() {
        return idx;
    }

    public void setidx(String idx) {
        this.idx = idx;
    }

    public Map<String, String> toHashMap() {
        Map<String, String> map = new HashMap<>();

        map.put("sid", sid);
        map.put("iid", iid);
        map.put("idx", idx);

        return map;
    }

    @Override
    public String toString() {
        return "ScanItem{" +
                "sid='" + sid + '\'' +
                ", iid='" + iid + '\'' +
                ", idx='" + idx + '\'' +
                '}';
    }
}
