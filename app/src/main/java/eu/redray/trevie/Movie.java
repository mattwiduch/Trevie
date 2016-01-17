/*
 * Copyright (C) 2015 Mateusz Widuch. All rights reserved.
 */

package eu.redray.trevie;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Represents movie item.
 */
public class Movie implements Parcelable {

    public static final String EXTRA_DETAILS = "movie_details";
    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    private String mId;
    private String mTitle;
    private String mReleaseDate;
    private String mRating;
    private String mSynopsis;
    private String mPosterPath;
    private String mRuntime;
    private String mGenres;
    private String mCountries;
    private ArrayList mTrailerLinks;
    private ArrayList mUserReviews;

    public Movie(String id, String title, String releaseDate, String avgRating, String overview, String posterPath,
                 String runtime, String genres, String countries, ArrayList trailers, ArrayList reviews) {
        mId = id;
        mTitle = title;
        mReleaseDate = releaseDate;
        mRating = avgRating;
        mSynopsis = overview;
        mPosterPath = posterPath;
        mRuntime = runtime;
        mGenres = genres;
        mCountries = countries;
        mTrailerLinks = trailers;
        mUserReviews = reviews;
    }

    protected Movie(Parcel in) {
        mId = in.readString();
        mTitle = in.readString();
        mReleaseDate = in.readString();
        mRating = in.readString();
        mSynopsis = in.readString();
        mPosterPath = in.readString();
        mRuntime = in.readString();
        mGenres = in.readString();
        mCountries = in.readString();
        mTrailerLinks = in.readArrayList(Uri.class.getClassLoader());
        mUserReviews = in.readArrayList(String.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mTitle);
        dest.writeString(mReleaseDate);
        dest.writeString(mRating);
        dest.writeString(mSynopsis);
        dest.writeString(mPosterPath);
        dest.writeString(mRuntime);
        dest.writeString(mGenres);
        dest.writeString(mCountries);
        dest.writeList(mTrailerLinks);
        dest.writeList(mUserReviews);
    }

    public String getId() {
        return mId;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public String getRating() {
        return mRating;
    }

    public String getSynopsis() {
        return mSynopsis;
    }

    public String getReleaseYear() {
        return mReleaseDate.substring(0, 4);
    }

    public String getRuntime() {
        return mRuntime;
    }

    public void setTrailerLinks(ArrayList trailerLinks) {
        mTrailerLinks = trailerLinks;
    }

    public void setCountries(String countries) {
        mCountries = countries;
    }

    public void setGenres(String genres) {
        mGenres = genres;
    }

    public void setRuntime(String runtime) {
        mRuntime = runtime;
    }

    public String getGenres() {
        return mGenres;
    }

    public String getCountries() {
        return mCountries;
    }

    public ArrayList getTrailerLinks() {
        return mTrailerLinks;
    }

    public ArrayList getUserReviews() {
        return mUserReviews;
    }

    public void setUserReviews(ArrayList userReviews) {
        mUserReviews = userReviews;
    }
}
