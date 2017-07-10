package com.epizon.agile.events;

import com.epizon.agile.error.AgileRegistrationError;

/**
 * Created by Comp12 on 12-Aug-16.
 */
public interface AgileTypingListener {
    void onStartedTypingEvent(String user);
    void onStoppedTypingEvent(String user);
}
