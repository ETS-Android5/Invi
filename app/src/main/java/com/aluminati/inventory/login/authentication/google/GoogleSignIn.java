package com.aluminati.inventory.login.authentication.google;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.login.authentication.LinkAccounts;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
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

public class GoogleSignIn extends Fragment {

    private static final String TAG = "GoogleSignIn";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("617397433442-urgnv43j7ik4obmqvoemf1huiv0pcnl1.apps.googleusercontent.com").requestEmail().build();

        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(getActivity(), googleSignInOptions);

        callbackManager = CallbackManager.Factory.create();

        View view = inflater.inflate(R.layout.google_signin, container, false);

        view.findViewById(R.id.google_signin_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        return view;
    }



    private void signIn(){
        Intent googleSignInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(googleSignInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);
                if(googleSignInAccount != null) {
                    firebaseAuthWithGoogle(googleSignInAccount);
                }else {
                    Log.d(TAG, "GoogleSignInClient:returned => null");
                }
            }catch (ApiException e){
                Log.w(TAG, "Google Sign In Failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount googleSignInAccount){
        Log.d(TAG, "FireBaseAuthWithGoogle:" + googleSignInAccount.getId());
        final AuthCredential credential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(getActivity(), task -> {
            if(!task.isSuccessful()){
                Log.w(TAG, "SignInWithGoogle:failed", task.getException());
                if(task.getException() instanceof FirebaseAuthUserCollisionException){
                    LoginManager.getInstance().logOut();
                    Log.w(TAG, "GoogleSignIn:failed because", task.getException());
                    LinkAccounts.linkAccountsInfo(getContext(), "Email is already registered, Login and link account or Recover Password");

                }
            }else{
                Log.d(TAG, "SignInWithGoogle:success");
                VerifyUser.checkUser(firebaseAuth.getCurrentUser(), getActivity(), VerificationStatus.GOOGLE);
            }
        });
    }

}
