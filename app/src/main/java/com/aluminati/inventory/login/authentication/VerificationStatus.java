package com.aluminati.inventory.login.authentication;

public class VerificationStatus {

    public static final int VERIFICATION_STARTED = 1001;
    public static final int VERIFICATION_FAILED = 1002;
    public static final int VERIFCIATION_SUCCESSFUL = 1003;
    public static final int VERIFICATION_CANCELED = 1004;

    public static final int NO_EMAIL_PROVIDED = 2001;
    public static final int EMAIL_NOT_VERIFIED = 2002;
    public static final int EMAIL_VERIFIED = 2003;
    public static final int EMAIL_NOT_PROVIDED = 2004;

    public static final int EMAIL = 5001;
    public static final int GOOGLE = 5002;
    public static final int FACEBOOK = 5003;
    public static final int PHONE = 5004;
    public static final int FIREBASE = 5005;
}
