package com.example.finalsurmitapp;

public class SOSModel {

    public double lat, lng;
    public long time;

    public SOSModel() {}

    public SOSModel(double lat, double lng, long time) {
        this.lat = lat;
        this.lng = lng;
        this.time = time;
    }
}