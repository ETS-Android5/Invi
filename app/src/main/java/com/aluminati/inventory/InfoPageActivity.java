package com.aluminati.inventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.users.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import java.io.InputStream;

public class InfoPageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private static final String TAG = "InfoPageActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_page);


        NavigationView navigationView = findViewById(R.id.nav_view);
                       navigationView.setNavigationItemSelectedListener(this);


        try{

            UserFetch.getUser(FirebaseAuth.getInstance().getCurrentUser().getEmail()).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    User user = new User(task.getResult());
                    Log.i(TAG, user.getEmail() + " " + user.getDisplayName());
//                    new RemoteImage(navigationView.getHeaderView(0).findViewById(R.id.user_profile_imageview)).execute(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
                    ((TextView) navigationView.getHeaderView(0).findViewById(R.id.email_verified)).setText(user.isEmailVerified() ? "Verified" : "Not Verified");
                    ((TextView) navigationView.getHeaderView(0).findViewById(R.id.user_profile_email)).setText(user.getEmail());
                    ((TextView) navigationView.getHeaderView(0).findViewById(R.id.user_profile_name)).setText(user.getDisplayName());
                }
            });

        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.log_out: {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(InfoPageActivity.this, MainActivity.class));
                finish();
                break;
            }

        }
        return true;
    }







}
