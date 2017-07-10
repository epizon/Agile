package com.epizon.agile;

import android.app.Service;
import android.content.Context;
import android.nfc.Tag;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.epizon.agile.constants.AgileConnection;
import com.epizon.agile.data.AgileData;
import com.epizon.agile.error.AgileLoginError;
import com.epizon.agile.error.AgileRegistrationError;
import com.epizon.agile.events.AgileAuthenticationListener;
import com.epizon.agile.events.AgileCheckOnlineListener;
import com.epizon.agile.events.AgileConnectionListener;
import com.epizon.agile.events.AgileFriendsLoginListener;
import com.epizon.agile.events.AgileMessageListener;
import com.epizon.agile.events.AgileRegistrationListener;
import com.epizon.agile.events.AgileTypingListener;
import com.epizon.agile.objs.AgileMediaType;
import com.epizon.agile.objs.AgileMessage;
import com.epizon.agile.objs.AgileUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import com.epizon.agile.constants.AgileConnection.*;
import com.epizon.agile.objs.AgileUserAvailabilityMode;

/**
 * Created by Comp12 on 06-Aug-16.
 */
public class Agile implements AgileConnectionListener{

    private static Agile instance;
    public static Agile getInstance() {
        if( instance == null ) {
            instance = new Agile();
        }
        return instance;
    }
    String TAG = "AGILE_LIBRARY";
    Socket socket = null;
    Context context;
    AgileConnectionListener agileconnectionlistener;
    AgileRegistrationListener chatregistrationlistener;
    AgileAuthenticationListener agileauthenticationlistener;
    AgileFriendsLoginListener friendsloginlistener;
    AgileMessageListener agilemessagelistener;
    AgileTypingListener agiletypinglistener;
    AgileCheckOnlineListener agilecheckonlinelistener;
    EditText edittext;

    boolean connected = false;

    boolean loggedIn = false;
    boolean isLoggingIn = false;

    AgileUser loggedUser = null;
    String url = "";
    String username = null, password =  null;
    String usernameServed = null, passwordServed =  null;
    String subscribedUser = "";

    private Agile(){
    }

    public boolean isConnected(){
        return connected;
    }

    public AgileUser getLoggedUser(){
        return loggedUser;
    }

    public boolean isLoggedIn(){
        return loggedIn;
    }

    public boolean isLoggingIn(){
        return isLoggingIn;
    }

    public void disconnect(){
        try{
            if(socket != null){
                offSockets();
                socket.disconnect();
            }
        }catch (Exception e){
            //Log.d(TAG,"Agile.disconnect() " + e.toString());
        }
    }

    public void onSockets(){
        try{
            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on(Socket.EVENT_DISCONNECT, onDisConnect);
            socket.on(AgileConnection.AGILE_REGISTER, onRegister);
            socket.on(AgileConnection.AGILE_LOGIN, onLogin);
            socket.on(AgileConnection.AGILE_CHECK_ONLINE, checkOnline);
            socket.on("asd", asd);
            socket.on(AgileConnection.AGILE_USER_LOGGED, friendLogged);
            socket.on(AgileConnection.AGILE_SEND_MESSAGE, sendMessage);
            socket.on(AgileConnection.AGILE_RECEIVE_MESSAGE, receiveMessage);
            socket.on(AgileConnection.AGILE_DELIVERY_REPORT, deliveryReport);
            socket.on(AgileConnection.AGILE_SEND_READ_REPORT, sendreadReport);
            socket.on(AgileConnection.AGILE_RECEIVE_READ, receiveRead);
            socket.on(AgileConnection.AGILE_START_TYPING_NOTIFICATION, startedTypingNotification);
            socket.on(AgileConnection.AGILE_STOP_TYPING_NOTIFICATION, stoppedTypingNotification);
        }catch (Exception e){
            //Log.d(TAG,"Agile.offSockets() " + e.toString());
        }
    }

