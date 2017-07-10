package com.epizon.agile.error;

/**
 * Created by Comp12 on 28-Jul-16.
 */
public class AgileRegistrationError {
    public static final int REGISTRATION_SUCCESS = 0;
    public static final int USER_ALREADY_EXISTS = 1;
    public static final int USER_INSERTION_ERROR = 2;
    int errorType;

    public void setErrorType(int errorType) {
        this.errorType = errorType;
    }

    public int getErrorType() {
        return errorType;
    }
}
