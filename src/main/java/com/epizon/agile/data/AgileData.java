package com.epizon.agile.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.epizon.agile.Agile;
import com.epizon.agile.objs.AgileMessage;
import com.epizon.agile.objs.AgileUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by Comp12 on 28-Aug-16.
 */
public class AgileData extends SQLiteOpenHelper {
    static AgileData instance = null;
    static SQLiteDatabase database = null;

    public static final int DATABASE_VERSION=2;
    public static final String DATABASE_NAME="AgileData.db";

    public static final String CHAT_TABLE="messages_table";
    public static final String ID="_id";
    public static final String MESSAGE_ID="message_id";
    public static final String FROM_EMAIL="from_email";
    public static final String FROM_NAME="from_name";
    public static final String TO_EMAIL="to_email";
    public static final String MESSAGE_SUBJECT="message_subject";
    public static final String MESSAGE_BODY="message";
    public static final String SENT_DATETIME="sent_datetime";
    public static final String DELIVERED_DATETIME="delivered_datetime";
    public static final String READ_DATETIME="read_datetime";
    public static final String MEDIA_TYPE="media_type";
    public static final String DOWNLOAD_URL="download_url";
    public static final String THUMBNAIL="thumbnail";
    public static final String USER_TYPE="user_type";
    public static final String FILE_NAME="file_name";
    public static final String READ="read";

    public static synchronized void init(Context context)
    {
        try{
            if (instance == null) {
                instance = new AgileData(context);
                backUpData();
            }
        }catch (Exception e){
            //Log.d("READ_MESSAGES", "init()"+e.toString());
        }
    }

    public static void backUpData(){
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();

        FileChannel source = null;
        FileChannel destination = null;
        String currentDBPath = "/data/" + "com.epizon.agilechatappdemo" + "/databases/" + DATABASE_NAME;
        String backupDBPath = DATABASE_NAME;

        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);

        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static SQLiteDatabase getDatabase() {
        try{
            if (null == database) {
                database = instance.getWritableDatabase();
            }
            return database;
        }catch (Exception e){
            return null;
        }
    }

    public static void deactivate() {
        try{
            if (null != database && database.isOpen()) {
                database.close();
            }
            database = null;
            instance = null;
        }catch (Exception e){
        }

    }

    AgileData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            String CREATE_CONTACTS_TABLE = " CREATE TABLE " + CHAT_TABLE + " ( "
                    + ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
                    + MESSAGE_ID + " TEXT  , "
                    + FROM_EMAIL + " TEXT  , "
                    + FROM_NAME + " TEXT  , "
                    + TO_EMAIL + " TEXT  , "
                    + MESSAGE_SUBJECT + " TEXT , "
                    + MESSAGE_BODY + " TEXT  , "
                    + SENT_DATETIME + " TEXT  , "
                    + DELIVERED_DATETIME + " TEXT  , "
                    + READ_DATETIME + " TEXT  , "
                    + MEDIA_TYPE + " TEXT  , "
                    + THUMBNAIL + " TEXT  , "
                    + DOWNLOAD_URL + " TEXT  , "
                    + USER_TYPE + " TEXT  , "
                    + FILE_NAME + " TEXT  , "
                    + READ + " INTEGER DEFAULT 0  "+ " ) ";
            db.execSQL(CREATE_CONTACTS_TABLE);
        }catch (Exception e){
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try{
            db.execSQL("DROP TABLE IF EXISTS " + CHAT_TABLE);
            onCreate(db);
        }catch (Exception e){
        }
    }

    public static long create(AgileMessage message) {
        try{
                ContentValues cv = new ContentValues();
            cv.put(MESSAGE_ID, message.getId());
            cv.put(FROM_EMAIL, message.getFrom());
            cv.put(FROM_NAME, message.getFromName());
            cv.put(TO_EMAIL, message.getTo());
            cv.put(MESSAGE_SUBJECT, message.getSubject());
            cv.put(MESSAGE_BODY, message.getMessageBody());
            cv.put(SENT_DATETIME, message.getSentDatetime());
            cv.put(DELIVERED_DATETIME, message.getDeliveredtDatetime());
            cv.put(READ_DATETIME, message.getReadDatetime());
            cv.put(MEDIA_TYPE, message.getMediatype());
            cv.put(THUMBNAIL, message.getEncodedThumbnail());
            cv.put(DOWNLOAD_URL, message.getDownloadUrl());
            cv.put(USER_TYPE, message.getUsertype());
            cv.put(FILE_NAME, message.getFilename());
            cv.put(READ, message.getRead() ? 1:0);
            long l = getDatabase().insert(CHAT_TABLE, null, cv);
            return l;
        }catch (Exception e){
            //Log.d("READ_MESSAGES", "create()"+e.toString());
            return 1000;
        }
    }
