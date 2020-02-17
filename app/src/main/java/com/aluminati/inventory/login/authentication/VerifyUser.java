package com.aluminati.inventory.login.authentication;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.aluminati.inventory.InfoPageActivity;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.register.RegisterActivity;
import com.aluminati.inventory.userprofile.UserProfile;
import com.aluminati.inventory.users.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class VerifyUser {

    private static final String TAG = "VerifyUser";
    private static int loginMethod;


    public static ActionCodeSettings setActionCodeSettings(){
        return ActionCodeSettings.newBuilder()
                        // URL you want to redirect back to. The domain (www.example.com) for this
                        // URL must be whitelisted in the Firebase Console.
                        .setUrl("https://aluminati.page.link/authentication")
                        // This must be true
                        .setHandleCodeInApp(true)
                        .setIOSBundleId("com.aluminati.inventory")
                        .setAndroidPackageName(
                                "com.aluminati.inventory",
                                true, /* installIfNotAvailable */
                                "1.0"    /* minimumVersion */)
                        .build();

    }


    public static void checkUser(FirebaseUser firebaseUser, Activity activity, int login_method){
        loginMethod = login_method;

            if(firebaseUser.getEmail() != null) {
                UserFetch.getUser(firebaseUser.getEmail()).addOnCompleteListener(task -> {
                    if (task.getResult() != null) {
                        if (task.getResult().exists()) {
                            isUserVerified(new User(task.getResult()), activity, false);
                        } else {
                            updateLayoutToRegisterActivity(activity);
                        }
                    }
                });
            }else {
                updateLayoutToRegisterActivity(activity);
            }

    }

    public static void userExists(User user){
        if(user != null){
            UserFetch.getUser(user.getEmail()).addOnCompleteListener(task -> {
                if(task.getResult() != null) {
                    if (task.getResult().exists()) {
                        Log.i(TAG, "Upadting User");
                        UserFetch.update(user);
                    } else {
                        Log.i(TAG, "Adding New User");
                        UserFetch.addNewUser(user);
                    }
                }
            });
        }
    }


    public static void isUserVerified(User user, Activity activity, boolean register_activity){
        Log.i(TAG, "Is User Verified");
        if (user != null) {
            if (user.isPhoneVerified() && user.isEmailVerified()){
                Log.i(TAG, "User verified -> InfoPage Activity Intent");
                activity.startActivity(new Intent(activity, UserProfile.class));
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

    private static void updateLayoutToRegisterActivity(Activity activity){
        Log.i(TAG, "Method " + loginMethod);
        Intent intent = new Intent(activity, RegisterActivity.class);
               intent.putExtra("login_method", loginMethod);
        activity.startActivity(intent);
        activity.finish();
    }



}
