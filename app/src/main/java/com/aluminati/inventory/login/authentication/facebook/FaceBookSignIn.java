package com.aluminati.inventory.login.authentication.facebook;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.LinkAccounts;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;


public class FaceBookSignIn extends Fragment implements View.OnClickListener{

    private static final String TAG = "FaceBookSignIn";
    private static final String FaceBookProviderId = "facebook.com";
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

        if(firebaseAuth.getCurrentUser() == null){
            LoginManager.getInstance().logOut();
        }
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
                    UserFetch.update(firebaseAuth.getCurrentUser().getEmail(), "is_facebook_linked", true);
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
                                Utils.makeSnackBarWithButtons(getResources().getString(R.string.facebook_sign_infailed), facebookLogin, getActivity());
                            }
                        }
                    });
                }
            }
        });

    }



    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.facebook_loging_fragment){
            if(facebookLogin.getText().equals(getResources().getString(R.string.com_facebook_loginview_log_out_button))){
                new UnlinkFacebook(FaceBookProviderId, (result) ->  {
                    if(result){
                        Utils.makeSnackBarWithButtons("FaceBook Unlinked", facebookLogin, getActivity());
                        Log.d(TAG, "Facebook Unlinked");
                    }else{
                        Utils.makeSnackBarWithButtons("Failed to Unlink FaceBook", facebookLogin, getActivity());
                        Log.d(TAG, "Failed To Unlink Facebook");
                    }
                });
            }
        }
    }

    public static class UnlinkFacebook extends AsyncTask<String, String, Boolean>{

        private boolean unlinked = false;
        private String provider;

        private FacebookConsumer facebookConsumer;

        public interface FacebookConsumer {
            void accept(Boolean internet);
        }

       public UnlinkFacebook(String provider, FacebookConsumer facebookConsumer) {
            this.provider = provider;
            this.facebookConsumer = facebookConsumer;
            execute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            if(AccessToken.getCurrentAccessToken() == null){
                Log.d(TAG, "Invalid Facebook Access Token");
                return false;
            }

            new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions", null,
                    HttpMethod.DELETE, response -> LoginManager.getInstance().logOut()).executeAsync();


            if(FirebaseAuth.getInstance().getCurrentUser() != null){
                FirebaseAuth.getInstance().getCurrentUser().unlink(this.provider).addOnCompleteListener(result -> {
                    if(result.isSuccessful()){
                        Log.i(TAG, "Unlinked Facebook Successfully");
                        unlinked = true;
                    }else {
                        Log.i(TAG, "Failed To Unlink Facebook");
                    }
                });
            }

            return unlinked;
        }

        @Override
        protected void onPostExecute(Boolean unlinked) {
            facebookConsumer.accept(unlinked);
        }
    }

}
