package com.fernando.footpath.model;

import java.io.Serializable;

public class ImageModel implements Serializable {
    private String url;
    private String name;
    private String owner;


    public ImageModel() {
    }

    public ImageModel(String name, String url, String owner) {
        this.name = name;
        this.url = url;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}