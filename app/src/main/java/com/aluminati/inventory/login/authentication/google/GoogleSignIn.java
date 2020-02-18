package com.aluminati.inventory.login.authentication.google;

import android.app.Activity;
import android.content.Context;
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

import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.BaseFragment;
import com.aluminati.inventory.login.authentication.ForgotPasswordActivity;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
import com.aluminati.inventory.userprofile.UserProfile;
import com.aluminati.inventory.users.User;
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

public class GoogleSignIn extends Fragment implements View.OnClickListener {

    private static final String TAG = GoogleSignIn.class.getName();
    private static final String GoogleProviderId = "google.com";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private Button googleButton;
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.google_client_id)).requestEmail().build();
        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(getActivity(), googleSignInOptions);
        firebaseAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();


        return inflater.inflate(R.layout.google_signin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        googleButton = view.findViewById(R.id.google_signin_button);
        googleButton.setOnClickListener(this);

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
                alertDialog("Failed to LogIn", "Email is already registered, Login and link account or Recover Password")
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
                    alertDialog("Failed to sign ", "User already registered")
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
                Utils.makeSnackBarWithButtons("Unlinked Google", googleButton, getActivity());
                Log.d(TAG, "Google Unlinked");
                reload();
            }).addOnFailureListener(result -> {
                Log.w(TAG, "Failed to Unlink Google", result);
                Utils.makeSnackBarWithButtons("Failed to Unlink Google", googleButton, getActivity());
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
                            alertDialog("Failed To Link Accounts", "Google account is linked to another user")
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
        if (view.getId() == R.id.google_signin_button) {
            if (googleButton.getText().toString().equals(getResources().getString(R.string.login_google))) {
                signIn();
            } else if (googleButton.getText().toString().equals(getResources().getString(R.string.unlink_google))) {
                alertDialog("Unlink Google", "Are you sure to unlink your Google Account ?")
                        .setPositiveButton("Yes", (dialog, id) -> unlinkGoogle())
                        .setNegativeButton("No", (dialog, id) -> dialog.dismiss())
                        .create()
                        .show();
            }
        }
    }



}
