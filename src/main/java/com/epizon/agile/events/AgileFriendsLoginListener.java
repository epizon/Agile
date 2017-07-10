package com.epizon.agile.events;


import com.epizon.agile.objs.AgileUser;

/**
 * Created by Comp12 on 01-Aug-16.
 */
public interface AgileFriendsLoginListener {
    void onFriendLogged(AgileUser friend);
}
