package com.epizon.agile.error;

/**
 * Created by Comp12 on 31-Jul-16.
 */
public class AgileLoginError {
    public static final int USER_DOESNT_EXISTS = 1;
    int errorType;

    public void setErrorType(int errorType) {
        this.errorType = errorType;
    }

    public int getErrorType() {
        return errorType;
    }
}
