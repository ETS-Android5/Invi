package com.aluminati.inventory.login.authentication;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.aluminati.inventory.InfoPageActivity;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.register.RegisterActivity;
import com.aluminati.inventory.users.User;
import com.google.firebase.auth.FirebaseUser;

public class VerifyUser {

    private static final String TAG = "VerifyUser";
    private static int loginMethod;

    public static void checkUser(FirebaseUser firebaseUser, Activity activity, int login_method){
        loginMethod = login_method;

            if(firebaseUser.getEmail() != null) {
                UserFetch.getUser(firebaseUser.getEmail()).addOnCompleteListener(task -> {
                    if (task.getResult() != null) {
                        if (task.getResult().exists()) {
                            isUserVerified(new User(task.getResult()), activity, false);
                        } else {
                            updateLayoutToRegisterActivity(new User(firebaseUser), activity);
                        }
                    }
                });
            }else {
                updateLayoutToRegisterActivity(new User(firebaseUser),activity);
            }

    }

    public static void userExists(User user){
        if(user != null){
            UserFetch.getUser(user.getEmail()).addOnCompleteListener(task -> {
                if(task.getResult() != null){
                    Log.i(TAG, "Upadting User");
                    UserFetch.update(user);
                } else {
                    Log.i(TAG, "Adding New User");
                    UserFetch.addNewUser(user);
                }
            });
        }
    }


    public static void isUserVerified(User user, Activity activity, boolean register_activity){
        Log.i(TAG, "Is User Verified");
        if (user != null) {
            if (user.isPhoneVerified() && user.isEmailVerified()){
                Log.i(TAG, "User verified -> InfoPage Activity Intent");
                activity.startActivity(new Intent(activity, InfoPageActivity.class));
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


    private static void updateLayoutToRegisterActivity(User fireBaseUser, Activity activity){
        Log.i(TAG, "Method " + loginMethod);
        Intent intent = new Intent(activity, RegisterActivity.class);
               intent.putExtra("user_info", fireBaseUser);
               intent.putExtra("login_method", loginMethod);
        activity.startActivity(intent);
        activity.finish();
    }



}
