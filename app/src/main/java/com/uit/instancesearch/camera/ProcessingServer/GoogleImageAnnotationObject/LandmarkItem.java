package com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject;

import android.graphics.Point;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.services.vision.v1.model.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m on 19/01/2017.
 */

public class LandmarkItem implements Parcelable {
    public String description;
    public float score;
    public List<Point> points; // rectangle on image
    public double latitude;
    public double longitude;

    public LandmarkItem() {
        this("",0.1f);
    }

    public LandmarkItem(String description, float score) {
        this.description = description;
        this.score = score;
        points = new ArrayList<>();
        latitude = -1;
        longitude = -1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.description);
        dest.writeFloat(this.score);
        dest.writeTypedList(this.points);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
    }

    protected LandmarkItem(Parcel in) {
        this.description = in.readString();
        this.score = in.readFloat();
        this.points = in.createTypedArrayList(Point.CREATOR);
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

    public static final Parcelable.Creator<LandmarkItem> CREATOR = new Parcelable.Creator<LandmarkItem>() {
        @Override
        public LandmarkItem createFromParcel(Parcel source) {
            return new LandmarkItem(source);
        }

        @Override
        public LandmarkItem[] newArray(int size) {
            return new LandmarkItem[size];
        }
    };
}
