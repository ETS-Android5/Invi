package com.aluminati.inventory.offline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConnectivityCheck extends BroadcastReceiver {


        private Snackbar snackbar;

        public ConnectivityCheck(View view){
            this.snackbar = Snackbar.make(view, "", BaseTransientBottomBar.LENGTH_INDEFINITE);
        }


        @Override
        public void onReceive(Context context, Intent intent) {
            if(isConnected(context)){
                new InternetCheck(result -> {
                    if(result){
                        snackbar.setText("Connected");
                        snackbar.dismiss();
                        Log.i("ConnectionActivity", "Connected");

                    }else{
                        snackbar.setText("Online : No Internet Access");
                        snackbar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
                        snackbar.show();
                        Log.i("ConnectionActivity", "Disconnected");
                    }
                });
            }else {
                snackbar.setText("Offline");
                snackbar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
                snackbar.show();
                Log.i("ConnectionActivity", "Network disconnected");
            }

        }

        private boolean isConnected(Context context) {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            return activeNetwork != null && activeNetwork.isConnected();
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

