package com.redraysoftware.trevie;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by frano on 03/11/2015.
 */
public class Movie implements Parcelable{
    private String mTitle;
    private String mReleaseDate;
    private String mRating;
    private String mSynopsis;
    private Integer mPosterId;

    public Movie() {
        mTitle = "Jebaka Bujaka";
        mReleaseDate = "2015";
        mRating = "10/10";
        mSynopsis = "Wyjebany film kurwa no";
        mPosterId = R.drawable.temp;
    }

    protected Movie(Parcel in) {
        mTitle = in.readString();
        mReleaseDate = in.readString();
        mRating = in.readString();
        mSynopsis = in.readString();
    }

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
        dest.writeInt(mPosterId);
    }

    public int getPosterId() {
        return mPosterId;
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
}