//

    public static int updateMessage(AgileMessage message) {
        try{
            ContentValues cv = new ContentValues();
            cv.put(DOWNLOAD_URL, message.getDownloadUrl());

            return getDatabase().update(CHAT_TABLE, cv, MESSAGE_ID+ "='" + message.getId()+"'", null);
        }catch (Exception e){
            //, "updateMessage()"+e.toString());
            return  1000;
        }
    }

    public static int updateSentDeliveryTime(AgileMessage message) {
        try{
            ContentValues cv = new ContentValues();
            cv.put(SENT_DATETIME, message.getSentDatetime());
            cv.put(DELIVERED_DATETIME, message.getDeliveredtDatetime());

            return getDatabase().update(CHAT_TABLE, cv, MESSAGE_ID+ "='" + message.getId()+"'", null);
        }catch (Exception e){
            return  1000;
        }
    }

    public static int updateReadTime(String messageId, String readTime) {
        try{
            ContentValues cv = new ContentValues();
            cv.put(READ_DATETIME, readTime);
            cv.put(READ, "1");
            return getDatabase().update(CHAT_TABLE, cv, MESSAGE_ID+ "='" + messageId +"'", null);
        }catch (Exception e){
            //Log.d("READ_MESSAGES", "updateReadTime()"+e.toString());
            return  1000;
        }
    }


    public static Cursor getCursorForgetAll(String from, String to) {
        try{
            String[] columns = new String[] {
                    MESSAGE_ID,
                    FROM_EMAIL,
                    FROM_NAME,
                    TO_EMAIL,
                    MESSAGE_SUBJECT,
                    MESSAGE_BODY,
                    SENT_DATETIME,
                    DELIVERED_DATETIME,
                    MEDIA_TYPE,
                    THUMBNAIL,
                    DOWNLOAD_URL,
                    USER_TYPE,
                    FILE_NAME,
                    READ,
                    READ_DATETIME
            };
            return getDatabase().query(CHAT_TABLE, columns,
                    FROM_EMAIL+"='"+from+"' AND "
                            +TO_EMAIL+"='"+from+"' OR "
                            +FROM_EMAIL+"='"+to+"' OR "
                            +TO_EMAIL+"='"+to+"'" , null, null, null,
                    ID+" ASC");
        }catch (Exception e){
            //Log.d("READ_MESSAGES", "getCursorForgetAll()"+e.toString());
            return null;
        }
    }

    public static Cursor getUnread(String from, String to) {
        try{
            String[] columns = new String[] {
                    MESSAGE_ID,
                    FROM_EMAIL,
                    FROM_NAME,
                    TO_EMAIL,
                    MESSAGE_SUBJECT,
                    MESSAGE_BODY,
                    SENT_DATETIME,
                    DELIVERED_DATETIME,
                    MEDIA_TYPE,
                    THUMBNAIL,
                    DOWNLOAD_URL,
                    USER_TYPE,
                    FILE_NAME,
                    READ,
                    READ_DATETIME
            };
            return getDatabase().query(CHAT_TABLE, columns,
                    READ+"=0 AND "+
                    FROM_EMAIL+"='"+from+"' AND "
                            +TO_EMAIL+"='"+from+"' OR "
                            +FROM_EMAIL+"='"+to+"' OR "
                            +TO_EMAIL+"='"+to+"'" , null, null, null,
                    ID+" ASC");
        }catch (Exception e){
            //Log.d("READ_MESSAGES", "getUnread()"+e.toString());
            return null;
        }
    }