    public void offSockets(){
        try{
            socket.off(Socket.EVENT_CONNECT, onConnect);
            socket.off(Socket.EVENT_DISCONNECT, onDisConnect);
            socket.off(AgileConnection.AGILE_REGISTER, onRegister);
            socket.off(AgileConnection.AGILE_LOGIN, onLogin);
            socket.off(AgileConnection.AGILE_CHECK_ONLINE, checkOnline);
            socket.off(AgileConnection.AGILE_USER_LOGGED, friendLogged);
            socket.off(AgileConnection.AGILE_SEND_MESSAGE, sendMessage);
            socket.off(AgileConnection.AGILE_RECEIVE_MESSAGE, receiveMessage);
            socket.off(AgileConnection.AGILE_DELIVERY_REPORT, deliveryReport);
            socket.off(AgileConnection.AGILE_SEND_READ_REPORT, sendreadReport);
            socket.off(AgileConnection.AGILE_RECEIVE_READ, receiveRead);
            socket.off(AgileConnection.AGILE_START_TYPING_NOTIFICATION, startedTypingNotification);
            socket.off(AgileConnection.AGILE_STOP_TYPING_NOTIFICATION, stoppedTypingNotification);
            loggedIn = false;
        }catch (Exception e){
            //Log.d(TAG,"Agile.offSockets() " + e.toString());
        }
    }

    public void initialize(Context context){
        try{
            this.context = context;
            String url = "http://ec2-52-41-161-21.us-west-2.compute.amazonaws.com:9091/";
            connectToAgile(url);
        }catch (Exception e){
            //Log.d(TAG,"initialize.Exception " + e.toString());
        }
    }

    private void connectToAgile(String url, AgileConnectionListener agileconnectionlistenerServed){
        if(isConnected()){
            //Log.d(TAG, "connectToAgile() cancelled isConnected()=="+isConnected());
            return;
        }
        this.url = url;
        this.agileconnectionlistener = agileconnectionlistenerServed;
        disconnect();
        try{
            //socket = IO.socket("http://192.168.4.213:4000/");
            socket = IO.socket(url);
            onSockets();
            socket.connect();
        }catch (Exception e){
            //Log.d(TAG,"connectToAgile().Exception " + e.toString());
            connected = false;
            loggedIn = false;
        }
    }

    private void connectToAgile(String url){
        if(isConnected()){
            return;
        }
        this.url = url;
        disconnect();
        try{
            IO.Options opts = new IO.Options();
            socket = IO.socket(url, opts);
            onSockets();
            socket.connect();
        }catch (Exception e){
            //Log.d(TAG,"connectToAgile().Exception " + e.toString());
            connected = false;
            loggedIn = false;
        }
    }

    private void connectToAgile(){
        if(isConnected()){
            return;
        }
        disconnect();
        try{
            socket = IO.socket(url);
            onSockets();
            socket.connect();
        }catch (Exception e){
            //Log.d(TAG,"connectToAgile().Exception " + e.toString());
            connected = false;
            loggedIn = false;
        }
    }

    public void registerUser(String emailId, String username, String status, String password, AgileRegistrationListener chatregistrationlistenerServed){
        this.chatregistrationlistener = chatregistrationlistenerServed;
        try{
            JSONObject object = new JSONObject();
            object.put(AgileConnection.EMAIL, emailId);
            object.put(AgileConnection.NAME, username);
            object.put(AgileConnection.STATUS, status);
            object.put(AgileConnection.PASS, password);
            socket.emit(AgileConnection.AGILE_REGISTER, object);
        }catch (Exception e){
            //Log.d(TAG,"registerUser().Exception " + e.toString());
        }
    }

