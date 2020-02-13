package com.aluminati.inventory.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aluminati.inventory.R;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.AuthenticationActivity;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.users.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

public class EmailAuthenticationFragment extends BaseFragment {

    private static final String TAG = "EmailAuthFrag";

    private EditText emailTextField;
    private Button emailVerifyButton;
    private TextView emailVerLabel;
    private TextView emailVerifyLabelView;
    private FirebaseAuth firebaseAuth;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.email_verification, container, false);

        emailTextField = view.findViewById(R.id.email_verify_text_field);
        emailVerifyButton = view.findViewById(R.id.email_verify_button);
        emailVerLabel = view.findViewById(R.id.email_verify_label);
        emailVerifyLabelView = view.findViewById(R.id.email_verify_text_view);





        completeSetUp.onComplete(3001);



        firebaseAuth = FirebaseAuth.getInstance();

        return view;
    }


}
