package com.fernando.footpath.model;

import android.content.Context;

import com.fernando.footpath.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TypeModel implements Serializable {

    private int id;
    private int icon;
    private String name;

    public TypeModel(int id, int icon, String name) {
        this.id = id;
        this.icon = icon;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<TypeModel> getExerciseTypes(Context c) {
        List<TypeModel> models = new ArrayList<>();

        models.add(new TypeModel(1, R.drawable.ic_walking_32dp, c.getResources().getString(R.string.walking)));
        models.add(new TypeModel(2, R.drawable.ic_running_32dp, c.getResources().getString(R.string.running)));
        models.add(new TypeModel(3, R.drawable.ic_hiking_32dp, c.getResources().getString(R.string.hiking)));

        return models;
    }

    public static List<TypeModel> getTrackTypes(Context c) {
        List<TypeModel> models = new ArrayList<>();

        models.add(new TypeModel(1, R.drawable.ic_point, c.getResources().getString(R.string.point_to_point)));
        models.add(new TypeModel(2, R.drawable.ic_loop, c.getResources().getString(R.string.loop)));

        return models;
    }

    public static int getImageTrackbyId(int id) {
        switch (id) {
            case 1:
                return R.drawable.ic_point;
            case 2:
                return R.drawable.ic_loop;

            default:
                return R.drawable.ic_point;
        }
    }

    public static int getImageActivitybyId(int id) {
        switch (id) {
            case 1:
                return R.drawable.ic_walking_32dp;
            case 2:
                return R.drawable.ic_running_32dp;
            case 3:
                return R.drawable.ic_hiking_32dp;

            default:
                return R.drawable.ic_walking_32dp;
        }
    }
}