    public void login(String email, String password, AgileAuthenticationListener agileauthenticationlistenerServed){
        if(isLoggingIn()){
            return;
        }
        this.usernameServed = email;
        this.passwordServed = password;
        this.agileauthenticationlistener = agileauthenticationlistenerServed;
        try{
            JSONObject object = new JSONObject();
            object.put(AgileConnection.EMAIL,email);
            object.put(AgileConnection.PASS,password);
            isLoggingIn = true;
            socket.emit(AgileConnection.AGILE_LOGIN, object);
        }catch (Exception e){
            //Log.d(TAG,"login().Exception " + e.toString());
            loggedIn = false;
            isLoggingIn = false;
        }
    }

    public void login(String email, String password){
        if(isLoggingIn()){
            return;
        }
        this.usernameServed = email;
        this.passwordServed = password;
        try{
            JSONObject object = new JSONObject();
            object.put(AgileConnection.EMAIL, email);
            object.put(AgileConnection.PASS, password);
            isLoggingIn = true;
            socket.emit(AgileConnection.AGILE_LOGIN, object);
        }catch (Exception e){
            //Log.d(TAG,"login().Exception " + e.toString());
            loggedIn = false;
            isLoggingIn = false;
        }
    }

    public void checkIsUserOnline(String from_email, String friend_email, AgileCheckOnlineListener agilecheckonlinelistenerServed){
        this.agilecheckonlinelistener = agilecheckonlinelistenerServed;
        if(isLoggingIn()){
            return;
        }
        try{
            subscribedUser = friend_email;
            JSONObject object = new JSONObject();
            object.put(AgileMessage.FROM_EMAIL, from_email);
            object.put(AgileMessage.TO_EMAIl, friend_email);
            socket.emit(AgileConnection.AGILE_CHECK_ONLINE, object);
        }catch (Exception e){
            //Log.d(TAG,"checkIsUserOnline().Exception " + e.toString());
        }
    }

    public void sendMessage(Context appContext, AgileMessage message){
        if(!isLoggedIn()){
            return;
        }
        try{
            JSONObject object = new JSONObject();
            object.put(AgileMessage.FROM_EMAIL,loggedUser.getEmail());
            object.put(AgileMessage.FROM_NAME,loggedUser.getName());
            object.put(AgileMessage.TO_EMAIl, message.getTo());
            object.put(AgileMessage.MESSAGE_ID, message.getId());
            object.put(AgileMessage.MEDIA_TYPE, message.getMediatype());
            object.put(AgileMessage.DOWNLOAD_URL, message.getDownloadUrl());
            object.put(AgileMessage.THUMBNAIL, message.getEncodedThumbnail());
            object.put(AgileMessage.MESSAGE_SUBJECT, message.getSubject());
            object.put(AgileMessage.MESSAGE_BODY, message.getMessageBody());
            socket.emit(AgileConnection.AGILE_SEND_MESSAGE, object);
            if(message.getMediatype() == AgileMediaType.TEXT || message.getMediatype() == AgileMediaType.LOCATION ){
                AgileData.init(context);
                AgileData.create(message);
                AgileData.deactivate();
            }else{
                AgileData.init(context);
                AgileData.updateMessage(message);
                AgileData.deactivate();
            }
        }catch (Exception e){
            //Log.d(TAG,"sendMessage().Exception " + e.toString());
        }
    }

    public void setUserAvailability(int availability){
        try {
            if(!isLoggedIn()){
                return;
            }
            JSONObject obj = new JSONObject();
            obj.put(AgileConnection.EMAIL, loggedUser.getEmail());
            obj.put(AgileConnection.AVAILABLITY, availability);
            socket.emit(AgileConnection.AGILE_SET_USER_AVAILABILITY, obj);
        }catch (Exception e){
        }
    }

    public void sendMessageHelper(){

    }


    public void setAgileConnectionListener(AgileConnectionListener agileconnectionlistenerServed){
        this.agileconnectionlistener = agileconnectionlistenerServed;
    }

    public void setAgileMessageListener(AgileMessageListener agilemessagelistenerServed){
        this.agilemessagelistener = agilemessagelistenerServed;
    }

    public void setAgileTypingListener(AgileTypingListener agiletypinglistenerServed){
        this.agiletypinglistener = agiletypinglistenerServed;
    }

