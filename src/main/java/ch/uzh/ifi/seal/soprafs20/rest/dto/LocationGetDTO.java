package ch.uzh.ifi.seal.soprafs20.rest.dto;

import ch.uzh.ifi.seal.soprafs20.constant.LocationType;

import java.util.ArrayList;
import java.util.List;

public class LocationGetDTO {
    private int id;
    private double[] coordinates;
    private double longitude;
    private double latitude;
    private ArrayList<String> additionalInformation;
    private LocationType locationType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<String> getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(ArrayList<String> additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }
}
