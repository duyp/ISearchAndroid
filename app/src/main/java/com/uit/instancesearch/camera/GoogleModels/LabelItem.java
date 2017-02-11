package com.uit.instancesearch.camera.GoogleModels;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by m on 20/01/2017.
 */

public class LabelItem implements Parcelable {
    public String description;
    public float score;

    public LabelItem() {
        this("", 0.1f);
    }

    public LabelItem(String description, float score) {
        this.description = description;
        this.score = score;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.description);
        dest.writeFloat(this.score);
    }

    protected LabelItem(Parcel in) {
        this.description = in.readString();
        this.score = in.readFloat();
    }

    public static final Parcelable.Creator<LabelItem> CREATOR = new Parcelable.Creator<LabelItem>() {
        @Override
        public LabelItem createFromParcel(Parcel source) {
            return new LabelItem(source);
        }

        @Override
        public LabelItem[] newArray(int size) {
            return new LabelItem[size];
        }
    };
}