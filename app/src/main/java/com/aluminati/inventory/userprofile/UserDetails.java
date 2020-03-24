package com.aluminati.inventory.userprofile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.utils.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.phoneauthentication.PhoneAuthentication;
import com.aluminati.inventory.login.authentication.verification.VerificationStatus;
import com.aluminati.inventory.login.authentication.verification.VerifyUser;
import com.aluminati.inventory.users.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.concurrent.Callable;

public class UserDetails extends Fragment implements View.OnClickListener {

    private static final String TAG = UserDetails.class.getName();
    private EditText userName;
    private EditText userEmail;
    private EditText phoneNumber;
    private TextView displayNameChange;
    private TextView emailChange;
    private TextView phoneChange;
    private TextView phoneVerified;
    private TextView emailVerified;
    private FirebaseUser firebaseUser;
    private String tmp;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(getResources().getLayout(R.layout.user_details), container, false);

        displayNameChange = view.findViewById(R.id.display_name_change);
        emailChange = view.findViewById(R.id.change_email);
        userEmail = view.findViewById(R.id.email_field);
        userName = view.findViewById(R.id.display_name_field);
        phoneNumber = view.findViewById(R.id.phone_number_field);
        phoneChange = view.findViewById(R.id.phone_number_change);
        phoneVerified = view.findViewById(R.id.phone_number_verified);
        emailVerified = view.findViewById(R.id.email_verified);

        displayNameChange.setOnClickListener(this);
        emailChange.setOnClickListener(this);
        phoneChange.setOnClickListener(this);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        return view;
    }



    private void emailCheck() {
        new AlertDialog.Builder(getContext())
                .setTitle("Change Email")
                .setMessage("Unlink Facebook And Google To Change Email")
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> dialogInterface.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void changeAttribute(EditText editText, TextView textView, Callable<Void> change) {
        if (textView.getText().equals(getResources().getString(R.string.change))) {
            if (editText.getId() == R.id.email_field) {
                UserFetch.getUser(firebaseUser.getEmail()).addOnCompleteListener(result -> {
                    if (result.isSuccessful() && result.getResult() != null) {
                        User user = new User(result.getResult());
                        if (user.isGoogleLinked() || user.isFacebookLinked()) {
                            emailCheck();
                        } else {
                            tmp = editText.getText().toString();
                            editText.setEnabled(true);
                            textView.setText(getResources().getString(R.string.save));
                        }
                    }
                });
            } else {
                tmp = editText.getText().toString();
                editText.setEnabled(true);
                textView.setText(getResources().getString(R.string.save));
            }
        } else if (textView.getText().equals(getResources().getString(R.string.save))) {
            editText.setEnabled(false);
            textView.setText(getResources().getString(R.string.change));
            try {
                change.call();
            } catch (Exception e) {
                Log.w(TAG, "Exception changing attributes => ", e);
            }
        }
    }

    private void setVerificationLabels() {

        UserFetch.getUser(firebaseUser.getEmail()).addOnSuccessListener(result -> {
            if (result.exists()) {
                User user = new User(result);
                if (user.isEmailVerified()) {
                    emailVerified.setText(getResources().getString(R.string.veirified));
                    emailVerified.setTextColor(getResources().getColor(R.color.password_verify));
                }

                if (user.isPhoneVerified()) {
                    phoneVerified.setText(getResources().getString(R.string.veirified));
                    phoneVerified.setTextColor(getResources().getColor(R.color.password_verify));
                }
            }
        }).addOnFailureListener(result -> Log.w(TAG, "Failed to fetch user", result));

    }

    private void reloadUser() {
        firebaseUser.reload().addOnSuccessListener(result -> {
            Log.i(TAG, "User reloaded");
            userName.setText(firebaseUser.getDisplayName());
            userEmail.setText(firebaseUser.getEmail());
            phoneNumber.setText(firebaseUser.getPhoneNumber());
            setVerificationLabels();
        }).addOnFailureListener(result -> {
            Log.w(TAG, "Failed to reload user", result);
            Utils.makeSnackBarWithButtons("Failed to reload user", displayNameChange, getActivity());
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VerificationStatus.PHONE_VERIFICATION) {
            if (resultCode == VerificationStatus.SUCCESSFULL_UPDATE) {
                Utils.makeSnackBarWithButtons("Updated Phone Number", userName, getActivity());
            } else if (resultCode == VerificationStatus.FAILED_UPDATE) {
                Utils.makeSnackBarWithButtons("Failed to Update Phone Number", userName, getActivity());
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        reloadUser();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.display_name_change: {
                changeAttribute(userName, displayNameChange, () -> {
                    if (!userName.getText().toString().isEmpty() && !tmp.equals(userName.getText().toString())) {
                        firebaseUser
                                .updateProfile(new UserProfileChangeRequest.Builder()
                                        .setDisplayName(userName.getText().toString()).build())
                                .addOnCompleteListener(result -> {
                                    if (result.isSuccessful()) {
                                        Utils.makeSnackBarWithButtons("Name Updated", displayNameChange, getActivity());
                                        reloadUser();
                                    } else {
                                        Utils.makeSnackBarWithButtons("Failed to Update Name", displayNameChange, getActivity());
                                    }
                                });
                    }
                    return null;
                });
                break;
            }
            case R.id.phone_number_change: {
                startActivityForResult(new Intent(getContext(), PhoneAuthentication.class), VerificationStatus.PHONE_VERIFICATION);
                break;
            }
            case R.id.change_email: {
                changeAttribute(userEmail, emailChange, () -> {
                    if (!userEmail.getText().toString().isEmpty() && !tmp.equals(userEmail.getText().toString())) {
                        firebaseUser.updateEmail(emailChange.toString()).addOnCompleteListener(result -> {
                            if (result.isSuccessful()) {
                                Utils.makeSnackBarWithButtons("Email Updated", displayNameChange, getActivity());
                                reloadUser();
                                VerifyUser.verifyEmail().addOnCompleteListener(verificationSent -> {
                                    if (verificationSent.isSuccessful()) {
                                        Utils.makeSnackBarWithButtons("Verification Email Sent", userEmail, getActivity());
                                        UserFetch.update(firebaseUser.getEmail(), "is_email_verified", false);
                                    } else {
                                        Utils.makeSnackBarWithButtons("Failed to Send Verification", userEmail, getActivity());
                                    }
                                });
                            } else {
                                Utils.makeSnackBarWithButtons("Failed to Update Emailed", displayNameChange, getActivity());
                            }
                        });
                    }
                    return null;
                });
                break;
            }
        }
    }
}
