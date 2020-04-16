package com.aluminati.inventory.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.utils.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.LogInActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

public class PassWordReEnter extends DialogFragment{

        private final String TAG = PassWordReEnter.class.getName();
        private EditText passWord;
        private String reason = "";
        private FirebaseUser firebaseUser;


        private PassWordReEnter(String reason) {
            this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            this.reason = reason;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            return inflater.inflate(getResources().getLayout(R.layout.password_enter), container, true);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            passWord = view.findViewById(R.id.password_enter);
            view.findViewById(R.id.delete_profile).setOnClickListener(click -> deleteUser());
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
        }

        public static PassWordReEnter newInstance(String reason) {
            return new PassWordReEnter(reason);
        }

        private void deleteUser(){

            if(!reason.isEmpty()){
                if(!passWord.getText().toString().isEmpty()){
                    relogin()
                            .addOnSuccessListener(success -> {
                                Log.i(TAG, "Re-logged in");
                                FirebaseAuth.getInstance().getCurrentUser().delete().addOnSuccessListener(result -> {
                                    Log.i(TAG, "User deleted");
                                    Utils.makeSnackBarWithButtons("User deleted", passWord, getActivity());
                                    UserFetch.addReason(firebaseUser.getEmail(), reason);
                                    UserFetch.deleteUser(firebaseUser.getEmail());

                                    Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_LONG).show();

                                    getActivity().startActivity(new Intent(getActivity(), LogInActivity.class));
                                    getActivity().finish();
                                }).addOnFailureListener(failure -> {
                                    Utils.makeSnackBar("Failed to delte account", getView(), getActivity());
                                    Log.w(TAG, "Failed to delete account", failure);
                                });
                            })
                            .addOnFailureListener(failure -> {
                                if(failure instanceof FirebaseAuthInvalidCredentialsException){
                                    Utils.makeSnackBar("Incorrect Password", getView(), getActivity());
                                }
                                Log.w(TAG, "Failed to re-loggin", failure);
                            });
                }else{
                    passWord.setHint(getResources().getString(R.string.reg_hint_password));
                }
            }else{
                    relogin()
                    .addOnSuccessListener(success -> {
                        Log.i(TAG, "Re logged in successfully");
                    })
                    .addOnFailureListener(failure -> {
                        Log.w(TAG, "Failed to re-log in", failure);
                        if(failure instanceof FirebaseAuthInvalidCredentialsException){
                            Utils.makeSnackBar("Incorrect Password", getView(), getActivity());
                        }
                    });
            }
        }

        private Task<AuthResult> relogin(){
                    return FirebaseAuth.getInstance().signInWithEmailAndPassword(firebaseUser.getEmail(), passWord.getText().toString());
        }



}
