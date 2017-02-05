package com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject;

import android.graphics.Point;
import android.hardware.camera2.params.Face;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Landmark;
import com.google.api.services.vision.v1.model.LocationInfo;
import com.google.api.services.vision.v1.model.Position;
import com.google.api.services.vision.v1.model.SafeSearchAnnotation;
import com.google.api.services.vision.v1.model.Vertex;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by m on 19/01/2017.
 */

public class GoogleVisionResultData implements Parcelable {

    ArrayList<LandmarkItem> landmarks;
    ArrayList<LabelItem> labels;
    ArrayList<TextItem> texts;
    ArrayList<LogoItem> logos;
    ArrayList<FaceItem> faces;
    SafeSearchItem safeSearch;

    public GoogleVisionResultData() {
        landmarks = new ArrayList<>();
        labels = new ArrayList<>();
        texts = new ArrayList<>();
        logos = new ArrayList<>();
        faces = new ArrayList<>();
    }

    public void setLandmarks(ArrayList<LandmarkItem> list) {
        landmarks = list;
    }

    public void setLabels(ArrayList<LabelItem> list) {
        labels = list;
    }

    public void setTexts(ArrayList<TextItem> list) {
        texts = list;
    }

    public void setLogos(ArrayList<LogoItem> list) {logos = list; }

    public void setFaces(ArrayList<FaceItem> list) {faces = list; }

    public void setSafeSearch(SafeSearchItem item) {safeSearch = item; }

    public ArrayList<LandmarkItem> getLandmark() {
        return  landmarks;
    }

    public ArrayList<LabelItem> getLabels() {
        return labels;
    }

    public ArrayList<TextItem> getTexts() {
        return texts;
    }

    public ArrayList<LogoItem> getLogos() {return logos; }

    public ArrayList<FaceItem> getFaces() {return faces; }

    public SafeSearchItem getSafeSearch() {return safeSearch; }

    public static GoogleVisionResultData getRespondData(BatchAnnotateImagesResponse responseData) {
        GoogleVisionResultData result = new GoogleVisionResultData();
        List<AnnotateImageResponse> responses = responseData.getResponses();
        result.setLabels(getLabelItems(responses.get(0).getLabelAnnotations()));
        result.setLandmarks(getLandmarkItems(responses.get(0).getLandmarkAnnotations()));
        result.setTexts(getTextItems(responses.get(0).getTextAnnotations()));
//        result.setFaces(getFaceItems(responses.get(0).getFaceAnnotations())); //  always empty response
//        result.setLogos(getLogoItems(responses.get(0).getLogoAnnotations())); //  always empty response
//        result.setSafeSearch(getSafeSearchItem(responses.get(0).getSafeSearchAnnotation())); //  always empty response
        return result;
    }

    public static ArrayList<LogoItem> getLogoItems(List<EntityAnnotation> list) {
        ArrayList<LogoItem> result = new ArrayList<>();
        if (list == null) return result;
        for(EntityAnnotation i : list) {
            LogoItem item = new LogoItem(i.getDescription(), i.getScore());

            List<Vertex> vertices = i.getBoundingPoly().getVertices();
            for(Vertex v : vertices) {
                int x = 0, y = 0;
                if (v!= null && v.getX() != null) x =v.getX();
                if (v!= null && v.getY() != null) y = v.getY();
                item.points.add(new Point(x, y));
            }
            result.add(item);
        }
        return result;
    }

    public static ArrayList<LabelItem> getLabelItems(List<EntityAnnotation> list) {
        ArrayList<LabelItem> result = new ArrayList<>();
        if (list == null) return result;
        for(EntityAnnotation i : list) {
            result.add(new LabelItem(i.getDescription(),i.getScore()));
        }
        return result;
    }

    public static ArrayList<LandmarkItem> getLandmarkItems(List<EntityAnnotation> list) {
        ArrayList<LandmarkItem> result = new ArrayList<>();
        if (list == null) return  result;
        for(EntityAnnotation i : list) {
            LandmarkItem item = new LandmarkItem();
            item.score = i.getScore();
            item.description = i.getDescription();

            List<Vertex> vertices = i.getBoundingPoly().getVertices();
            for(Vertex v : vertices) {
                int x = 0, y = 0;
                if (v!= null && v.getX() != null) x =v.getX();
                if (v!= null && v.getY() != null) y = v.getY();
                item.points.add(new Point(x, y));
            }

            LocationInfo lInfo = i.getLocations().get(0); // first location
            item.latitude = lInfo.getLatLng().getLatitude();
            item.longitude = lInfo.getLatLng().getLongitude();
            result.add(item);
        }
        return result;
    }

