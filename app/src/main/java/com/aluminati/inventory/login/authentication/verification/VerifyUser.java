package com.aluminati.inventory.login.authentication.verification;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.aluminati.inventory.HomeActivity;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.authentication.AuthenticationActivity;
import com.aluminati.inventory.register.RegisterActivity;
import com.aluminati.inventory.users.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class VerifyUser {

    private static final String TAG = "VerifyUser";


    public static ActionCodeSettings setActionCodeSettings(){
        return ActionCodeSettings.newBuilder()
                        .setUrl("https://aluminati.page.link/authentication")
                        .setHandleCodeInApp(true)
                        .setIOSBundleId("com.aluminati.inventory")
                        .setAndroidPackageName
                                (
                                "com.aluminati.inventory",
                                true, /* installIfNotAvailable */
                                "1.0"    /* minimumVersion */
                                )
                        .build();

    }


    public static void checkUser(FirebaseUser firebaseUser, Activity activity, int login_method){

            if(firebaseUser.getEmail() != null) {
                UserFetch.getUser(firebaseUser.getEmail()).addOnSuccessListener(task -> {
                    if (task.exists()) {
                        isUserVerified(new User(task), activity, false);
                    } else {
                        updateLayoutToRegisterActivity(activity, login_method);
                    }
                }).addOnFailureListener(result -> {
                    Log.w(TAG, "Failed to check is user verified");
                });
            }

    }

    public static void verifyUser(User user){
        if(user != null){
            if(UserFetch.userExists(user.getEmail())){
                UserFetch.update(user);
            }else {
                UserFetch.addNewUser(user);
            }
        }
    }

    public static void isUserVerified(User user, Activity activity, boolean register_activity){
        Log.i(TAG, "Is User Verified");
        if (user != null) {
            if (user.isPhoneVerified() && user.isEmailVerified()){
                Log.i(TAG, "User verified -> InfoPage Activity Intent");
                //TODO: this needs to be put in a frag activity.startActivity(new Intent(activity, UserProfile.class));
                activity.startActivity(new Intent(activity, HomeActivity.class));
                activity.finish();
            }else {
                Log.i(TAG, "User not veirifed -> Authentication Activity Intent");
                Intent intent = new Intent(activity, AuthenticationActivity.class);
                       intent.putExtra("user_reg", register_activity);
                activity.startActivity(intent);
                activity.finish();
            }
        }
    }

    public static Task<Void> verifyEmail(){
        return FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification(setActionCodeSettings());
    }


    public static void updateFireBaseUser(FirebaseUser firebaseUser, String name){
            firebaseUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build());
    }

    private static void updateLayoutToRegisterActivity(Activity activity, int loginMethod){
        Log.i(TAG, "Method " + loginMethod);
        Intent intent = new Intent(activity, RegisterActivity.class);
               intent.putExtra("login_method", loginMethod);
        activity.startActivity(intent);
    }



}
