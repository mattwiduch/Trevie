/*
 * Copyright (C) 2015 Mateusz Widuch. All rights reserved.
 */

package eu.redray.trevie;

import android.os.Parcel;
import android.os.Parcelable;

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
    private String mTitle;
    private String mReleaseDate;
    private String mRating;
    private String mSynopsis;
    private String mPosterPath;

    public Movie(String title, String releaseDate, String avgRating, String overview, String posterPath) {
        mTitle = title;
        mReleaseDate = releaseDate;
        mRating = avgRating;
        mSynopsis = overview;
        mPosterPath = posterPath;
    }

    protected Movie(Parcel in) {
        mTitle = in.readString();
        mReleaseDate = in.readString();
        mRating = in.readString();
        mSynopsis = in.readString();
        mPosterPath = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mReleaseDate);
        dest.writeString(mRating);
        dest.writeString(mSynopsis);
        dest.writeString(mPosterPath);
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
}
