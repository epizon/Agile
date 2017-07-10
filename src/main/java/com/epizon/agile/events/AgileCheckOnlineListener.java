package com.epizon.agile.events;

import com.epizon.agile.error.AgileLoginError;
import com.epizon.agile.objs.AgileUser;

/**
 * Created by Comp12 on 08-Sep-16.
 */
public interface AgileCheckOnlineListener {
    void onCheckOnline(String email, boolean isOnline, String lastSeen);
    void onUserLogin(String email);
    void onUserLogOut(String email, String lastSeen);
}
