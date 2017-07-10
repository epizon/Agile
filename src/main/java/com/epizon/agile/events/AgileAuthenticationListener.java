package com.epizon.agile.events;



import com.epizon.agile.error.AgileLoginError;
import com.epizon.agile.objs.AgileUser;

import java.util.List;

/**
 * Created by Comp12 on 06-Aug-16.
 */
public interface AgileAuthenticationListener {
    void onLoginSuccess(AgileUser loggedUser, List<AgileUser> friendList);
    void onLoginFailed(AgileLoginError error);
}
