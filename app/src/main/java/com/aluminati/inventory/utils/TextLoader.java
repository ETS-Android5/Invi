package com.aluminati.inventory.utils;

import android.graphics.Color;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;

public class TextLoader {


    private long startTime, currentTime, finishedTime = 0L;
    private int endTime = 0;
    private AtomicBoolean atomicBoolean;
    private Handler handler;
    private Runnable runnable;

    public TextLoader() {
        this.atomicBoolean = new AtomicBoolean(true);
        this.handler = new Handler();
    }

    public void setForeground(TextView textView, String string){



        textView.setText(string);
        startTime = System.currentTimeMillis();
        currentTime = startTime;



        runnable = new Runnable() {
            @Override
            public void run() {

                currentTime = System.currentTimeMillis();
                finishedTime = currentTime - startTime;

                if(atomicBoolean.get()){
                    endTime = (int) ((finishedTime / 250));// divide this by
                    if(endTime == textView.getText().toString().length()){
                        startTime = System.currentTimeMillis();
                        atomicBoolean.set(false);
                    }

                    if(endTime <= textView.getText().length()) {
                        Spannable spannableString = new SpannableString(textView.getText());
                        spannableString.setSpan(new ForegroundColorSpan(Color.GRAY), 0, endTime, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textView.setText(spannableString);
                        handler.postDelayed(this, 250);
                    }

                }else{
                    endTime = (int) ((finishedTime / 250));// divide this by
                    if(endTime == textView.getText().toString().length()){
                        startTime = System.currentTimeMillis();
                        atomicBoolean.set(true);
                    }

                    if(endTime <= textView.getText().length()) {
                        Spannable spannableString = new SpannableString(textView.getText());
                        spannableString.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, endTime, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textView.setText(spannableString);
                        handler.postDelayed(this, 250);
                    }

                }


            }
        };

        handler.postDelayed(runnable, 1000);
    }

    public void stopLoader(){
        handler.removeCallbacks(runnable);
    }
}
