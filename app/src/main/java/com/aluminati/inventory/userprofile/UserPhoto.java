package com.aluminati.inventory.userprofile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.fragments.fragmentListeners.socialAccounts.ReloadImage;
import com.aluminati.inventory.fragments.fragmentListeners.socialAccounts.ReloadImageResponse;
import com.aluminati.inventory.users.User;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class UserPhoto extends Fragment implements View.OnClickListener {


    private static final String TAG = UserPhoto.class.getName();
    private static final int GET_PHOTO = 1;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private TextView userImageChange;
    private ImageView userPhoto;
    private FirebaseAuth firebaseAuth;
    private ReloadImage reloadImageResponse;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.user_photo, container, false);

        userPhoto = view.findViewById(R.id.userImage);
        userImageChange = view.findViewById(R.id.user_image_change);
        userImageChange.setOnClickListener(this);
        userPhoto.setOnClickListener(this);
        userPhoto.setDrawingCacheEnabled(true);
        progressBar = view.findViewById(R.id.photo_progress_loader);


        userPhoto.addOnLayoutChangeListener((view1, i, i1, i2, i3, i4, i5, i6, i7) -> {
            if(hasImage((ImageView)view1)){
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        bindActivity(getActivity());
        firebaseAuth = FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Log.i(TAG, "Picture not found");
                return;
            }
            try {
                Log.i(TAG, "Picture found");
                progressBar.setVisibility(View.VISIBLE);
                InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData());
                if(inputStream != null){
                    Log.i(TAG, "Stream not null" + inputStream.toString());
                    Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(inputStream));
                    if(hasImage(userPhoto)) {
                        new FireBaseStorage(firebaseAuth.getCurrentUser()).uploadPhot(data.getData(),getContext(), bitmap);
                        Snackbar.make(userImageChange, getResources().getString(R.string.photo_changed), BaseTransientBottomBar.LENGTH_LONG);
                    }
                }else Log.i(TAG, "Stream  null");
            }catch (FileNotFoundException e){
                Log.w(TAG, "File not found", e);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(firebaseAuth.getCurrentUser() != null){
            userPhoto.setImageURI(firebaseAuth.getCurrentUser().getPhotoUrl());
        }

    }

    private boolean hasImage(@NonNull ImageView view) {

        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
        }

        return hasImage;

    }

    private void countDown() {
        new CountDownTimer(5 * 1000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GET_PHOTO);
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
        } else {
            Snackbar.make(userImageChange, " Permission already granted", BaseTransientBottomBar.LENGTH_LONG).show();
            pickImage();
        }
    }


    public String encodeTobase64(Bitmap image)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);

        Log.e("LOOK", imageEncoded);
        return imageEncoded;
    }

    public Bitmap decodeBase64(String input)
    {
        try{
            byte [] encodeByte = Base64.decode(input,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(userImageChange, "Storage Permission Granted", BaseTransientBottomBar.LENGTH_LONG).show();
                pickImage();
            } else {
                Snackbar.make(userImageChange, "Storage Permission Denied", BaseTransientBottomBar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.userImage: {
                userImageChange.setVisibility(View.VISIBLE);
                countDown();
                break;
            }
            case R.id.user_image_change:{
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                break;
            }
        }
    }

    private static class RemoteImage extends AsyncTask<String, Void, Bitmap> {

        @SuppressLint("StaticFieldLeak")
        ImageView profileImageViwe;

        RemoteImage(ImageView profileImageViwe){
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

    private void setOnResponse(int code){
        if(code == 3001){
            if(firebaseAuth.getCurrentUser().getPhotoUrl() != null) {
                new RemoteImage(userPhoto).execute(firebaseAuth.getCurrentUser().getPhotoUrl().toString());
                reloadImageResponse.reloaded(true);
            }
        }
    }

    private void bindActivity(Activity appCompatActivity){
        if(getActivity() instanceof UserProfile){
            ((UserProfile)appCompatActivity).setReloadImageResponse(this::setOnResponse);
        }
    }

    public <T extends AppCompatActivity> void setReloadImage(ReloadImage reloadImage){
        this.reloadImageResponse = reloadImage;
    }

    class FireBaseStorage{

        private final String USER_PHOTOS = "user_photos";
        private final String TAG = FireBaseStorage.class.getName();
        private FirebaseStorage firebaseStorage;
        private StorageReference storageReference;
        private FirebaseUser firebaseUser;


        public FireBaseStorage(FirebaseUser firebaseUser){
            this.firebaseStorage = FirebaseStorage.getInstance();
            this.storageReference = firebaseStorage.getReference();
            this.firebaseUser = firebaseUser;
        }

        private void uploadPhot(Uri file, Context context, Bitmap bitmap) {

            Cursor returnCursor =
                    context.getContentResolver().query(file, null, null, null, null);

            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

            returnCursor.moveToFirst();

            StorageReference userPhotoRef = storageReference
                    .child(USER_PHOTOS.concat("/" + returnCursor.getString(nameIndex)));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = userPhotoRef.putBytes(data);

            uploadTask.addOnFailureListener(exception -> {
                Log.w(TAG, "Failed to upload to Firebasestorgae successfully", exception);
            }).addOnSuccessListener(taskSnapshot -> {
                Log.i(TAG, "File uploaded to Firebasestorgae successfully " + new File(file.toString()).getName());
            });


            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return userPhotoRef.getDownloadUrl();
            }).addOnSuccessListener(result -> {
                Log.i(TAG, "Url of file successfully got");
                firebaseUser.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(result).build())
                        .addOnSuccessListener(updatePhoto -> {
                            Log.i(TAG, "Updated photo successfully");
                            new RemoteImage(userPhoto).execute(result.toString());
                            progressBar.setVisibility(View.INVISIBLE);
                        })
                        .addOnFailureListener(updatePhoto -> Log.w(TAG, "Failed to update user photo", updatePhoto));
                UserFetch.update(firebaseUser.getEmail(), "user_image", result.toString());
            }).addOnFailureListener(result -> {
                Log.w(TAG, "Failed to get url of file", result);
            });


        }
    }
}
