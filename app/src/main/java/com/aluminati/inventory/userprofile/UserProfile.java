package com.aluminati.inventory.userprofile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aluminati.inventory.R;
import com.aluminati.inventory.firestore.UserFetch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.InputStream;

public class UserProfile extends AppCompatActivity implements View.OnClickListener{

    private EditText userName;
    private EditText userEmail;
    private EditText phoneNumber;
    private ImageView userPhoto;
    private TextView displayNameChange;
    private TextView emailChange;
    private TextView phoneChange;
    private TextView nameField;
    private TextView surNameField;
    private FirebaseUser firebaseUser;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        displayNameChange = findViewById(R.id.display_name_change);
        emailChange = findViewById(R.id.phone_number_change);
        userEmail = findViewById(R.id.email_field);
        userName = findViewById(R.id.display_name_field);
        phoneNumber = findViewById(R.id.phone_number_field);
        userPhoto = findViewById(R.id.userImage);
        surNameField = findViewById(R.id.surname_field);
        nameField = findViewById(R.id.name_field);


        displayNameChange.setOnClickListener(this);
        emailChange.setOnClickListener(this);
        //phoneChange.setOnClickListener(this);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void changeView(EditText editText, TextView textView){
        if(editText.isEnabled()){
            editText.setEnabled(false);
            textView.setText(getResources().getString(R.string.change));
        }else{
            editText.setEnabled(true);
            textView.setText(getResources().getString(R.string.save));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.display_name_change:{

                break;
            }
            case R.id.phone_number_change:{

                break;
            }
            case R.id.email_field:{

                break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseUser != null){
            String name[] = firebaseUser.getDisplayName().split(" ");

            nameField.setText(name[0]);
            surNameField.setText(name[1]);
            userName.setText(firebaseUser.getDisplayName());
            userEmail.setText(firebaseUser.getEmail());
            phoneNumber.setText(firebaseUser.getPhoneNumber());
            new RemoteImage(userPhoto).execute(firebaseUser.getPhotoUrl().toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onStateNotSaved() {
        super.onStateNotSaved();
    }

    private class RemoteImage extends AsyncTask<String, Void, Bitmap> {

        ImageView profileImageViwe;

        public RemoteImage(ImageView profileImageViwe){
            this.profileImageViwe = profileImageViwe;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap userIcon = null;
            try{
                InputStream inputStream = new java.net.URL(urlDisplay).openStream();
                userIcon = BitmapFactory.decodeStream(inputStream);
            }catch (Exception e){
                Log.w("Error Converting", e);
            }
            return userIcon;
        }

        protected void onPostExecute(Bitmap result){
            profileImageViwe.setImageBitmap(result);
        }


    }
}
