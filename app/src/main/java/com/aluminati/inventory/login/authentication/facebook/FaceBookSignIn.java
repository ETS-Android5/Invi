package com.aluminati.inventory.login.authentication.facebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.login.authentication.LogInActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.utils.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.forgotPassWord.ForgotPasswordActivity;
import com.aluminati.inventory.login.authentication.verification.VerificationStatus;
import com.aluminati.inventory.login.authentication.verification.VerifyUser;
import com.aluminati.inventory.userprofile.UserProfile;
import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.UserInfo;

import java.util.ArrayList;
import java.util.Collection;


public class FaceBookSignIn extends Fragment implements View.OnClickListener, OnStateChangeListener {

    private static final String TAG = FaceBookSignIn.class.getName();
    private static final String FaceBookProviderId = "facebook.com";
    private AccessTokenTracker accessTokenTracker;
    private SwipeButton facebookSwipeLogin;
    private Button facebookLogin;
    private CallbackManager callbackManager;
    private LoginManager loginManager;
    private FirebaseAuth firebaseAuth;
    private Collection<String> permissions;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = null;

        if(getActivity() instanceof LogInActivity){
            view = inflater.inflate(R.layout.facebook_signin, container, false);
        }else if(getActivity() instanceof UserProfile){
            view = inflater.inflate(R.layout.facebook_unlink, container, false);
        }

        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        permissions = new ArrayList<>();
        permissions.add("email");

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Log.i(TAG, "Facebook token revoked");
                    accessTokenTracker.stopTracking();
                }
            }
        };


        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(TAG, "Login Successful :");
                if(loginResult != null){
                    Log.i(TAG, "Login Successful :" + loginResult.getAccessToken());
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }else {
                    Log.i(TAG, "Unable To Login");
                }
            }
            @Override
            public void onCancel() {
                Log.i(TAG, "Login Canceled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.w(TAG, "Login Error", error);
            }
        });


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if(getActivity() instanceof LogInActivity){
            facebookSwipeLogin = view.findViewById(R.id.facebook_loging_fragment);
            facebookSwipeLogin.setOnStateChangeListener(this);
        }else if(getActivity() instanceof UserProfile){
            facebookLogin = view.findViewById(R.id.facebook_unlink_button);
            facebookLogin.setOnClickListener(this);
            isFacebookLinked();
        }




    }

    private void isFacebookLinked(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            for(UserInfo userInfo : FirebaseAuth.getInstance().getCurrentUser().getProviderData()){
                if(userInfo.getProviderId().equals(FaceBookProviderId)){
                    facebookLogin.setText(getString(R.string.unlink_facebook));
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        isFacebookLinked();
    }

    @Override
    public void onStop() {
        super.onStop();
        loginManager.unregisterCallback(callbackManager);
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        Log.i(TAG, "HandlingFacebookAceessToken:" + accessToken);


        final AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());
        if(firebaseAuth.getCurrentUser() != null){
            if(getContext() instanceof UserProfile){
                Log.i(TAG, "Linking Facebook");
                linkAccounts(authCredential);
            }
        }else {
            firebaseAuth.signInWithCredential(authCredential).addOnSuccessListener(result -> {
                Log.d(TAG, "SignInWithGoogle:success");
                VerifyUser.checkUser(firebaseAuth.getCurrentUser(), getActivity(), VerificationStatus.FACEBOOK);
                accessTokenTracker.startTracking();
            }).addOnFailureListener(result -> {
                Log.w(TAG, "Log In With Go", result);
                if (result instanceof FirebaseAuthUserCollisionException) {
                    Log.d(TAG, "Log In Failed", result);
                    alertDialog("Log In Error", "Account linked is associated with anther user\n\nLog in to link account or recover password")
                            .setPositiveButton("Ok", (dialog, i) -> dialog.dismiss())
                            .setNegativeButton("Recover Password", ((dialogInterface, i) -> {
                                startActivity(new Intent(getActivity(), ForgotPasswordActivity.class));
                            }))
                            .create()
                            .show();

                } else {
                    Utils.makeSnackBarWithButtons(getResources().getString(R.string.facebook_sign_infailed), facebookLogin, getActivity());
                }
            });
        }
    }

    private AlertDialog.Builder alertDialog(String title, String message){
        return new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(true);
    }

    private void linkAccounts(AuthCredential credential) {

        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnSuccessListener(result -> {
                        Log.d(TAG, "linkWithCrediential:success");
                        facebookLogin.setText(getResources().getString(R.string.unlink_facebook));
                        UserFetch.update(firebaseAuth.getCurrentUser().getEmail(), "is_facebook_linked", true);
                        Utils.makeSnackBar("Facebook Linked", facebookLogin, getActivity());
                        accessTokenTracker.startTracking();
                    })
                    .addOnFailureListener(getActivity(), result -> {
                        Log.w(TAG, "linkWithCreditential:failed", result);
                    });
        }
    }

    private void reload(){
        FirebaseAuth.getInstance().getCurrentUser().reload().addOnSuccessListener(unlink -> {
            Log.i(TAG, "Successfully reloaded user");
                facebookLogin.setText(getResources().getString(R.string.link_faebook));
        }).addOnFailureListener(unlink -> Log.w(TAG, "Failed to reload user", unlink));
    }

    private void unLinkFaceBook(){

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions", null,
                HttpMethod.DELETE, response -> LoginManager.getInstance().logOut()).executeAsync();

        FirebaseAuth.getInstance().getCurrentUser().unlink(FaceBookProviderId).addOnSuccessListener(authResult -> {
            Log.d(TAG, "Facebook Unlinked");
            Utils.makeSnackBarWithButtons("FaceBook Unlinked", facebookLogin, getActivity());
            UserFetch.update(firebaseAuth.getCurrentUser().getEmail(), "is_facebook_linked", false);
            reload();
        }).addOnFailureListener(result -> {
            Utils.makeSnackBarWithButtons("Failed to Unlink FaceBook", facebookLogin, getActivity());
            Log.w(TAG, "Failed to unlink Facebook", result);
        });

    }

    private void unLinkConfirm(){
        alertDialog("Facebook Unlink", "Are you sure you want to unlink Facebook Account")
                .setPositiveButton("Yes", (dialog, i) -> unLinkFaceBook())
                .setNegativeButton("No", (dialog, i) -> dialog.dismiss())
                .create()
                .show();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.facebook_unlink_button){
            Log.i(TAG, "Logging");

            if(facebookLogin.getText().equals(getResources().getString(R.string.unlink_facebook))){
                Log.i(TAG, "Logging =");

                unLinkConfirm();
            }else if(facebookLogin.getText().equals(getResources().getString(R.string.link_faebook))){
                Log.i(TAG, "Logging +");
                if(getActivity() == null){
                    Log.i(TAG, "Activity is null");
                }
                loginManager.logIn(this, permissions);
            }
        }
    }


    @Override
    public void onStateChange(boolean active) {
        if(active){
            loginManager.logIn(this, permissions);
        }
    }
}
