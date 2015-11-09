package com.mrunal_sonal.whereabout;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by mrunal on 7/8/15.
 */
public class CurrentlyTracking {
    int ID;
    int counter;
    Friends friend;
    Marker latestLocation;

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Friends getFriend() {
        return friend;
    }

    public void setFriend(Friends friend) {
        this.friend = friend;
    }

    public Marker getLatestLocation() {
        return latestLocation;
    }

    public void setLatestLocation(Marker latestLocation) {
        this.latestLocation = latestLocation;
    }
}