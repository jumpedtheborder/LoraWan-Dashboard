package com.uniofsurrey.lorawandashboard.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.ZonedDateTime;

@Entity
public class Webhook implements Comparable<Webhook> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String appId;

    private String devId;

    private ZonedDateTime dateTime;

    private String direction;

    private String candidateConsist;

    private boolean flag;

    private Long groupId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getCandidateConsist() {
        return candidateConsist;
    }

    public void setCandidateConsist(String candidateConsist) {
        this.candidateConsist = candidateConsist;
    }

    public boolean getFlag() { return flag; }

    public void setFlag(boolean flag) { this.flag = flag; }

    @Override
    public int compareTo(Webhook target) {
        // Sort descending.
        if(target.dateTime.compareTo(this.dateTime) >  0 ){
            return 1;
        }
        else if(target.dateTime.compareTo(this.dateTime) <  0){
            return -1;
        }
        else{
            return 0;
        }
    }

}
