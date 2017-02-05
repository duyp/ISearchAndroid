package com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by m on 22/01/2017.
 */

public class SafeSearchItem implements Parcelable {

    public static final String[] VALUES = new String[] {"VERY_UNLIKELY",
                                        "UNLIKELY", "POSSIBLE", "LIKELY", "VERY_LIKELY"};
    public static final int[] SCORES = new int[] {2, 25, 50, 75, 100};

    public static final String NAME_SAFESEARCH_JSON = "safeSearchAnnotation";
    public static final String KEY_ADULT = "adult";
    public static final String KEY_SPOOF = "spoof";
    public static final String KEY_MEDICAL = "medical";
    public static final String KEY_VIOLENCE = "violence";

    public String adultValue = "";
    public String spoofValue = "";
    public String medicalValue = "";
    public String violenceValue = "";
    public SafeSearchItem() {}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.adultValue);
        dest.writeString(this.spoofValue);
        dest.writeString(this.medicalValue);
        dest.writeString(this.violenceValue);
    }

    protected SafeSearchItem(Parcel in) {
        this.adultValue = in.readString();
        this.spoofValue = in.readString();
        this.medicalValue = in.readString();
        this.violenceValue = in.readString();
    }

    public static final Parcelable.Creator<SafeSearchItem> CREATOR = new Parcelable.Creator<SafeSearchItem>() {
        @Override
        public SafeSearchItem createFromParcel(Parcel source) {
            return new SafeSearchItem(source);
        }

        @Override
        public SafeSearchItem[] newArray(int size) {
            return new SafeSearchItem[size];
        }
    };

    public static int getScoreOf(String value) {
        int n = VALUES.length;
        for (int i = 0; i < n; i++) {
            if (VALUES[i].equals(value)) return SCORES[i];
        }
        return 0;
    }
}
