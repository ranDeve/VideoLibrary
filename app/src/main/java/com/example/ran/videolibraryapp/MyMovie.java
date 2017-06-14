package com.example.ran.videolibraryapp;

/**
 * Created by Ran on 08/02/2017.
 */

public class MyMovie {
    int id;
    String subject, body, movieUrl, movieBodyId;

    public MyMovie(String subject, String movieUrl, String movieBodyId) {

        this.subject = subject;
        this.movieUrl = movieUrl;
        this.movieBodyId = movieBodyId;
    }

    @Override
    public String toString() {
        return subject;
    }
}
