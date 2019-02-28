package com.uniofsurrey.lorawandashboard.models;

public class DeviceDTO {

    private String deviceName;

    private Float latitude;

    private Float longitude;

    private String region;

    private String pairedDevice;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPairedDevice() {
        return pairedDevice;
    }

    public void setPairedDevice(String pairedDevice) {
        this.pairedDevice = pairedDevice;
    }
}
