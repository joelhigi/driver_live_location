package com.specialops.loctrackerjava.models;

public class Journey {
    private String driverID;
    private String driverName;
    private Double latitude;
    private Double longitude;
    private boolean sharingStatus;
    public Journey(String userID, String name, Double lat, Double lng, boolean newLocationSharingStatus)
    {
        driverID = userID;
        driverName = name;
        latitude = lat;
        longitude = lng;
        sharingStatus = newLocationSharingStatus;
    }

    public String getDriverID()
    {
        return driverID;
    }

    public void setDriverID(String newID)
    {
        driverID = newID;
    }

    public String getDriverName()
    {
        return driverName;
    }

    public void setDriverName(String newName)
    {
        driverName = newName;
    }

    public Double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(Double newLat)
    {
        latitude = newLat;
    }

    public Double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(Double newLng)
    {
        longitude = newLng;
    }

    public boolean isSharingStatus()
    {
        return sharingStatus;
    }

    public void setSharingStatus(boolean newSharingStatus)
    {
        sharingStatus = newSharingStatus;
    }
}
