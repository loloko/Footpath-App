package com.fernando.footpath.model;

import android.util.Log;

import com.fernando.footpath.config.ConfigFirebase;
import com.fernando.footpath.util.UserFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserModel implements Serializable {
    private static final String TAG = "UserModel";

    private String id;
    private String name;
    private String email;
    private String password;
    private String photo;
    private List<RateModel> rates;
    private List<String> favorites;

    public UserModel() {
    }

    public UserModel(String id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Exclude
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getFavorites() {
        if (favorites == null)
            favorites = new ArrayList<>();

        return favorites;
    }

    public void setFavorites(List<String> favorites) {
        this.favorites = favorites;
    }

    public List<RateModel> getRates() {
        if (rates == null)
            rates = new ArrayList<>();

        return rates;
    }

    public void setRates(List<RateModel> rates) {
        this.rates = rates;
    }

    public void save() {
        try {

            DatabaseReference user = ConfigFirebase.getFirebaseDatabase().child("user").child(getId());

            user.setValue(this);
        } catch (Exception e) {
            Log.e(TAG, "save: " + e.getMessage());
        }
    }

    public void updateInfo() {
        try {

            DatabaseReference firebase = ConfigFirebase.getFirebaseDatabase();

            DatabaseReference userRef = firebase.child("user").child(UserFirebase.getIdUserBase64());

            HashMap<String, Object> map = new HashMap<>();
            map.put("name", getName());
            map.put("photo", getPhoto());
            map.put("email", getEmail());

            userRef.updateChildren(map);
        } catch (Exception e) {
            Log.e(TAG, "updateInfo: " + e.getMessage());
        }
    }

}