    public static ArrayList<TextItem> getTextItems(List<EntityAnnotation> list) {
        ArrayList<TextItem> result = new ArrayList<>();
        if (list == null) return result;
        for(EntityAnnotation i : list) {
            TextItem item = new TextItem();
            item.locale = i.getLocale();
            item.content = i.getDescription();
            List<Vertex> vertices = i.getBoundingPoly().getVertices();
            for(Vertex v : vertices) {
                int x = 0, y = 0;
                if (v!= null && v.getX() != null) x =v.getX();
                if (v!= null && v.getY() != null) y = v.getY();
                item.vertices.add(new Point(x, y));
            }
            result.add(item);
        }
        return result;
    }

    public static ArrayList<FaceItem> getFaceItems(List<FaceAnnotation> list) {
        ArrayList<FaceItem> result = new ArrayList<>();
        if (list == null) return result;
        for(FaceAnnotation i : list) {
            FaceItem face = new FaceItem();
            face.panAngle = i.getPanAngle();
            face.rollAngle = i.getRollAngle();
            face.tiltAngle = i.getTiltAngle();
            face.detectionConfidence = i.getDetectionConfidence();
            face.landmarkingConfidence = i.getLandmarkingConfidence();
            face.anger = i.getAngerLikelihood();
            face.sorrow = i.getSorrowLikelihood();
            face.joy = i.getJoyLikelihood();
            face.exposed = i.getUnderExposedLikelihood();
            face.blurred = i.getBlurredLikelihood();
            face.headwear = i.getHeadwearLikelihood();
            face.surprise = i.getSurpriseLikelihood();

            // inface bounding
            List<Vertex> vertices = i.getBoundingPoly().getVertices();
            for (Vertex v : vertices) {
                int x = 0, y = 0;
                if (v!= null && v.getX() != null) x =v.getX();
                if (v!= null && v.getY() != null) y = v.getY();
                face.inFacePoints.add(new Point(x, y));
            }

            // outface bounding
            vertices = i.getFdBoundingPoly().getVertices();
            for (Vertex v : vertices) {
                face.outFacePoints.add(new Point(v.getX(), v.getY()));
            }

            // components in face. Eg: noise, eyes, lips, mouth...
            List<Landmark> landmarks = i.getLandmarks();
            for(Landmark l : landmarks) {
                Position p = landmarks.get(0).getPosition();
                float[] f = new float[3];
                f[0] = p.getX();
                f[1] = p.getY();
                f[2] = p.getZ();
                face.componentPoints.add(f);
            }

            result.add(face);
        }

        return result;
    }

    public static SafeSearchItem getSafeSearchItem(SafeSearchAnnotation annotation) {
        SafeSearchItem result = new SafeSearchItem();

        if (annotation == null) return null;

        result.adultValue = annotation.getAdult();
        result.medicalValue = annotation.getMedical();
        result.spoofValue = annotation.getSpoof();
        result.violenceValue = annotation.getViolence();
        return result;
    }

    public static SafeSearchItem getSafeSearchItem(JSONObject obj) {
        SafeSearchItem result = new SafeSearchItem();
        try {
            JSONObject root = (JSONObject) obj.getJSONArray("responses").get(0);
            JSONObject safeSearchObj = root.getJSONObject(SafeSearchItem.NAME_SAFESEARCH_JSON);
            result.violenceValue = safeSearchObj.getString(SafeSearchItem.KEY_VIOLENCE);
            result.spoofValue = safeSearchObj.getString(SafeSearchItem.KEY_SPOOF);
            result.medicalValue = safeSearchObj.getString(SafeSearchItem.KEY_MEDICAL);
            result.adultValue = safeSearchObj.getString(SafeSearchItem.KEY_ADULT);
        } catch (JSONException e) {
            e.printStackTrace();
            return result;
        }
        return result;
    }

