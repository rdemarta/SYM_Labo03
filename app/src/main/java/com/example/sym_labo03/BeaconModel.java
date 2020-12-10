package com.example.sym_labo03;

public class BeaconModel {

    private String uuid;
    private String minor;
    private String major;
    private int rssi;

    public BeaconModel(String uuid, String major, String minor, int rssi) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
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
