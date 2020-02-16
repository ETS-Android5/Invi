package com.aluminati.inventory.login.authentication.google;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.MainActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.fragments.fragmentListeners.phone.PhoneVerificationReciever;
import com.aluminati.inventory.fragments.fragmentListeners.phone.PhoneVerificationSender;
import com.aluminati.inventory.login.authentication.LinkAccounts;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
import com.aluminati.inventory.userprofile.UserProfile;
import com.aluminati.inventory.users.User;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import java.util.List;

public class GoogleSignIn extends Fragment implements View.OnClickListener {

    private static final String TAG = "GoogleSignIn";
    private static final String GoogleProviderId = "Google";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;
    private Button googleButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("617397433442-urgnv43j7ik4obmqvoemf1huiv0pcnl1.apps.googleusercontent.com").requestEmail().build();

        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(getActivity(), googleSignInOptions);

        callbackManager = CallbackManager.Factory.create();

        View view = inflater.inflate(R.layout.google_signin, container, false);

        googleButton = view.findViewById(R.id.google_signin_button);
        googleButton.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
            UserFetch.getUser(firebaseAuth.getCurrentUser().getEmail()).addOnCompleteListener(rusult -> {
                if (rusult.isSuccessful() && rusult.getResult() != null) {
                    Log.i(TAG, "User => " + firebaseAuth.getCurrentUser().getEmail());
                    User user = new User(rusult.getResult());
                    if (user.isGoogleLinked()) {
                        Log.i(TAG, "User => " + user.isGoogleLinked());
                        googleButton.setText(getResources().getString(R.string.unlink_google));
                    }
                }
            });
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

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(getActivity(), task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "SignInWithGoogle:failed", task.getException());
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    LoginManager.getInstance().logOut();
                    Log.w(TAG, "GoogleSignIn:failed because", task.getException());
                    LinkAccounts.linkAccountsInfo(getContext(), "Email is already registered, Login and link account or Recover Password");
                }
            } else {
                Log.d(TAG, "SignInWithGoogle:success");
                UserFetch.update(firebaseAuth.getCurrentUser().getEmail(), "is_google_linked", true);
                LinkAccounts.linkAccounts(credential,getActivity(), TAG);
                VerifyUser.checkUser(firebaseAuth.getCurrentUser(), getActivity(), VerificationStatus.GOOGLE);
            }
        });
    }

    private void unlinkGoogle() {
        firebaseAuth.getCurrentUser().unlink(GoogleProviderId).addOnCompleteListener(getActivity(), result -> {
            if (result.isSuccessful()) {
                googleButton.setText(getResources().getString(R.string.login_google));
                Utils.makeSnackBar("Unlinked Google", googleButton, getActivity());
                Log.d(TAG, "Unlinked Google");
            }else{
                Utils.makeSnackBar("Failed to Unlink Google", googleButton, getActivity());
                Log.d(TAG, "Failed to Unlink Facebook");
            }
        });
    }

    private void alertUnlinkDialog(){
        new AlertDialog.Builder(getContext())
                .setTitle("Unlink Google")
                .setMessage("Are you sure ?")
                .show();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.google_signin_button) {
            if (googleButton.getText().toString().equals(getResources().getString(R.string.login_google))) {
                signIn();
            } else if (googleButton.getText().toString().equals(getResources().getString(R.string.unlink_google))) {
                unlinkGoogle();
            }
        }
    }



}
