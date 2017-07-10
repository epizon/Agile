package com.epizon.agile;

/**
 * Created by Comp12 on 21-Aug-16.
 */
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.epizon.agile.Agile;
import com.epizon.agile.error.AgileLoginError;
import com.epizon.agile.events.AgileAuthenticationListener;
import com.epizon.agile.events.AgileConnectionListener;
import com.epizon.agile.events.AgileMessageListener;
import com.epizon.agile.events.AgileTypingListener;
import com.epizon.agile.objs.AgileMessage;
import com.epizon.agile.objs.AgileUser;

import java.util.List;

public abstract class AgileListenerService extends Service implements AgileMessageListener, AgileTypingListener, AgileConnectionListener{

    String TAG = "AGILE_LIBRARY";
    Agile agile;

    public AgileListenerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUpAgile();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    public void setUpAgile(){
        agile = Agile.getInstance();
        agile.setAgileMessageListener(this);
        agile.setAgileTypingListener(this);
        agile.setAgileConnectionListener(this);
    }

    public void loginToAgile(){
        agile.login("sibin@gmail.com", "pass", new AgileAuthenticationListener() {
            @Override
            public void onLoginSuccess(final AgileUser loggedUser, List<AgileUser> friendList) {
            }

            @Override
            public void onLoginFailed(AgileLoginError error) {
            }
        });
    }

    @Override
    public void onMessageReceivedEvent(AgileMessage receivedMessage) {
        onMessageReceived(receivedMessage);
    }

    @Override
    public void onMessageSentEvent(AgileMessage sentMessage) {
        onMessageSentSuccessfully(sentMessage);
    }

    @Override
    public void onMessageDeliveredEvent(AgileMessage deliveredMessage) {
        onMessageDelivered(deliveredMessage);
    }

    @Override
    public void onMessageSendFailedEvent() {
    }

    @Override
    public void onMessageReadEvent(AgileMessage readMessage) {
        onMessageRead(readMessage);
    }

    @Override
    public void onStartedTypingEvent(String user) {
        onStartedTyping(user);
    }

    @Override
    public void onStoppedTypingEvent(String user) {
        onStoppedTyping(user);
    }

    @Override
    public void onAgileServerConnected() {
        onAgileConnected();
    }

    @Override
    public void onAgileServerDisConnected() {
        onAgileDisConnected();
    }

    public abstract void onMessageReceived(AgileMessage receivedMessage);
    public abstract void onMessageSentSuccessfully(AgileMessage sentMessage);
    public abstract void onMessageDelivered(AgileMessage deliveredMessage);
    public abstract void onMessageRead(AgileMessage readMessage);
    public abstract void onMessageSendFailed(AgileMessage message);
    public abstract void onStartedTyping(String user);
    public abstract void onStoppedTyping(String user);
    public abstract void onAgileConnected();
    public abstract void onAgileDisConnected();
}
