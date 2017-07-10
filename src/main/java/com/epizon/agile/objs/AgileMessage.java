package com.epizon.agile.objs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.android.internal.util.Predicate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Comp12 on 08-Aug-16.
 */
public class AgileMessage implements Serializable {
    public String USER_TYPE = "";
    public static final int USER = 0;
    public static final int OTHER = 1;

    String from, fromName, to;
    String id = "", subject = "", messageBody = "", sentDatetime = "", deliveredtDatetime = "", readDatetime = "";
    int usertype = 0;
    int mediatype = 1;
    String downloadUrl = "", encodedThumbnail = "";
    Bitmap thumbbitmap = null, bitmap = null;
    String filename = "";
    String filepath = "";
    boolean read = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getSentDatetime() {
        return sentDatetime;
    }

    public void setSentDatetime(String sentDatetime) {
        this.sentDatetime = sentDatetime;
    }

    public String getDeliveredtDatetime() {
        return deliveredtDatetime;
    }

    public void setDeliveredtDatetime(String deliveredtDatetime) {
        this.deliveredtDatetime = deliveredtDatetime;
    }

    public String getReadDatetime() {
        return readDatetime;
    }

    public void setReadDatetime(String readtDatetime) {
        this.readDatetime = readtDatetime;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromName() {
        return fromName;
    }

    public int getUsertype() {
        return usertype;
    }

    public void setUsertype(int usertype) {
        this.usertype = usertype;
    }

    public int getMediatype() {
        return mediatype;
    }

    public void setMediatype(int mediatype) {
        this.mediatype = mediatype;
    }

    public void attachImage(Bitmap image){
        try{
            this.bitmap = image;
            image = ThumbnailUtils.extractThumbnail(image, 100, 100);
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOS);
            encodedThumbnail = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
            Log.d("ENCODED_STRING", encodedThumbnail);
            this.mediatype = AgileMediaType.IMAGE;
        }catch (Exception e){
            encodedThumbnail = "";
        }
    }

    public void attachVideo(File videoFile){
        try{
            this.mediatype = AgileMediaType.VIDEO;
            this.filepath = videoFile.getAbsolutePath();
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            this.bitmap = ThumbnailUtils.createVideoThumbnail(videoFile.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
            this.bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOS);
            encodedThumbnail = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
        }catch (Exception e){
            Log.d("OKRESPONSE_", e.toString());
            encodedThumbnail = "";
        }
    }

    public void attachLocationFile(Bitmap image){
        try{
            this.bitmap = image;
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOS);
            encodedThumbnail = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
            decodeThumbNail(encodedThumbnail);
            Log.d("ENCODED_STRING", encodedThumbnail);
            this.mediatype = AgileMediaType.LOCATION;
        }catch (Exception e){
            encodedThumbnail = "";
        }
    }

    public void attachAudio(File audioFile){
        try{
            this.mediatype = AgileMediaType.AUDIO;
            encodedThumbnail = "";
        }catch (Exception e){
            Log.d("OKRESPONSE_", e.toString());
            encodedThumbnail = "";
        }
    }

    public String getEncodedThumbnail() {
        return encodedThumbnail;
    }

    public void setEncodedThumbnail(String encodedThumbnail) {
        this.encodedThumbnail = encodedThumbnail;
        decodeThumbNail(this.encodedThumbnail);
    }

    public void decodeThumbNail(String input){
        try{
            byte[] decodedBytes = Base64.decode(input, 0);
            this.thumbbitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        }catch (Exception e){
        }
    }

    public Bitmap getThumbnail(){
        return thumbbitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilePath() {
        return filepath;
    }

    public void setFilePath(String filepath) {
        this.filepath = filepath;
    }

    public void attachPdf(){
    }

    public boolean getRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public static final String MESSAGE_ID = "message_id";
    public static final String FROM_EMAIL = "from_email";
    public static final String FROM_NAME = "from_name";
    public static final String TO_EMAIl = "to_email";
    public static final String MESSAGE_SUBJECT = "message_subject";
    public static final String MESSAGE_BODY = "message_body";
    public static final String SENT_TIME = "senttime";
    public static final String DELIVERED_TIME = "deliveredtime";
    public static final String READ_TIME = "readtime";
    public static final String MEDIA_TYPE = "media_type";
    public static final String DOWNLOAD_URL = "download_url";
    public static final String THUMBNAIL = "thumbnail";
}

