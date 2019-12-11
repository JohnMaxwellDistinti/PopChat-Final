package com.google.firebase.codelab.friendlychat;

public class Beacon {
    //Create private Note data fields
    private String title;
    private String description;
    private double Latitude;
    private double Longitude;

    //Create a constructor with filled params
    public Beacon(String title, String description, double Latitude, double Longitude) {
        this.title = title;
        this.description = description;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
    }

    //Create a constructor with empty params
    public Beacon() {
        title = "A Generic Location";
        description = null;
        Latitude = -1;
        Longitude = -1;
    }

    //Relevant getters & setters
    @Override
    public String toString() {
        //return this.title + " located at Latitiude:" + this.Latitude + " , Longitude:" + this.Longitude;
        return "Location: " + this.title;
    }

    public String getTitle(){
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getLatitude() {
        return Latitude;
    }

    public double getLongitude(){
        return Longitude;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLatitude(double Latitude){
        this.Latitude = Latitude;
    }

    public void setLongitude(double Longitude){
        this.Longitude = Longitude;
    }
}