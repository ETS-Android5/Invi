package com.aluminati.inventory.login.authentication.twitter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.paris.Paris;
import com.aluminati.inventory.LogInActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.userprofile.UserProfile;
import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.TwitterAuthCredential;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.auth.UserInfo;

public class TwitterSignIn extends Fragment implements View.OnClickListener, OnStateChangeListener
{


    private static final String TAG = TwitterSignIn.class.getName();
    private static final String TwiiterProviderId = "twitter.com";
    private Task<AuthResult> pendingResultTask;
    private OAuthProvider.Builder provider;
    private FirebaseAuth firebaseAuth;
    private Button twitterButton;
    private SwipeButton twitterSwipeButton;



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getActivity() instanceof LogInActivity){
            twitterSwipeButton = view.findViewById(R.id.twitter_swipe_button);
            twitterSwipeButton.setOnStateChangeListener(this);
        }else if(getActivity() instanceof UserProfile){
            twitterButton = view.findViewById(R.id.twitter_button);
            twitterButton.setOnClickListener(this);
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        provider = OAuthProvider.newBuilder(TwiiterProviderId);
        firebaseAuth = FirebaseAuth.getInstance();

        pendingResultTask = firebaseAuth.getPendingAuthResult();

        isTwitterLinked();

        View view = null;
        if(getActivity() instanceof LogInActivity){
            view = inflater.inflate(R.layout.twitter_signin, container, false);
        }else if(getActivity() instanceof UserProfile){
            view = inflater.inflate(R.layout.twitter_unlink, container, false);
        }

        return view;
    }




    private void signIn(){
        if (pendingResultTask != null) {
            pendingResultTask.addOnSuccessListener(authResult -> {
                Log.i(TAG, "Twitter Successfully loged in");

            }).addOnFailureListener(e -> Log.w(TAG, "Failed to login Twitter", e));
        } else {
            firebaseAuth.startActivityForSignInWithProvider(getActivity(), provider.build())
                    .addOnSuccessListener(authResult -> {
                        Log.i(TAG, "Twitter Successfully loged in");
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to login Twitter", e));
        }
    }

    private void signInWithTwitter(AuthResult authResult){
        if(firebaseAuth.getCurrentUser() != null){
            if(getActivity() instanceof UserProfile){

            }else{

            }
        }else {
            signIn();
        }
    }

    private void isTwitterLinked(){
        if(firebaseAuth.getCurrentUser() != null) {
            for (UserInfo userInfo :firebaseAuth.getCurrentUser().getProviderData()) {
                if (userInfo.getProviderId().equals(TwiiterProviderId)) {
                    twitterButton.setText(getResources().getString(R.string.logout_twitter));
                }
            }
        }
    }

    private void linkAccounts(AuthCredential credential) {

        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnSuccessListener(result -> {
                        Log.d(TAG, "linkWithCrediential:success");
                        twitterButton.setText(getResources().getString(R.string.logout_twitter));
                        //UserFetch.update(firebaseAuth.getCurrentUser().getEmail(), "is_facebook_linked", true);
                        Utils.makeSnackBar("Twitter Linked", twitterButton, getActivity());

                    })
                    .addOnFailureListener(getActivity(), result -> {
                        Log.w(TAG, "linkWithCreditential:failed", result);
                    });
        }
    }

    private void unlinkTwitter(){
        if(firebaseAuth.getCurrentUser() != null){

        }
    }






    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.twitter_button){
            if(twitterButton.getText().toString().equals(getResources().getString(R.string.logout_twitter))){

            }else if(twitterButton.getText().toString().equals(getResources().getString(R.string.signin_with_twitter))){
                signIn();
            }
        }
    }

    @Override
    public void onStateChange(boolean active) {

    }
}
