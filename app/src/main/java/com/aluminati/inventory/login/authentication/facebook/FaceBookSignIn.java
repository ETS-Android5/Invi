package com.aluminati.inventory.login.authentication.facebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.LinkAccounts;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class FaceBookSignIn extends Fragment{

    private static final String TAG = "FaceBookSignIn";
    private FirebaseAuth firebaseAuth;
    private LoginButton facebookLogin;
    private CallbackManager callbackManager;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.facebook_signin, container, false);

        callbackManager = CallbackManager.Factory.create();

        facebookLogin = view.findViewById(R.id.facebook_loging_fragment);
        facebookLogin.setPermissions("email");

        facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
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

        firebaseAuth = FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken accessToken){
        Log.i(TAG, "HandlingFacebookAceessToken:" + accessToken);
        final AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    try {
                        Log.i(TAG,"FacebookSignIn:success" + firebaseAuth.getCurrentUser().getEmail());
                        VerifyUser.checkUser(firebaseAuth.getCurrentUser(), getActivity(), VerificationStatus.FACEBOOK);
                    }catch (NullPointerException e){
                        Log.w(TAG, "Email empty", e);
                    }
                } else {

                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                        FirebaseAuth.getInstance().signOut();
                        Log.w(TAG, "FacebookSignIn:failed because", task.getException());
                        LinkAccounts.linkAccountsInfo(getContext(), "Email is already registered, login and link account" +
                                " or recover password");
                    }

                    Toast.makeText(getActivity(), "Authentication Failed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }



}