    public static ArrayList<LogoItem> getLogoItem(JSONObject obj) {
        ArrayList<LogoItem> result = new ArrayList<>();
        try {
            JSONObject root = (JSONObject) obj.getJSONArray("responses").get(0);
            JSONArray logoObj = root.getJSONArray(LogoItem.NAME_LOGO_JSON);
            int n = logoObj.length();
            for (int i = 0; i < n; i++) {
                LogoItem item = new LogoItem();
                JSONObject o = logoObj.getJSONObject(i);
                item.description = o.getString(LogoItem.KEY_DESCRIPTION);
                item.score = (float)o.getDouble(LogoItem.KEY_SCORE);
                JSONArray verticesObj = o.getJSONObject(LogoItem.KEY_BOUNDING_POLY)
                                         .getJSONArray(LogoItem.KEY_VERTICES);
                for (int j = 0; j < verticesObj.length(); j++) {
                    JSONObject vertexObj = (JSONObject)verticesObj.get(j);
                    item.points.add(new Point(vertexObj.getInt("x"), vertexObj.getInt("y")));
                }
                result.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
        return result;
    }

    public static ArrayList<FaceItem> getFaceItem(JSONObject obj) {
        ArrayList<FaceItem> result = new ArrayList<>();
        try {
            JSONObject root = (JSONObject) obj.getJSONArray("responses").get(0);
            JSONArray faceObj = root.getJSONArray(FaceItem.NAME_FACE_JSON);

            int n = faceObj.length();
            for (int i = 0; i < n; i++) {
                FaceItem item = new FaceItem();
                JSONObject o = (JSONObject) faceObj.get(i);

                item.detectionConfidence = (float)o.getDouble(FaceItem.KEY_DETECTION_CONFIDENCE);
                item.landmarkingConfidence = (float)o.getDouble(FaceItem.KEY_LANDMARKING_CONFIDENCE);

                item.rollAngle = (float)o.getDouble(FaceItem.KEY_ROLL_ANGLE);
                item.panAngle = (float)o.getDouble(FaceItem.KEY_PAN_ANGLE);
                item.tiltAngle = (float)o.getDouble(FaceItem.KEY_TILT_ANGLE);
                item.anger = o.getString(FaceItem.KEY_ANGER);
                item.surprise = o.getString(FaceItem.KEY_SURPRISE);
                item.sorrow = o.getString(FaceItem.KEY_SORROW);
                item.blurred = o.getString(FaceItem.KEY_BLURRED);
                item.headwear = o.getString(FaceItem.KEY_HEADWEAR);
                item.exposed = o.getString(FaceItem.KEY_EXPOSED);
                item.joy = o.getString(FaceItem.KEY_JOY);

                JSONArray verticesObj = o.getJSONObject(FaceItem.KEY_BOUNDING_POLY)
                        .getJSONArray(FaceItem.KEY_VERTICES);
                for(int j = 0; j < verticesObj.length(); j++) {
                    JSONObject vertexObj = verticesObj.getJSONObject(j);
                    item.outFacePoints.add(new Point(vertexObj.getInt("x"), vertexObj.getInt("y")));
                }

                verticesObj = o.getJSONObject(FaceItem.KEY_FD_BOUNDING_POLY)
                        .getJSONArray(FaceItem.KEY_VERTICES);
                for(int j = 0; j < verticesObj.length(); j++) {
                    JSONObject vertexObj = verticesObj.getJSONObject(j);
                    item.inFacePoints.add(new Point(vertexObj.getInt("x"), vertexObj.getInt("y")));
                }

                JSONArray landmarksObj = o.getJSONArray(FaceItem.KEY_LANDMARKS);
                for (int j = 0; j < landmarksObj.length(); j++) {
                    JSONObject positionObj = landmarksObj.getJSONObject(j)
                            .getJSONObject(FaceItem.KEY_POSITION);
                    float[] f = new float[3];
                    f[0] = (float)positionObj.getDouble("x");
                    f[1] = (float)positionObj.getDouble("y");
                    f[2] = (float)positionObj.getDouble("z");
                    item.componentPoints.add(f);
                }

                result.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.landmarks);
        dest.writeTypedList(this.labels);
        dest.writeTypedList(this.texts);
        dest.writeTypedList(this.logos);
        dest.writeTypedList(this.faces);
        dest.writeParcelable(this.safeSearch, flags);
    }

    protected GoogleVisionResultData(Parcel in) {
        this.landmarks = in.createTypedArrayList(LandmarkItem.CREATOR);
        this.labels = in.createTypedArrayList(LabelItem.CREATOR);
        this.texts = in.createTypedArrayList(TextItem.CREATOR);
        this.logos = in.createTypedArrayList(LogoItem.CREATOR);
        this.faces = in.createTypedArrayList(FaceItem.CREATOR);
        this.safeSearch = in.readParcelable(SafeSearchItem.class.getClassLoader());
    }

    public static final Parcelable.Creator<GoogleVisionResultData> CREATOR = new Parcelable.Creator<GoogleVisionResultData>() {
        @Override
        public GoogleVisionResultData createFromParcel(Parcel source) {
            return new GoogleVisionResultData(source);
        }

        @Override
        public GoogleVisionResultData[] newArray(int size) {
            return new GoogleVisionResultData[size];
        }
    };
}