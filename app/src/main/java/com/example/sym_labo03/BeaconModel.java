package com.example.sym_labo03;

public class BeaconModel {

    private String uuid;
    private String minor;
    private String major;
    private int rssi;

    public BeaconModel(String uuid, String minor, String major, int rssi) {
        this.uuid = uuid;
        this.minor = minor;
        this.major = major;
        this.rssi = rssi;
    }

    public String getUuid() {
        return uuid;
    }

    public String getMinor() {
        return minor;
    }

    public String getMajor() {
        return major;
    }

    public int getRssi() {
        return rssi;
    }
}
