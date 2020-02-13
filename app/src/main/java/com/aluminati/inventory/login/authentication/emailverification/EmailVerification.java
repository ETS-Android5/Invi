package com.aluminati.inventory.login.authentication.emailverification;


import com.google.firebase.auth.ActionCodeSettings;

public class EmailVerification {

    private static final String TAG = "EmailVerification";

    private static ActionCodeSettings actionCodeSettings(){
        return ActionCodeSettings.newBuilder()
                        .setUrl("https://aluminati.page.link/authentication")
                        .setHandleCodeInApp(true)
                        .setIOSBundleId("com.aluminati.inventory")
                        .setAndroidPackageName(
                                "com.aluminati.inventory",
                                true, /* installIfNotAvailable */
                                "12"    /* minimumVersion */)
                        .build();

    }


}
