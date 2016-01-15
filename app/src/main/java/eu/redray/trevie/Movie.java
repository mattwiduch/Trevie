/*
 * Copyright (C) 2015 Mateusz Widuch. All rights reserved.
 */

package eu.redray.trevie;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

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
    private List<String> mGenres;
    private List<String> mCountries;

    public Movie(String id, String title, String releaseDate, String avgRating, String overview, String posterPath,
                 String runtime, List<String> genres, List<String> countries) {
        mId = id;
        mTitle = title;
        mReleaseDate = releaseDate;
        mRating = avgRating;
        mSynopsis = overview;
        mPosterPath = posterPath;
        mRuntime = runtime;
        mGenres = genres;
        mCountries = countries;
    }

    protected Movie(Parcel in) {
        mId = in.readString();
        mTitle = in.readString();
        mReleaseDate = in.readString();
        mRating = in.readString();
        mSynopsis = in.readString();
        mPosterPath = in.readString();
        mRuntime = in.readString();
        mGenres = in.readArrayList(String.class.getClassLoader());
        mCountries = in.readArrayList(String.class.getClassLoader());
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
        dest.writeList(mGenres);
        dest.writeList(mCountries);
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

    public List<String> getGenres() {
        return mGenres;
    }

    public List<String> getCountries() {
        return mCountries;
    }
}
