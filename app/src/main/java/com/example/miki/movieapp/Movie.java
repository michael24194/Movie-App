package com.example.miki.movieapp;

import java.util.ArrayList;

/**
 * Created by Miki on 3/25/2016.
 */
public class Movie {
    private String original_title;
    private String overview;
    private String poster_path;
    private float vote_average;
    private String release_date;
    private int id;




    public Movie(String original_title, String overview, String poster_path, float vote_average, String release_date, int id) {
        this.original_title = original_title;
        this.overview = overview;
        this.poster_path = poster_path;
        this.vote_average = vote_average;
        this.release_date = release_date;
        this.id = id;
    }

    public Movie() {
    }

    public String getOriginal_title() {
        return original_title;
    }

    public String getOverview() {
        return overview;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public float getVote_average() {
        return vote_average;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setOriginal_title(String original_title) {
        this.original_title = original_title;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public void setVote_average(float vote_average) {
        this.vote_average = vote_average;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
