package com.uniofsurrey.lorawandashboard.config.models;

public class DevicePacket {
    private String applicationID;
    private String applicationName;
    private String deviceName;
    private String devEUI;
    private String[] rxInfo;
    private String data;

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDevEUI() {
        return devEUI;
    }

    public void setDevEUI(String devEUI) {
        this.devEUI = devEUI;
    }

    public String[] getRxInfo() {
        return rxInfo;
    }

    public void setRxInfo(String[] rxInfo) {
        this.rxInfo = rxInfo;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
