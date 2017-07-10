package com.epizon.agile.events;

/**
 * Created by Comp12 on 26-Jul-16.
 */
public interface AgileConnectionListener {
    void onAgileServerConnected();
    void onAgileServerDisConnected();
}
