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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.aluminati.inventory.MainActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.fragments.fragmentListeners.phone.PhoneVerificationSender;
import com.aluminati.inventory.login.authentication.LinkAccounts;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
import com.aluminati.inventory.userprofile.UserProfile;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;


public class FaceBookSignIn extends Fragment
{

    private static final String TAG = "FaceBookSignIn";
    private FirebaseAuth firebaseAuth;
    private LoginButton facebookLogin;
    private CallbackManager callbackManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.facebook_signin, container, true);


        FacebookSdk.sdkInitialize(getActivity());


        callbackManager = CallbackManager.Factory.create();

        facebookLogin = view.findViewById(R.id.facebook_loging_fragment);
        facebookLogin.setPermissions("email");



        firebaseAuth = FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "Request code " + requestCode + " resultCode + " + resultCode);
    }

    private void disconnectFacebook(String providerId){
        if(AccessToken.getCurrentAccessToken() == null){
            return;
        }

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions", null,
                HttpMethod.DELETE, response -> LoginManager.getInstance().logOut()).executeAsync();


        if(firebaseAuth.getCurrentUser() != null){
            firebaseAuth.getCurrentUser().unlink(providerId).addOnCompleteListener(result -> {
                if(result.isSuccessful()){

                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if(loginResult != null){
                    Log.i(TAG, "Login Successful :" + loginResult.getAccessToken());
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }else {
                    FirebaseAuth.getInstance().signOut();
                    LoginManager.getInstance().logOut();
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
    }

    @Override
    public void onStop() {
        super.onStop();
        facebookLogin.unregisterCallback(callbackManager);
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        Log.i(TAG, "HandlingFacebookAceessToken:" + accessToken);
        final AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());

        UserFetch.getUser(firebaseAuth.getCurrentUser().getEmail()).addOnCompleteListener(result -> {
            if (result.isSuccessful() && result.getResult() != null) {
                if (result.getResult().exists()) {
                    LinkAccounts.linkAccounts(authCredential, getActivity(), TAG);
                } else {
                    firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(signInResult -> {
                        if (signInResult.isSuccessful()) {
                            Log.i(TAG, "FacebookSignIn:success" + firebaseAuth.getCurrentUser().getEmail());
                            VerifyUser.checkUser(firebaseAuth.getCurrentUser(), getActivity(), VerificationStatus.GOOGLE);
                            UserFetch.update(firebaseAuth.getCurrentUser().getEmail(), "is_facebook_linked", true);
                        } else {
                            if (signInResult.getException() instanceof FirebaseAuthUserCollisionException) {
                                FirebaseAuth.getInstance().signOut();
                                LoginManager.getInstance().logOut();
                                Log.w(TAG, "FacebookSignIn:failed because", signInResult.getException());
                                LinkAccounts.linkAccountsInfo(getContext(), "Email is already registered, login and link account or recover password");
                                Utils.makeSnackBar(getResources().getString(R.string.facebook_sign_infailed), facebookLogin, getActivity());
                            }
                        }
                    });
                }
            }
        });

    }

}
