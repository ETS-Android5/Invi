package com.aluminati.inventory.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.aluminati.inventory.R;
import com.ebanx.swipebtn.SwipeButton;


public class DeleteUser extends DialogFragment {

    private static final String TAG = DeleteUser.class.getName();

    private RadioGroup deleteUserReasons;
    private SwipeButton deleteUser;
    private EditText otherReason;
    private String reason = "";

    public DeleteUser() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(getResources().getLayout(R.layout.delete_user), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deleteUserReasons = view.findViewById(R.id.delete_user_reasons);

        deleteUser = view.findViewById(R.id.delete_user);
        otherReason = view.findViewById(R.id.other_reason);

        deleteUser.setOnStateChangeListener(click -> {
            if(click){
                if(reason.isEmpty()){
                    otherReason.setText("Please give reason");
                    otherReason.setTextColor(getResources().getColor(R.color.google_red));
                    deleteUser.setActivated(false);
                }else{
                    deleteUser();
                }
            }
        });

        deleteUserReasons.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.radio_reason_5){
                otherReason.setEnabled(true);
            }else {
                reason = ((RadioButton)view.findViewById(checkedId)).getText().toString();
            }
        });
    }


    public static DeleteUser newInstance(String title) {
        DeleteUser deleteUser = new DeleteUser();
        Bundle args = new Bundle();
        args.putString("title", title);
        deleteUser.setArguments(args);
        return deleteUser;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void deleteUser(){
        if(otherReason.isEnabled()){
            reason = otherReason.getText().toString();
        }

        PassWordReEnter passWordReEnter = PassWordReEnter.newInstance(reason);
        passWordReEnter.show(getParentFragmentManager(),"password_renter");

    }




}
