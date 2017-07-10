package com.epizon.agile.events;


import com.epizon.agile.error.AgileRegistrationError;

/**
 * Created by Comp12 on 27-Jul-16.
 */
public interface AgileRegistrationListener {
    void onRegistrationFailed(AgileRegistrationError error);
    void onRegistrationSuccess(String success);
}