//
    public static List<AgileMessage> getAll(String from, String to) {
        List<AgileMessage> messages = new ArrayList<>();
        try{
            Cursor cursor = AgileData.getCursorForgetAll(from, to);
            if (cursor.moveToFirst()) {

                do {
                    AgileMessage message = new AgileMessage();
                    message.setId(cursor.getString(0));
                    message.setFrom(cursor.getString(1));
                    message.setFromName(cursor.getString(2));
                    message.setTo(cursor.getString(3));
                    message.setSubject(cursor.getString(4));
                    message.setMessageBody(cursor.getString(5));
                    message.setSentDatetime(cursor.getString(6));
                    message.setDeliveredtDatetime(cursor.getString(7));
                    message.setMediatype(Integer.parseInt(cursor.getString(8)));
                    message.setEncodedThumbnail(cursor.getString(9));
                    message.setDownloadUrl(cursor.getString(10));
                    message.setUsertype(Integer.parseInt(cursor.getString(11)));
                    message.setFilename(cursor.getString(12));
                    message.setRead(cursor.getInt(13) == 1);
                    message.setReadDatetime(cursor.getString(14));
                    messages.add(message);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }catch (Exception e){
            //Log.d("READ_MESSAGES", "getAll()"+e.toString());
        }
        return messages;
    }

    public static List<AgileMessage> getUnreadMessages(String from, String to) {
        List<AgileMessage> messages = new ArrayList<>();
        try{
            Cursor cursor = AgileData.getUnread(from, to);
            if (cursor.moveToFirst()) {

                do {
                    AgileMessage message = new AgileMessage();
                    message.setId(cursor.getString(0));
                    message.setFrom(cursor.getString(1));
                    message.setFromName(cursor.getString(2));
                    message.setTo(cursor.getString(3));
                    message.setSubject(cursor.getString(4));
                    message.setMessageBody(cursor.getString(5));
                    message.setSentDatetime(cursor.getString(6));
                    message.setDeliveredtDatetime(cursor.getString(7));
                    message.setMediatype(Integer.parseInt(cursor.getString(8)));
                    message.setEncodedThumbnail(cursor.getString(9));
                    message.setDownloadUrl(cursor.getString(10));
                    message.setUsertype(Integer.parseInt(cursor.getString(11)));
                    message.setFilename(cursor.getString(12));
                    message.setRead(cursor.getInt(13) == 1);
                    message.setReadDatetime(cursor.getString(14));
                    messages.add(message);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }catch (Exception e){
            //Log.d("READ_MESSAGES", "getUnreadMessages()"+e.toString());
        }
        return messages;
    }


    public static List<AgileUser> getUsers(String userId) {
        List<AgileUser> list = new ArrayList<>();
        Cursor cursor = null;
        try {String queryString = "select _id,message_id,from_email,from_name,to_email,message_subject,message,sent_datetime,delivered_datetime,media_type,thumbnail,download_url,user_type,file_name,read,(select count(*) from messages_table where from_email=al.from_email AND read='0') as unread,read_datetime from (select _id as idd,from_email,* from messages_table union select _id,to_email,* from messages_table) as al where from_email!='"+ userId +"' group by from_email order by idd desc;";
//            Log.d("DATA_ERROR", queryString+"");
            AgileUser agileUser;
            String pic = null;
            if(getDatabase() == null){
                Log.d("DATA_ERROR", "getDatabase() == null");
            }
            cursor =  getDatabase().rawQuery(queryString, null);
            Log.d("DATA_ERROR", cursor.getCount()+"");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    if(cursor.getString(2) == null){
                        continue;
                    }
                    agileUser = new AgileUser();
                    agileUser.setEmail(cursor.getString(2));
                    agileUser.setName(cursor.getString(3));
                    agileUser.setUnreadCount(cursor.getString(15));
                    AgileMessage message = new AgileMessage();
                    message.setId(cursor.getString(1));
                    message.setFrom(cursor.getString(2));
                    message.setFromName(cursor.getString(3));
                    message.setTo(cursor.getString(4));
                    message.setSubject(cursor.getString(5));
                    message.setMessageBody(cursor.getString(6));
                    message.setSentDatetime(cursor.getString(7));
                    message.setDeliveredtDatetime(cursor.getString(8));
                    message.setReadDatetime(cursor.getString(16));
                    message.setMediatype(Integer.parseInt(cursor.getString(9)));
                    message.setEncodedThumbnail(cursor.getString(10));
                    message.setDownloadUrl(cursor.getString(11));
                    message.setUsertype(Integer.parseInt(cursor.getString(12)));
                    message.setFilename(cursor.getString(13));
                    agileUser.setLastMessage(message);
                    list.add(agileUser);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Log.d("READ_MESSAGES", "getUsers()"+e.toString());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.deactivate();
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }

    public static void setMessageRead(List<AgileMessage> unreadMessages) {
        if(unreadMessages.size()<=0){
            return;
        }
        String ids = "";
        for(AgileMessage message:unreadMessages){
            if(!message.getId().equals("")){
                if(ids.equals("")){
                    ids+="'"+message.getId()+"'";
                }else {
                    ids+=",'"+message.getId()+"'";
                }
            }
        }
        Cursor cursor = null;
        try {String queryString = "UPDATE " + CHAT_TABLE + " SET "+ READ +"=1 WHERE "+ MESSAGE_ID +" IN("+ ids +");" ;
            cursor =  getDatabase().rawQuery(queryString, null);
        } catch (Exception e) {
            e.printStackTrace();
            //Log.d("READ_MESSAGES", "setMessageRead()"+e.toString());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.deactivate();
                cursor.close();
                cursor = null;
            }
        }
    }

    public static void deleteMessages(List<AgileMessage> unreadMessages) {
        if(unreadMessages.size()<=0){
            return;
        }
        String ids = "";
        for(AgileMessage message:unreadMessages){
            if(!message.getId().equals("")){
                if(ids.equals("")){
                    ids+="'"+message.getId()+"'";
                }else {
                    ids+=",'"+message.getId()+"'";
                }
            }
        }
        Cursor cursor = null;
        try {String queryString = "DELETE FROM " + CHAT_TABLE + " WHERE "+ MESSAGE_ID +" IN("+ ids +");" ;//
            cursor =  getDatabase().rawQuery(queryString, null);
        } catch (Exception e) {
            e.printStackTrace();
            //Log.d("READ_MESSAGES", "deleteMessages()"+e.toString());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.deactivate();
                cursor.close();
                cursor = null;
            }
        }
    }

    public static String getMaxMessageId(){
        try{
            Cursor cursor = getDatabase().rawQuery("SELECT MAX("+ ID +") FROM "+ CHAT_TABLE, new String[] {});
            int last = (cursor.moveToFirst() ? cursor.getInt(0) : 0);
            Log.d("MAXID", String.valueOf(last));
            return String.valueOf(last);
        }catch (Exception e){
            return "0";
        }
    }

}