    public void setFriendLoginListener(AgileFriendsLoginListener friendsloginlistenerServed){
        this.friendsloginlistener = friendsloginlistenerServed;
    }
    //===========================================================================================================================
    //================================================Emitters START===================================================================

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if(agileconnectionlistener != null){
                //Log.d(TAG,"onConnected");
                connected = true;
                agileconnectionlistener.onAgileServerConnected();
                if(username != null && password != null){
                    //login(username, password);
                    //Log.d(TAG,"onConnected username != null && password != null");
                }else {
                    //Log.d(TAG,"onConnected username == null && password == null");
                }
            }
        }
    };

    private Emitter.Listener onDisConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if(agileconnectionlistener != null){
                //Log.d(TAG,"onDisConnected");
                connected = false;
                loggedIn = false;
                isLoggingIn = false;
                agileconnectionlistener.onAgileServerDisConnected();
            }
        }
    };
    String users = "";
    private Emitter.Listener onRegister = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[1];
                Log.d(TAG, "onLogin: "+data.toString()+"\n");
                if(data.getString(AgileConnection.ERROR_TYPE).equals("0")){
                    if(chatregistrationlistener != null){
                        chatregistrationlistener.onRegistrationSuccess(data.toString());
                    }
                }else if(data.getString(AgileConnection.ERROR_TYPE).equals("1")){
                    if(chatregistrationlistener != null){
                        AgileRegistrationError error = new AgileRegistrationError();
                        error.setErrorType(AgileRegistrationError.USER_ALREADY_EXISTS);
                        chatregistrationlistener.onRegistrationFailed(error);
                    }
                }else if(data.getString(AgileConnection.ERROR_TYPE).equals("2")){
                    if(chatregistrationlistener != null){
                        AgileRegistrationError error = new AgileRegistrationError();
                        error.setErrorType(AgileRegistrationError.USER_INSERTION_ERROR);
                        chatregistrationlistener.onRegistrationFailed(error);
                    }
                }
            }catch (Exception e){
                Log.d(TAG, "registerUser: "+e.toString());
            }
        }
    };

    public void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener checkOnline = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
            }catch (Exception e){
                //Log.d(TAG, "checkOnline: "+e.toString());
            }
        }
    };

    private Emitter.Listener asd = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                if(data.getString("to_email").equals(subscribedUser)){
                    if(agilecheckonlinelistener != null){
                        String lastSeen = data.getString("last_seen");
                        if(!lastSeen.equals("")){
                            lastSeen = getDate(lastSeen);
                        }
                        agilecheckonlinelistener.onCheckOnline(data.getString("from_email"), data.getBoolean("isonline"), lastSeen);
                    }
                }
            }catch (Exception e){
                //Log.d(TAG, "checkOnline: "+e.toString());
            }
        }
    };

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[1];
                isLoggingIn = false;
                if(data.getString(AgileConnection.ERROR_TYPE).equals("0")){
                    loggedIn = true;
                    if(agileauthenticationlistener != null){
                        username = usernameServed;
                        password =  passwordServed;
                        List<AgileUser> list = new ArrayList<>();
                        loggedUser = new AgileUser(data.getString(AgileConnection.EMAIL), data.getString(AgileConnection.NAME), data.getString(AgileConnection.STATUS));
                        agileauthenticationlistener.onLoginSuccess(loggedUser, list);
                    }
                }else if(data.getString(AgileConnection.ERROR_TYPE).equals("1")){
                    loggedIn = false;
                    if(agileauthenticationlistener != null){
                        AgileLoginError error = new AgileLoginError();
                        error.setErrorType(AgileLoginError.USER_DOESNT_EXISTS);
                        agileauthenticationlistener.onLoginFailed(error);
                    }
                }
            }catch (Exception e){
                //Log.d(TAG, "onLogin: "+e.toString());
                loggedIn = false;
                isLoggingIn = false;
            }
        }
    };

    private Emitter.Listener friendLogged = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                if(data.getString(AgileConnection.EMAIL).equals(subscribedUser)){
                    if(data.getBoolean(AgileConnection.LOGGED)){
                        if(agilecheckonlinelistener != null){
                            agilecheckonlinelistener.onUserLogin(data.getString(AgileConnection.EMAIL));
                        }
                    }else {
                        if(agilecheckonlinelistener != null){
                            String lastSeen = data.getString(AgileConnection.LAST_SEEN);
                            if(!lastSeen.equals("")){
                                lastSeen = getDate(lastSeen);
                            }
                            agilecheckonlinelistener.onUserLogOut(data.getString(AgileConnection.EMAIL), lastSeen);
                        }
                    }
                }
            }catch (Exception e){
                //Log.d(TAG, "userLogged: "+e.toString());
            }
        }
    };


    private Emitter.Listener sendMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                Log.d(TAG, "sendMessage: "+data.toString()+"\n");
                socket.emit(AgileConnection.AGILE_SEND_MESSAGE_VERIFIED, data);
                AgileMessage message = new AgileMessage();
                message.setId(data.getString(AgileMessage.MESSAGE_ID));
                message.setFrom(data.getString(AgileMessage.FROM_EMAIL));
                message.setFromName(data.getString(AgileMessage.FROM_NAME));
                message.setTo(data.getString(AgileMessage.TO_EMAIl));
                message.setSubject(data.getString(AgileMessage.MESSAGE_SUBJECT));
                message.setMessageBody(data.getString(AgileMessage.MESSAGE_BODY));
                message.setSentDatetime(getDate(data.getString(AgileMessage.SENT_TIME)));
                message.setMediatype(Integer.parseInt(data.getString(AgileMessage.MEDIA_TYPE)));
                message.setDownloadUrl(data.getString(AgileMessage.DOWNLOAD_URL));
                message.setEncodedThumbnail(data.getString(AgileMessage.THUMBNAIL));
                message.setUsertype(AgileMessage.USER);
                agilemessagelistener.onMessageSentEvent(message);
                AgileData.init(context);
                AgileData.updateSentDeliveryTime(message);
                AgileData.deactivate();
            }catch (Exception e){
                //Log.d(TAG, "messagereceived: "+e.toString());
            }
        }
    };

    private Emitter.Listener receiveMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                //Log.d(TAG, "receiveMessage: "+data.toString()+"\n");
                socket.emit(AgileConnection.AGILE_RECEIVE_MESSAGE, data);
                AgileMessage message = new AgileMessage();
                message.setId(data.getString(AgileMessage.MESSAGE_ID));
                message.setFrom(data.getString(AgileMessage.FROM_EMAIL));
                message.setFromName(data.getString(AgileMessage.FROM_NAME));
                message.setTo(data.getString(AgileMessage.TO_EMAIl));
                message.setSubject(data.getString(AgileMessage.MESSAGE_SUBJECT));
                message.setMessageBody(data.getString(AgileMessage.MESSAGE_BODY));
                message.setSentDatetime(getDate(data.getString(AgileMessage.SENT_TIME)));
                message.setMediatype(Integer.parseInt(data.getString(AgileMessage.MEDIA_TYPE)));
                message.setDownloadUrl(data.getString(AgileMessage.DOWNLOAD_URL));
                message.setEncodedThumbnail(data.getString(AgileMessage.THUMBNAIL));
                message.setUsertype(AgileMessage.OTHER);
                message.setRead(false);
                agilemessagelistener.onMessageReceivedEvent(message);
                AgileData.init(context);
                AgileData.create(message);
                AgileData.deactivate();
            }catch (Exception e){
                //Log.d(TAG, "messagereceived: "+e.toString());
            }
        }
    };

    private Emitter.Listener deliveryReport = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                AgileMessage message = new AgileMessage();
                message.setId(data.getString(AgileMessage.MESSAGE_ID));
                message.setFrom(data.getString(AgileMessage.FROM_EMAIL));
                message.setFromName(data.getString(AgileMessage.FROM_NAME));
                message.setTo(data.getString(AgileMessage.TO_EMAIl));
                message.setSubject(data.getString(AgileMessage.MESSAGE_SUBJECT));
                message.setMessageBody(data.getString(AgileMessage.MESSAGE_BODY));
                message.setSentDatetime(getDate(data.getString(AgileMessage.SENT_TIME)));
                message.setDeliveredtDatetime(getDate(data.getString(AgileMessage.DELIVERED_TIME)));
                message.setMediatype(Integer.parseInt(data.getString(AgileMessage.MEDIA_TYPE)));
                message.setDownloadUrl(data.getString(AgileMessage.DOWNLOAD_URL));
                message.setEncodedThumbnail(data.getString(AgileMessage.THUMBNAIL));
                message.setUsertype(AgileMessage.USER);
                socket.emit(AgileConnection.AGILE_DELIVERY_VERIFIED, data);
                agilemessagelistener.onMessageDeliveredEvent(message);
                AgileData.init(context);
                AgileData.updateSentDeliveryTime(message);
                AgileData.deactivate();
            }catch (Exception e){
                //Log.d(TAG, AgileConnection.AGILE_DELIVERY_VERIFIED+": "+e.toString());
            }
        }
    };

    private Emitter.Listener sendreadReport = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                AgileData.init(context);
                AgileData.updateReadTime(data.getString(AgileMessage.MESSAGE_ID), getDate(data.getString(AgileMessage.READ_TIME)));
                AgileData.deactivate();
            }catch (Exception e){
                //Log.d(TAG, "sendreadReport: "+e.toString());
            }
        }
    };


    private Emitter.Listener receiveRead = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                socket.emit(AgileConnection.AGILE_RECEIVE_READ_VERIFIED, data);
                AgileMessage message = new AgileMessage();
                message.setId(data.getString(AgileMessage.MESSAGE_ID));
                message.setFrom(data.getString(AgileMessage.FROM_EMAIL));
                message.setTo(data.getString(AgileMessage.TO_EMAIl));
                message.setReadDatetime(getDate(data.getString(AgileMessage.READ_TIME)));
                agilemessagelistener.onMessageReadEvent(message);
                AgileData.init(context);
                AgileData.updateReadTime(data.getString(AgileMessage.MESSAGE_ID), getDate(data.getString(AgileMessage.READ_TIME)));
                AgileData.deactivate();
            }catch (Exception e){
                //Log.d(TAG, "sendreadReport: "+e.toString());
            }
        }
    };

    private Emitter.Listener startedTypingNotification = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                agiletypinglistener.onStartedTypingEvent(data.getString(AgileMessage.FROM_EMAIL));
            }catch (Exception e){
                //Log.d(TAG, "startedTypingNotification: "+e.toString());
            }
        }
    };

    private Emitter.Listener stoppedTypingNotification = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                    agiletypinglistener.onStoppedTypingEvent(data.getString(AgileMessage.FROM_EMAIL));
            }catch (Exception e){
                //Log.d(TAG, "stoppedTypingNotification: "+e.toString());
            }
        }
    };


    int count = 1;
    public void setAutomaticReconnection(){
        final Handler handler_light= new Handler();
        handler_light.postDelayed(new Runnable() {
            @Override
            public void run() {
                count++;
            }
        }, 1000);
    }

    @Override
    public void onAgileServerConnected() {
        count = 1;
    }

    @Override
    public void onAgileServerDisConnected() {
        setAutomaticReconnection();
    }

    private boolean isTyping = false;
    private Handler mTypingHandler = new Handler();
    private static final int TYPING_TIMER_LENGTH = 600;
    private String toEmail = "";

    public void enableTypingNotification(EditText edittext, final String toEmailServed){
        this.edittext = edittext;
        edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!isLoggedIn()){
                    return;
                }
                try{
                    toEmail = toEmailServed;
                    JSONObject obj = new JSONObject();
                    obj.put(AgileMessage.FROM_EMAIL, loggedUser.getEmail());
                    obj.put(AgileMessage.TO_EMAIl, toEmail);
                    if (!isTyping) {
                        isTyping = true;

                        socket.emit(AgileConnection.AGILE_START_TYPING_NOTIFICATION, obj);
                    }

                    mTypingHandler.removeCallbacks(onTypingTimeout);
                    mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
                }catch (Exception e){
                    //Log.d(TAG, e.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            try{
                if (!isTyping) return;

                isTyping = false;
                JSONObject obj = new JSONObject();
                obj.put(AgileMessage.FROM_EMAIL, loggedUser.getEmail());
                obj.put(AgileMessage.TO_EMAIl, toEmail);
                socket.emit(AgileConnection.AGILE_STOP_TYPING_NOTIFICATION, obj);
            }catch (Exception e){

            }
        }
    };

    public List<AgileMessage> getUserMessages(Context context, String loggedUser, String friend) {
        List<AgileMessage> messages;
        AgileData.init(context);
        messages = AgileData.getAll(loggedUser, friend);
        //Log.d(TAG, messages.size() + "");
        AgileData.deactivate();
        return messages;
    }

    public List<AgileMessage> getUnReadMessages(String loggedUser, String friend) {
        List<AgileMessage> messages;
        AgileData.init(context);
        messages = AgileData.getUnreadMessages(loggedUser, friend);
        //Log.d(TAG, messages.size() + "");
        AgileData.deactivate();
        return messages;
    }

    public List<AgileUser> getRecentChats(Context context, String userId) {
        List<AgileUser> messages;
        AgileData.init(context);
        messages = AgileData.getUsers(userId);
        ////Log.d(TAG, messages.size() + ""+loggedUser.getEmail());
        AgileData.deactivate();
        return messages;
    }

    public boolean setMessagesRead(Context context, List<AgileMessage> unreadMessages) {
        AgileData.init(context);
        AgileData.setMessageRead(unreadMessages);
        AgileData.deactivate();
        return true;
    }

    public void setMessagesReadInServer(Context context, List<AgileMessage> unreadMessages) {
        if(isConnected() && isLoggedIn()){
            for (AgileMessage message:unreadMessages){
                sendReadReportMethod(message);
            }
        }
    }

    public void sendReadReportMethod(AgileMessage message){
        try{
            JSONObject object = new JSONObject();
            object.put(AgileMessage.FROM_EMAIL, message.getFrom());
            object.put(AgileMessage.TO_EMAIl, message.getTo());
            object.put(AgileMessage.MESSAGE_ID, message.getId());
            socket.emit(AgileConnection.AGILE_SEND_READ_REPORT, object);
            Log.d(TAG, "sendReadReportMethod(): "+object.toString());
        }catch (Exception e){
            Log.d(TAG, "sendReadReportMethod().Error: "+e.toString());
        }
    }

    private String getDate(String dateString) {
        try{
            SimpleDateFormat formatter = new SimpleDateFormat(AgileConnection.DATE_FORMAT);
            formatter.setTimeZone(TimeZone.getTimeZone(AgileConnection.SERVER_Z));
            Date value = formatter.parse(dateString);;
            formatter = new SimpleDateFormat(AgileConnection.DATE_FORMAT);
            formatter.setTimeZone(TimeZone.getDefault());
            return formatter.format(value);
        }catch (Exception e){
            return dateString;
        }
    }

    public void saveMessage(Context context, AgileMessage message){
        AgileData.init(context);
        AgileData.create(message);
        AgileData.deactivate();
    }

    public void deleteMessages(Context context, List<AgileMessage> messagesList){
        AgileData.init(context);
        AgileData.deleteMessages(messagesList);
        AgileData.deactivate();
    }
}
