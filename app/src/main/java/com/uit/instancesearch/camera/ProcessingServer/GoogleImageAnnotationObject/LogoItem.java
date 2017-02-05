package com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m on 22/01/2017.
 */

public class LogoItem implements Parcelable {

    public static final String NAME_LOGO_JSON = "logoAnnotations";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_SCORE = "score";
    public static final String KEY_BOUNDING_POLY = "boundingPoly";
    public static final String KEY_VERTICES = "vertices";

    public String description;
    public float score;
    public List<Point> points; // rectangle on image

    public LogoItem() {
        this("",0.1f);
    }

    public LogoItem(String description, float score) {
        this.description = description;
        this.score = score;
        points = new ArrayList<>();
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
    }

    protected LogoItem(Parcel in) {
        this.description = in.readString();
        this.score = in.readFloat();
        this.points = in.createTypedArrayList(Point.CREATOR);
    }

    public static final Parcelable.Creator<LogoItem> CREATOR = new Parcelable.Creator<LogoItem>() {
        @Override
        public LogoItem createFromParcel(Parcel source) {
            return new LogoItem(source);
        }

        @Override
        public LogoItem[] newArray(int size) {
            return new LogoItem[size];
        }
    };
}
