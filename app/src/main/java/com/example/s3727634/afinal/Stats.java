package com.example.s3727634.afinal;

public class Stats {
    private String date;
    private String words;
    private String steps;
    private String locations;

    public Stats(String date, String words, String steps, String locations) {
        this.date = date;
        this.words = words;
        this.steps = steps;
        this.locations = locations;
    }

    public String getDate() {
        return date;
    }

    public String getWords() {
        return words;
    }

    public String getSteps() {
        return steps;
    }

    public String getLocations() {
        return locations;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public void setLocations(String locations) {
        this.locations = locations;
    }
}

