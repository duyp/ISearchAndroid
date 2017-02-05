package com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m on 22/01/2017.
 */

public class FaceItem implements Parcelable {



    public static final String NAME_FACE_JSON = "faceAnnotations";
    public static final String KEY_ROLL_ANGLE = "rollAngle";
    public static final String KEY_TILT_ANGLE = "tiltAngle";
    public static final String KEY_PAN_ANGLE = "panAngle";
    public static final String KEY_DETECTION_CONFIDENCE = "detectionConfidence";
    public static final String KEY_LANDMARKING_CONFIDENCE = "landmarkingConfidence";

    public static final String KEY_JOY = "joyLikelihood";
    public static final String KEY_SORROW = "sorrowLikelihood";
    public static final String KEY_ANGER = "angerLikelihood";
    public static final String KEY_SURPRISE = "surpriseLikelihood";
    public static final String KEY_EXPOSED = "underExposedLikelihood";
    public static final String KEY_BLURRED = "blurredLikelihood";
    public static final String KEY_HEADWEAR = "headwearLikelihood";

    public static final String KEY_BOUNDING_POLY = "boundingPoly";
    public static final String KEY_FD_BOUNDING_POLY = "fdBoundingPoly";
    public static final String KEY_VERTICES = "vertices";

    public static final String KEY_LANDMARKS = "landmarks";
    public static final String KEY_POSITION = "position";

    public List<Point> inFacePoints;
    public List<Point> outFacePoints;
    public List<float[]> componentPoints;

    public float rollAngle;
    public float panAngle;
    public float tiltAngle;
    public float detectionConfidence;
    public float landmarkingConfidence;

    public String anger;
    public String joy;
    public String blurred;
    public String surprise;
    public String headwear;
    public String sorrow;
    public String exposed;

    public FaceItem() {
        inFacePoints = new ArrayList<>();
        outFacePoints = new ArrayList<>();
        componentPoints = new ArrayList<>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.inFacePoints);
        dest.writeTypedList(this.outFacePoints);
        dest.writeList(this.componentPoints);
        dest.writeFloat(this.rollAngle);
        dest.writeFloat(this.panAngle);
        dest.writeFloat(this.tiltAngle);
        dest.writeFloat(this.detectionConfidence);
        dest.writeFloat(this.landmarkingConfidence);
        dest.writeString(this.anger);
        dest.writeString(this.joy);
        dest.writeString(this.blurred);
        dest.writeString(this.surprise);
        dest.writeString(this.headwear);
        dest.writeString(this.sorrow);
        dest.writeString(this.exposed);
    }

    protected FaceItem(Parcel in) {
        this.inFacePoints = in.createTypedArrayList(Point.CREATOR);
        this.outFacePoints = in.createTypedArrayList(Point.CREATOR);
        this.componentPoints = new ArrayList<float[]>();
        in.readList(this.componentPoints, float[].class.getClassLoader());
        this.rollAngle = in.readFloat();
        this.panAngle = in.readFloat();
        this.tiltAngle = in.readFloat();
        this.detectionConfidence = in.readFloat();
        this.landmarkingConfidence = in.readFloat();
        this.anger = in.readString();
        this.joy = in.readString();
        this.blurred = in.readString();
        this.surprise = in.readString();
        this.headwear = in.readString();
        this.sorrow = in.readString();
        this.exposed = in.readString();
    }

    public static final Parcelable.Creator<FaceItem> CREATOR = new Parcelable.Creator<FaceItem>() {
        @Override
        public FaceItem createFromParcel(Parcel source) {
            return new FaceItem(source);
        }

        @Override
        public FaceItem[] newArray(int size) {
            return new FaceItem[size];
        }
    };
}
