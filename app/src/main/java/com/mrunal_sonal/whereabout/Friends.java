package com.mrunal_sonal.whereabout;

/**
 * Created by mrunal on 7/2/15.
 */
public class Friends {

    String name;
    String Phonenumber;
    String location;
    boolean requested;
    long id;
    String sharingTime;
    boolean isSharing;

    public String getPhonenumber() {
        return Phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        Phonenumber = phonenumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSharingTime() {
        return sharingTime;
    }

    public void setSharingTime(String sharingTime) {
        this.sharingTime = sharingTime;
    }

    public Boolean getIsSharing() {
        return isSharing;
    }

    public void setIsSharing(Boolean isSharing) {
        this.isSharing = isSharing;
    }

    public Friends() {
        this.isSharing = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isRequested() {
        return requested;
    }

    public void setRequested(boolean requested) {
        this.requested = requested;
    }


}
