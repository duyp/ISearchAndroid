package com.uit.instancesearch.camera.GoogleModels;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.services.vision.v1.model.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m on 20/01/2017.
 */

public class TextItem implements Parcelable {
    public String locale;
    public String content;
    public List<Point> vertices;

    public TextItem() {
        this("en", "");
    }

    public TextItem(String locale, String content) {
        this.content = content;
        this.locale = locale;
        vertices = new ArrayList<>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.locale);
        dest.writeString(this.content);
        dest.writeList(this.vertices);
    }

    protected TextItem(Parcel in) {
        this.locale = in.readString();
        this.content = in.readString();
        this.vertices = new ArrayList<>();
        in.readList(this.vertices, Vertex.class.getClassLoader());
    }

    public static final Parcelable.Creator<TextItem> CREATOR = new Parcelable.Creator<TextItem>() {
        @Override
        public TextItem createFromParcel(Parcel source) {
            return new TextItem(source);
        }

        @Override
        public TextItem[] newArray(int size) {
            return new TextItem[size];
        }
    };
}