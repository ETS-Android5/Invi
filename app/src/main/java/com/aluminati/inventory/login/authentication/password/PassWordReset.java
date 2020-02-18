package com.aluminati.inventory.login.authentication.password;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.fragments.fragmentListeners.password.PassWordListenerReciever;
import com.aluminati.inventory.fragments.fragmentListeners.password.PassWordListenerSender;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.users.User;
import com.google.firebase.auth.FirebaseAuth;

public class PassWordReset extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = PassWordReset.class.getName();
    private TextView passWordResetMessage;
    private PassWordListenerReciever passWordListenerReciever;
    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.reset_password);
        passWordResetMessage = findViewById(R.id.password_reset_error);
        findViewById(R.id.password_reset_cancel).setOnClickListener(this);
        findViewById(R.id.password_reset_button).setOnClickListener(this);


        Password password = (Password) getSupportFragmentManager().findFragmentById(R.id.password_reset_frag);
        bindPassWordFragment(password);

        user = new User(FirebaseAuth.getInstance().getCurrentUser());

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void bindPassWordFragment(Password password){
        password.setPassWordListenerSender(this::onPassWordMatchSend);
    }

    private void onPassWordMatchSend(String password, String confrimPassWord, boolean meetsReq){
        if(password.isEmpty()){
           // passWordResetMessage.setTextColor(getResources().getString(R.string.pass));
        }else if(confrimPassWord.isEmpty()){

        }else if(!meetsReq){
            passWordResetMessage.setText(getResources().getString(R.string.password_dont_meet_requirements));
        }else if(!password.equals(confrimPassWord)){
            passWordResetMessage.setText(getResources().getString(R.string.password_dont_match));
        }else if(password.contains(user.getDisplayName().split(" ")[0]) || password.contains(user.getDisplayName().split("")[1])){
            passWordResetMessage.setText(getResources().getString(R.string.password_conatins_name));
        }else{
            resetPassWord(password);
        }
    }

    private void resetPassWord(String password){
        FirebaseAuth.getInstance().getCurrentUser().updatePassword(password).addOnCompleteListener(update -> {
            if(update.isSuccessful()){
                Log.i(TAG, "Password updated successfully");
                setResult(Activity.RESULT_OK);
                finish();
            }else{
                Log.w(TAG, "Password Update Failed", update.getException());
            }
        });
    }

    public <T extends Fragment> void setPassWordListenerReciever(PassWordListenerReciever passWordListenerReciever){
        this.passWordListenerReciever = passWordListenerReciever;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.password_reset_cancel:{
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
            }
            case R.id.password_reset_button:{
                passWordListenerReciever.askForPassWord(3001);
            }
        }
    }
}
