package com.example.geotourisme.ViewModel;

public class BufferParams {
    public final double lat;
    public final double lng;
    public final double radius;

    public BufferParams(double lat, double lng, double radius) {
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
    }
}
