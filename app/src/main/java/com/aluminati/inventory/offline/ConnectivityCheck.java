package com.aluminati.inventory.offline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.aluminati.inventory.login.authentication.LogInActivity;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConnectivityCheck extends BroadcastReceiver {

        private static final String TAG = ConnectivityCheck.class.getName();
        public static Snackbar snackbar;
        private View view;



        public ConnectivityCheck(View view){
            this.view = view;

            createSnackBar();
        }

        private Snackbar createSnackBar(){
            snackbar = Snackbar.make(view, "", BaseTransientBottomBar.LENGTH_INDEFINITE).setBehavior(new NoSwipeBehavior());


            Button button = new Button(snackbar.getContext());
                   button.setText("Info");
                   button.setId(View.generateViewId());
                   button.setBackgroundColor(Color.TRANSPARENT);
                   button.setOnClickListener(result -> LogInActivity.alertDialog.show());
                   button.setTextColor(Color.YELLOW);



                         ConstraintLayout constraintLayout = new ConstraintLayout(snackbar.getContext());
                                          constraintLayout.addView(button);
                                          constraintLayout.setId(View.generateViewId());

            ConstraintSet constraintSet = new ConstraintSet();
                          constraintSet.clone(constraintLayout);
                          constraintSet.connect(button.getId(),ConstraintSet.RIGHT,constraintLayout.getId(),ConstraintSet.RIGHT,0);
                          constraintSet.connect(button.getId(),ConstraintSet.TOP,constraintLayout.getId(),ConstraintSet.TOP,0);
                          constraintSet.connect(button.getId(), ConstraintSet.LEFT,constraintLayout.getId(), ConstraintSet.LEFT, 0);
                          constraintSet.connect(button.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(), ConstraintSet.BOTTOM, 0);

                          constraintSet.setHorizontalBias(button.getId(), 1);
                          constraintSet.applyTo(constraintLayout);

            Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
            snackbarLayout.addView(constraintLayout);
            return snackbar;
        }




        @Override
        public void onReceive(Context context, Intent intent) {
            if(snackbar == null){
                createSnackBar();
            }
            if(isConnected(context)){
                new InternetCheck(result -> {
                    if(result) {
                        snackbar.setText("Connected");
                        snackbar.dismiss();
                        Log.i("ConnectionActivity", "Connected");
                    }else{
                        snackbar.setText("Online : No Internet Access");
                        snackbar.show();
                        Log.i("ConnectionActivity", "Disconnected");
                    }
                });
            }else {
                snackbar.setText("Offline");
                snackbar.show();
                Log.i("ConnectionActivity", "Network disconnected");
            }

        }

        private boolean isConnected(Context context) {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            return activeNetwork != null && activeNetwork.isConnected();
        }


        public static boolean isSnacBarVisible(){
            return snackbar.isShown();
        }



    class NoSwipeBehavior extends BaseTransientBottomBar.Behavior {
        @Override
        public boolean canSwipeDismissView(View child) {
            return false;
        }
    }

    private static class InternetCheck extends AsyncTask<Void,Void,Boolean> {

        private Consumer mConsumer;

        public interface Consumer {
            void accept(Boolean internet);
        }

        InternetCheck(Consumer consumer) {
            mConsumer = consumer;
            execute();
        }


        @Override protected Boolean doInBackground(Void... voids) { try {
            Socket sock = new Socket();
            sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);
            sock.close();
            return true;
        } catch (IOException e) { return false; } }

        @Override protected void onPostExecute(Boolean internet) { mConsumer.accept(internet); }
    }




}

