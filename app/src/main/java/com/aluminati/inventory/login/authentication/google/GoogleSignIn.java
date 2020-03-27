package com.aluminati.inventory.login.authentication.google;

import android.app.Activity;
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
import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import static com.aluminati.inventory.Constants.GoogleProviderId;

public class GoogleSignIn extends Fragment implements View.OnClickListener, OnStateChangeListener {

    private static final String TAG = GoogleSignIn.class.getName();
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private Button googleButton;
    private SwipeButton googleSwipeButton;
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("617397433442-urgnv43j7ik4obmqvoemf1huiv0pcnl1.apps.googleusercontent.com").requestEmail().build();
        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(getActivity(), googleSignInOptions);
        firebaseAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();

        View view = null;

        if(getActivity() instanceof LogInActivity){
            view = inflater.inflate(R.layout.google_signin, container, false);
            googleSwipeButton = view.findViewById(R.id.google_signin_button);
            googleSwipeButton.setOnStateChangeListener(this);
        }else if(getActivity() instanceof UserProfile){
            view = inflater.inflate(R.layout.google_unlink, container, false);
            googleButton = view.findViewById(R.id.google_button);
            googleButton.setOnClickListener(this);
        }

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        reload();
    }

    private void isGoogleLinked(){
        if(firebaseAuth.getCurrentUser() != null) {
            for (UserInfo userInfo : firebaseAuth.getCurrentUser().getProviderData()) {
                if(userInfo.getProviderId().equals(GoogleProviderId)){
                    googleButton.setText(getResources().getString(R.string.unlink_google));
                }
            }
        }
    }

    private void signIn() {
        Intent googleSignInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(googleSignInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);
                if (googleSignInAccount != null) {
                    firebaseAuthWithGoogle(googleSignInAccount);
                } else {
                    Log.d(TAG, "GoogleSignInClient:returned => null");
                }
            } catch (ApiException e) {
                Log.w(TAG, "Google Sign In Failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "FireBaseAuthWithGoogle:" + googleSignInAccount.getId());

        final AuthCredential credential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

        if(firebaseAuth.getCurrentUser() != null){
            if(getContext() instanceof UserProfile){
                linkAccounts(credential, getActivity(), "is_google_linked", TAG);
            }else {
                alertDialog(getResources().getString(R.string.login_error), getResources().getString(R.string.accout_linked_allready))
                        .setPositiveButton("Ok", (dialog, id) -> dialog.cancel())
                        .setNegativeButton("Recover Password", (dialog, id) -> getContext().startActivity(new Intent(getContext(), ForgotPasswordActivity.class)))
                        .create()
                        .show();
            }
        }else {
            firebaseAuth.signInWithCredential(credential).addOnSuccessListener(task -> {
                Log.d(TAG, "SignInWithGoogle:success");
                VerifyUser.checkUser(firebaseAuth.getCurrentUser(), getActivity(), VerificationStatus.GOOGLE);
            }).addOnFailureListener(result -> {
                Log.w(TAG, "Failed to Log In", result);
                if (result instanceof FirebaseAuthUserCollisionException) {
                    alertDialog(getResources().getString(R.string.login_error), getResources().getString(R.string.accout_linked_allready))
                            .setPositiveButton("Ok", ((dialogInterface, i) -> dialogInterface.dismiss()))
                            .create()
                            .show();
                } else {
                    Utils.makeSnackBarWithButtons(getResources().getString(R.string.login_failed), googleButton, getActivity());
                }
            });
        }
    }




    private void unlinkGoogle() {
        if(firebaseAuth.getCurrentUser() != null){
            firebaseAuth.getCurrentUser().unlink(GoogleProviderId).addOnSuccessListener(getActivity(), result -> {
                UserFetch.update(firebaseAuth.getCurrentUser().getEmail(), "is_google_linked", false);
                googleButton.setText(getResources().getString(R.string.login_google));
                Utils.makeSnackBarWithButtons(getResources().getString(R.string.google_unlinked), googleButton, getActivity());
                Log.d(TAG, "Google Unlinked");
                reload();
            }).addOnFailureListener(result -> {
                Log.w(TAG, "Failed to Unlink Google", result);
                Utils.makeSnackBarWithButtons(getResources().getString(R.string.failed_unlink_account), googleButton, getActivity());
            });
        }
    }

    private void linkAccounts(AuthCredential credential, Activity activity, String signInMethod, String tag) {

        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnSuccessListener(result -> {
                        Log.d(tag, "linkWithCrediential:success");
                        UserFetch.update(firebaseAuth.getCurrentUser().getEmail(), signInMethod, true);
                        reload();
                    })
                    .addOnFailureListener(activity, result -> {
                        if(result instanceof FirebaseAuthUserCollisionException){
                            alertDialog(getResources().getString(R.string.login_error), getResources().getString(R.string.accout_linked_allready))
                                    .setPositiveButton("Ok", ((dialogInterface, i) -> dialogInterface.dismiss()))
                                    .create()
                                    .show();
                        }
                        Log.w(tag, "linkWithCreditential:failed", result);
                    });
        }
    }

    private void reload(){
        if(firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.getCurrentUser().reload().addOnSuccessListener(reload -> {
                Log.i(TAG, "Reloaded user successfully");
                isGoogleLinked();
            }).addOnFailureListener(reload -> {
                Log.w(TAG, "Failed to reload user", reload);
            });
        }
    }

    private AlertDialog.Builder alertDialog(String title, String message){

        return new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.google_button) {
            if (googleButton.getText().toString().equals(getResources().getString(R.string.link_google))) {
                signIn();
            } else if (googleButton.getText().toString().equals(getResources().getString(R.string.unlink_google))) {
                alertDialog(getResources().getString(R.string.unlink_google), getResources().getString(R.string.unlink_google_msg))
                        .setPositiveButton("Yes", (dialog, id) -> unlinkGoogle())
                        .setNegativeButton("No", (dialog, id) -> dialog.dismiss())
                        .create()
                        .show();
            }
        }
    }


    @Override
    public void onStateChange(boolean active) {
        if(active){
            googleSwipeButton.dispatchSetActivated(false);
            signIn();
        }
    }
}
