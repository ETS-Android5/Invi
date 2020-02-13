package com.aluminati.inventory.login.authentication.password;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.fragmentListeners.password.PassWordListenerSender;
import com.aluminati.inventory.register.RegisterActivity;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class Password extends Fragment implements View.OnClickListener{

    private static final String TAG = "Password";
    private PassWordListenerSender passWordListenerReciever;
    private EditText passWord;
    private EditText confrimPassWord;
    private Button showPassword;
    private Button confirmPasswordButton;
    private PassWordPopUp passWordPopUp;
    private ProgressBar passWordStrenght;
    private ProgressBar confirmPassWordStrength;
    private AtomicInteger strenght;
    private ArrayList<AtomicBoolean> locks;
    private boolean meetsRequirements = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.password_fragment, container, false);

        passWord = view.findViewById(R.id.password_view);
        confrimPassWord = view.findViewById(R.id.confirm_password_view);
        showPassword = view.findViewById(R.id.show_password);
        confirmPasswordButton = view.findViewById(R.id.confirm_password_show);

        showPassword.setOnClickListener(this);
        confirmPasswordButton.setOnClickListener(this);
        passWordStrenght = view.findViewById(R.id.password_strength_indicator);
        confirmPassWordStrength = view.findViewById(R.id.password_confirm_strength_indicator);

        view.findViewById(R.id.generate_password).setOnClickListener(this);
        view.findViewById(R.id.password_option).setOnClickListener(this);

        addTextWatcherToPassWord(passWord);

        strenght = new AtomicInteger(0);

        passWordPopUp = new PassWordPopUp(getActivity());

        initLocks();

        bindActivity((RegisterActivity)getContext());

        return view;
    }


    public void bindActivity(RegisterActivity registerActivity){
        registerActivity.setPassWordListenerReciever((code) -> onCodeReceived(code));
    }

    public void onCodeReceived(int code){
        switch (code){
            case 3001:{
                if (!passWord.getText().toString().isEmpty() && !confrimPassWord.getText().toString().isEmpty()){
                        passWordListenerReciever.onPassWordMatchSend(passWord.getText().toString(), confrimPassWord.getText().toString());
                }
                Log.i(TAG, "Code " + code);

            }
        }
    }



    private void showPassword(EditText editText, Button button){
        if(editText.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD){
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            button.setCompoundDrawables(getResources().getDrawable(R.drawable.hide_password, null), null, null, null);
            //button.setTooltipText(getResources().getString(R.string.hide_password)); Min SDK 26
        }
    }

    private void addTextWatcherToPassWord(EditText editText){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passWordValidator(charSequence.toString());
                if(charSequence.toString().isEmpty()){
                    clearLocks();
                    strenght.set(0);
                    passWordPopUp.clear();
                    passWordValidator(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void initLocks(){
        locks = new ArrayList<>(5);
        for(int i = 0; i < 5; i++){
            locks.add(new AtomicBoolean(true));
        }
    }

    private void clearLocks(){
        locks.forEach(x -> x.set(true));
    }

    private void passWordValidator(String passWord){
        if(passWord.length() >= 8){
            if(locks.get(0).get()) {
                passWordPopUp.setPassWordLength();
                strenght.getAndAdd(20);
                locks.get(0).set(false);
            }
        }

        if(Pattern.compile("[@#$%!]{2,}").matcher(passWord).find()){
            if(locks.get(1).get()) {
                passWordPopUp.setContainsSpecialChar();
                strenght.getAndAdd(20);
                locks.get(1).set(false);
            }
        }

        if(Pattern.compile("[a-z]{2,}").matcher(passWord).find()){
            if(locks.get(2).get()) {
                passWordPopUp.setContainsLowerCase();
                strenght.getAndAdd(20);
                locks.get(2).set(false);
            }
        }

        /*
        if(passWord.chars().forEach(Character::isUpperCase)){
            if(locks.get(3).get()) {
                passWordPopUp.setContainsUpperCase();
                strenght.getAndAdd(20);
                locks.get(3).set(false);
            }
        }
        */

        if(Pattern.compile("\\d+").matcher(passWord).find()){
            if(locks.get(4).get()) {
                passWordPopUp.setContainsDigit();
                strenght.getAndAdd(20);
                locks.get(4).set(false);
            }
        }
        Log.i(TAG, "Password strenght "  + strenght.get());
        confirmPassWordStrength.setProgress(strenght.get());
        passWordStrenght.setProgress(strenght.get());
        setProgressListener(passWordStrenght);
        setProgressListener(confirmPassWordStrength);
    }

    private void setProgressListener(ProgressBar progressBar) {
        int progress = progressBar.getProgress();
        if(progress > 25 && progress < 50){
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.week_password, null)));
        }else if(progress > 50 && progress < 75){
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.mid_strong_password, null)));
        }else{
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.password_verify, null)));
        }
    }

    private void generatePassword(){
        int leftLimit = 48,rightLimit = 122, targetStringLength = 15;
        strenght.set(0);

        String generatedString = new Random().ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        passWord.setText(generatedString);
        confrimPassWord.setText(generatedString);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.show_password:{
                showPassword(passWord, showPassword);
                break;
            }
            case R.id.confirm_password_show:{
                showPassword(confrimPassWord, confirmPasswordButton);
                break;
            }
            case R.id.generate_password:{
                clearLocks();
                generatePassword();
                break;
            }case R.id.password_option:{
                passWordPopUp.show();
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    public <T extends AppCompatActivity> void setPassWordListenerSender(PassWordListenerSender passWordListenerSender){
        this.passWordListenerReciever = passWordListenerSender;
    }

    class PassWordPopUp{

        private Dialog passWordDialog;
        private TextView passWordLength;
        private TextView containsDigit;
        private TextView containsUpperCase;
        private TextView containsLowerCase;
        private TextView containsSpecialChar;
        private Button okButton;
        private ColorStateList color;

        PassWordPopUp(Activity activity){
            passWordDialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar);
            passWordDialog.setContentView(R.layout.password_prompt);
            passWordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));
            passWordLength = passWordDialog.findViewById(R.id.password_length);
            containsDigit = passWordDialog.findViewById(R.id.password_digit);
            containsUpperCase = passWordDialog.findViewById(R.id.password_upper_case);
            containsLowerCase = passWordDialog.findViewById(R.id.password_lower_case);
            containsSpecialChar = passWordDialog.findViewById(R.id.password_special_char);
            color = containsSpecialChar.getTextColors();
            okButton = passWordDialog.findViewById(R.id.password_check_button);
            okButton.setOnClickListener(click -> {
                passWordDialog.cancel();
            });
        }


        void setContainsDigit(){
            containsDigit.setTextColor(getResources().getColor(R.color.password_verify, null));
        }

        void setPassWordLength(){
            passWordLength.setTextColor(getResources().getColor(R.color.password_verify, null));
        }

        void setContainsUpperCase(){
            containsUpperCase.setTextColor(getResources().getColor(R.color.password_verify, null));
        }

        void setContainsLowerCase(){
            containsLowerCase.setTextColor(getResources().getColor(R.color.password_verify, null));
        }

        void setContainsSpecialChar(){
            containsSpecialChar.setTextColor(getResources().getColor(R.color.password_verify, null));
        }

        void clear(){
            containsDigit.setTextColor(color);
            containsSpecialChar.setTextColor(color);
            containsLowerCase.setTextColor(color);
            containsSpecialChar.setTextColor(color);
            passWordLength.setTextColor(color);
        }

        void show(){
           passWordDialog.show();
        }

    }
}
