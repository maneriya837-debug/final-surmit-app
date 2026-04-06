package com.example.finalsurmitapp;

public class Shelter {
    private String name, status;
    private double lat, lng;

    public Shelter() {} // Firebase requires empty constructor

    public Shelter(String name, double lat, double lng, String status) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.status = status;
    }

    public String getName() { return name; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public String getStatus() { return status; }
}