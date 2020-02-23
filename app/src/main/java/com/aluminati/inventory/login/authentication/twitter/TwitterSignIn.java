package com.aluminati.inventory.login.authentication.twitter;

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
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.verification.VerificationStatus;
import com.aluminati.inventory.login.authentication.verification.VerifyUser;
import com.aluminati.inventory.login.authentication.password.PassWordReset;
import com.aluminati.inventory.userprofile.UserProfile;
import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.UserInfo;

public class TwitterSignIn extends Fragment implements View.OnClickListener, OnStateChangeListener {


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

        isTwitterLinked();

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        provider = OAuthProvider.newBuilder(TwiiterProviderId);
        firebaseAuth = FirebaseAuth.getInstance();

        pendingResultTask = firebaseAuth.getPendingAuthResult();


        View view = null;
        if(getActivity() instanceof LogInActivity){
            view = inflater.inflate(R.layout.twitter_signin, container, false);
        }else if(getActivity() instanceof UserProfile){
            view = inflater.inflate(R.layout.twitter_unlink, container, false);
        }

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        isTwitterLinked();
    }

    private void signIn(){
        if (pendingResultTask != null) {
            pendingResultTask.addOnSuccessListener(authResult -> {
                Log.i(TAG, "Twitter Successfully loged in");
                VerifyUser.checkUser(authResult.getUser(), getActivity(), VerificationStatus.TWITTER);
            }).addOnFailureListener(e -> {
                if(e instanceof FirebaseAuthUserCollisionException){
                    userExists();
                }
                Log.w(TAG, "Failed to login Twitter", e);
                Utils.makeSnackBar("Failed to Login", twitterSwipeButton, getActivity());
            });
        } else {
            firebaseAuth.startActivityForSignInWithProvider(getActivity(), provider.build())
               .addOnSuccessListener(authResult -> {
                   Log.i(TAG, "Twitter Successfully loged in" + authResult.getUser().getEmail());
                   VerifyUser.checkUser(authResult.getUser(), getActivity(), VerificationStatus.TWITTER);
               })
               .addOnFailureListener(e -> {
                   if(e instanceof FirebaseAuthUserCollisionException){
                       userExists();
                   }
                   Log.w(TAG, "Failed to login Twitter", e);
                   Utils.makeSnackBar("Failed to Login", twitterSwipeButton, getActivity());
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

    private void userExists(){
        alertDialog("Login Error", "Account linked is associated with anther user\n\n Log in to link account or recover password")
                .setPositiveButton("Ok", (dialog, i) -> dialog.dismiss())
                .setNegativeButton("Recoverd Password", ((dialogInterface, i) -> {
                    startActivity(new Intent(getActivity(), PassWordReset.class));
                }))
                .create()
                .show();
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

    private void linkAccounts() {

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser != null) {

            firebaseUser.startActivityForLinkWithProvider(getActivity(), provider.build())
                    .addOnSuccessListener(
                            authResult -> {
                                Log.i(TAG, "Twitter account linked");
                                Utils.makeSnackBar("Twitter Account Linked", twitterButton, getActivity());
                                UserFetch.update(firebaseUser.getEmail(), "is_twitter_linked", true);
                            })
                    .addOnFailureListener(result -> {
                        Log.w(TAG, "Failed to linke Twitter", result);
                        Utils.makeSnackBar("Failed to Link Twitter", twitterButton, getActivity());
                    });

        }

    }

    private void unlinkTwitter(){
        if(firebaseAuth.getCurrentUser() != null){
            firebaseAuth.getCurrentUser().unlink(TwiiterProviderId)
                    .addOnSuccessListener(result -> {
                        Log.i(TAG, "Twitter unlinked");
                        twitterButton.setText(getResources().getString(R.string.link_twiiter));
                        Utils.makeSnackBar("Twitter unlinked", twitterButton, getActivity());
                    }).addOnFailureListener(result -> {
                        Log.w(TAG, "Failed to unlink Twitter account", result);
                        Utils.makeSnackBarWithButtons("Failed to unlink Twitter", twitterButton, getActivity());
            });
        }
    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.twitter_button){
               if(twitterButton.getText().equals(getResources().getString(R.string.link_twiiter))){
                    linkAccounts();
               }else if(twitterButton.getText().equals(getResources().getString(R.string.unlink_twitter))){
                    unlinkTwitter();
               }
        }
    }

    @Override
    public void onStateChange(boolean active) {
        if(active){
            signIn();
        }
    }
}
