package com.avinashiyer.parkingapp;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by avinashiyer on 10/14/17.
 */

public class ParkMarker {
    private int id;
    private LatLng coord;
    private boolean isEmpty;

    public ParkMarker(int id, LatLng coord, boolean isEmpty){
        this.id = id;
        this.coord = coord;
        this.isEmpty = isEmpty;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LatLng getCoord() {
        return coord;
    }

    public void setCoord(LatLng coord) {
        this.coord = coord;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }
}
