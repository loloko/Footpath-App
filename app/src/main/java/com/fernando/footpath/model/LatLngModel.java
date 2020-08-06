package com.fernando.footpath.model;

import java.io.Serializable;

public class LatLngModel implements Serializable {

    private double lat;
    private double lng;

    public LatLngModel() {
    }

    public LatLngModel(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
