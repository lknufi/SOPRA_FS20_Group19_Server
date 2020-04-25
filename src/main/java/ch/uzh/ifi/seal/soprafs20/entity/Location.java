package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.constant.LocationType;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Location {

    private int id;
    private String address;
    private String[] additionalInformation;
    private double longitude;
    private double latitude;
    private double[] coordinates;
    private LocationType locationType;
    private String picture;
    private int baujahr;
    private String art_txt;
    private String brunnenart_txt;

    public Location(){}

    public void setBrunnenart_txt(String brunnenart_txt) {
        this.brunnenart_txt = brunnenart_txt;
    }

    public void setBaujahr(int baujahr) {
        this.baujahr = baujahr;
    }

    public void setArt_txt(String art_txt) {
        this.art_txt = art_txt;
    }

    public String getBrunnenart_txt() {
        return brunnenart_txt;
    }

    public int getBaujahr() {
        return baujahr;
    }

    public String getArt_txt() {
        return art_txt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String[] getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String[] additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.longitude = coordinates[0];
        this.latitude = coordinates[1];
        this.coordinates = coordinates;
    }


    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }


    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
