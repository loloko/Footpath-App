package com.fernando.footpath.model;

import java.io.Serializable;

public class RateModel implements Serializable {

    private String trackId;
    private float rate;


    public RateModel() {
    }

    public RateModel(String trackId, float rate) {
        this.trackId = trackId;
        this.rate = rate;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }
}