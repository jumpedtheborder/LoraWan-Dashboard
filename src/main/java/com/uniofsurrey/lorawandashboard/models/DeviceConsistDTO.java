package com.uniofsurrey.lorawandashboard.models;

public class DeviceConsistDTO {
    private String deviceName;
    private String formattedTime;
    private String candidateConsist;
    private String direction;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    public String getCandidateConsist() {
        return candidateConsist;
    }

    public void setCandidateConsist(String candidateConsist) {
        this.candidateConsist = candidateConsist;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
