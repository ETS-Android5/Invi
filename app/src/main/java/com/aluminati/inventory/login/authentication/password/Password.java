package com.aluminati.inventory.login.authentication.password;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class Password extends Fragment implements View.OnClickListener{

    private static final String TAG = Password.class.getName();
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

        if(getContext() instanceof PassWordReset){
            bindActivity((PassWordReset)getContext());
        }else if(getContext() instanceof RegisterActivity){
            bindActivity((RegisterActivity)getContext());
        }




        return view;
    }


    private void bindActivity(AppCompatActivity appCompatActivity){
        if(appCompatActivity instanceof PassWordReset){
            ((PassWordReset)appCompatActivity).setPassWordListenerReciever(this::onCodeReceived);
        }else if(appCompatActivity instanceof RegisterActivity){
            ((RegisterActivity)appCompatActivity).setPassWordListenerReciever(this::onCodeReceived);
        }
    }

    private void onCodeReceived(int code){
        if (code == 3001) {
            passWordListenerReciever.onPassWordMatchSend(passWord.getText().toString(), confrimPassWord.getText().toString(), meetsRequirments());
            Log.i(TAG, "Code " + code);
        }
    }



    private void showPassword(EditText editText, Button button){
        if((editText.getInputType()-1) == InputType.TYPE_TEXT_VARIATION_PASSWORD ){
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            button.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.hide_password,null), null, null ,null);
            ///// From SDK 26 buttonPositive.setTooltipText(getResources().getString(R.string.hide_password));
        }else if((editText.getInputType()-1) == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD){
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            button.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.show_password,null), null, null ,null);
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

        for(AtomicBoolean atomicBoolean : locks){
            atomicBoolean.set(true);
        }
        /// From SDK 24 locks.forEach(x -> x.set(true));
    }

    private boolean meetsRequirments(){
        /// Min SDK 24 return locks.stream().filter(x -> !x.get()).count() == locks.size();
        int count = 0;
        for(AtomicBoolean atomicBoolean : locks){
            if(!atomicBoolean.get()){
                count++;
            }
        }
        return count == locks.size();
    }

    private void passWordValidator(String passWord){
        if(passWord.length() >= 8){
            if(locks.get(0).get()) {
                passWordPopUp.setPassWordLength();
                strenght.getAndAdd(20);
                locks.get(0).set(false);
            }
        }else{
            if(!locks.get(0).get()){
                strenght.getAndSet(strenght.get() - 20);
            }
        }


        patternMatcher(passWord, locks.get(1), "[@#$%!<>`']{1,}", () -> {
                passWordPopUp.setContainsSpecialChar();
                return null;
        });

        patternMatcher(passWord, locks.get(2), "[a-z]{1,}", () -> {
                passWordPopUp.setContainsLowerCase();
                return null;
        });

        patternMatcher(passWord, locks.get(3), "[A-Z]{1,}", () -> {
                passWordPopUp.setContainsUpperCase();
                return null;
        });
        patternMatcher(passWord, locks.get(4), "\\d+", () -> {
                passWordPopUp.setContainsDigit();
                return null;
        });

        Log.i(TAG, "Password strenght "  + strenght.get());
        confirmPassWordStrength.setProgress(strenght.get());
        passWordStrenght.setProgress(strenght.get());
        setProgressListener(passWordStrenght);
        setProgressListener(confirmPassWordStrength);
    }

    private void patternMatcher(String password, AtomicBoolean atomicBoolean, String regex, Callable<Void> regexVer){
        if(Pattern.compile(regex).matcher(password).find()){
            if(atomicBoolean.get()){
                try {
                    regexVer.call();
                    strenght.getAndAdd(20);
                    atomicBoolean.set(false);
                }catch (Exception e){
                    Log.w(TAG, "Error calling function", e);
                }
            }
        }else{
            if(!atomicBoolean.get()){
                strenght.getAndSet(strenght.get() - 20);
                atomicBoolean.set(true);
            }
        }
    }

    private void setProgressListener(ProgressBar progressBar) {
        int progress = progressBar.getProgress();
        if(progress > 25 && progress < 50){
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.week_password)));
        }else if(progress > 50 && progress < 75){
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.mid_strong_password)));
        }else{
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.password_verify)));
        }
    }

    private void generatePassword(){
        int leftLimit = '#',rightLimit = 'z', targetStringLength = 30;
        strenght.set(0);

        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();

        int passWordLength = (random.nextInt(targetStringLength))+10;

        for(int i = 0; i < passWordLength; i++ ){
            int res = ThreadLocalRandom.current().nextInt(leftLimit, rightLimit);
            stringBuilder.append((char)res);
        }

        /*

            ############# Min SDK = 24 ################

            String generatedString = new Random().ints(leftLimit, rightLimit + 1)
                    .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();

         */

        String password = new String(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        passWord.setText(password);
        confrimPassWord.setText(password);
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
                passWordPopUp.clear();
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

    public void setPassWordListenerSender(PassWordListenerSender passWordListenerSender){
        this.passWordListenerReciever = passWordListenerSender;
    }

    class PassWordPopUp{

        private Dialog passWordDialog;
        private TextView passWordLength;
        private TextView containsDigit;
        private TextView containsUpperCase;
        private TextView containsLowerCase;
        private TextView containsSpecialChar;
        private ColorStateList color;

        PassWordPopUp(Activity activity){
            passWordDialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar);
            passWordDialog.setContentView(R.layout.password_prompt);
            passWordLength = passWordDialog.findViewById(R.id.password_length);
            containsDigit = passWordDialog.findViewById(R.id.password_digit);
            containsUpperCase = passWordDialog.findViewById(R.id.password_upper_case);
            containsLowerCase = passWordDialog.findViewById(R.id.password_lower_case);
            containsSpecialChar = passWordDialog.findViewById(R.id.password_special_char);
            color = containsSpecialChar.getTextColors();
            passWordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            passWordDialog.findViewById(R.id.password_check_button).setOnClickListener(click -> {
                passWordDialog.cancel();
            });
        }


        void setContainsDigit(){
            containsDigit.setTextColor(getResources().getColor(R.color.password_verify));
        }

        void setPassWordLength(){
            passWordLength.setTextColor(getResources().getColor(R.color.password_verify));
        }

        void setContainsUpperCase(){
            containsUpperCase.setTextColor(getResources().getColor(R.color.password_verify));
        }

        void setContainsLowerCase(){
            containsLowerCase.setTextColor(getResources().getColor(R.color.password_verify));
        }

        void setContainsSpecialChar(){
            containsSpecialChar.setTextColor(getResources().getColor(R.color.password_verify));
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
