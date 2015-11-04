package com.redraysoftware.trevie;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by frano on 03/11/2015.
 */
public class MovieDetails implements Parcelable{
    private String mTitle;
    private String mReleaseDate;
    private String mRating;
    private String mSynopsis;
    private Integer mPosterId;

    public MovieDetails() {
        mTitle = "Jebaka Bujaka";
        mReleaseDate = "2015";
        mRating = "10/10";
        mSynopsis = "Wyjebany film kurwa no";
        mPosterId = R.drawable.temp;
    }

    protected MovieDetails(Parcel in) {
        mTitle = in.readString();
        mReleaseDate = in.readString();
        mRating = in.readString();
        mSynopsis = in.readString();
    }

    public static final Creator<MovieDetails> CREATOR = new Creator<MovieDetails>() {
        @Override
        public MovieDetails createFromParcel(Parcel in) {
            return new MovieDetails(in);
        }

        @Override
        public MovieDetails[] newArray(int size) {
            return new MovieDetails[size];
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
}
