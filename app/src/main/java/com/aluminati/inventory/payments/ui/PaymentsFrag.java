package com.aluminati.inventory.payments.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCase;
import androidx.camera.core.impl.ImageCaptureConfig;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.aluminati.inventory.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.document.FirebaseVisionCloudDocumentRecognizerOptions;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static android.content.Context.CAMERA_SERVICE;
import static androidx.camera.core.ImageCapture.*;

public class PaymentsFrag extends Fragment implements View.OnClickListener {

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private View imgCapture;
    private PreviewView textureView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageAnalysis imageAnalysis;
    private ImageCapture imageCapture;
    private ImageButton picImage;
    private final int STORAGE_PERMISSION_CODE = 2001;
    static final int REQUEST_IMAGE_CAPTURE = 1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(getResources().getLayout(R.layout.payments), container, false);

        //cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        picImage = view.findViewById(R.id.imgCapture);
        picImage.setOnClickListener(this);
        //imageCapture();

       // textureView = view.findViewById(R.id.preview_view);


        return view;
    }

    private void imageAnalysis(){
        imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(
                Runnable::run,
                image -> {

                });

    }

    private void imageCapture(){
        imageCapture =
                new Builder()
                        .setTargetRotation(getActivity().getWindowManager().getDefaultDisplay().getRotation())
                        .build();
    }




    /*
    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }

     */

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            //imageView.setImageBitmap(imageBitmap);
            analyze(imageBitmap);
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.imgCapture) {

            //checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

            dispatchTakePictureIntent();

        }
    }

/*
    private void takePhoto(){


        /*
        File filechild =
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        ,"IMG_"+System.currentTimeMillis()+".jpg");


        if(filechild.exists()){
            try{
                if(filechild.createNewFile())Log.i("PayFrag", "Created file");
            }catch (IOException e){
                Log.w("PayFrag", "Failed to create file", e);
            }
        }

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(filechild).build();


        imageCapture.takePicture(outputFileOptions, Runnable::run, new OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull OutputFileResults outputFileResults)
            {
                Log.i("Pictur ", "Picture Taken ");
            }



            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.w("Pictur ", "Picture Taken", exception);
            }

        });



       imageCapture.takePicture(Runnable::run, new ImageCapture.OnImageCapturedCallback() {
           @Override
           public void onCaptureSuccess(@NonNull ImageProxy imageProxy){

                new CardImageAnalyzer().analyze(imageProxy);
           }
       });
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
        } else {
            Snackbar.make(picImage, " Permission already granted", BaseTransientBottomBar.LENGTH_LONG).show();
            takePhoto();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(picImage, "Storage Permission Granted", BaseTransientBottomBar.LENGTH_LONG).show();
                takePhoto();
            } else {
                Snackbar.make(picImage, "Storage Permission Denied", BaseTransientBottomBar.LENGTH_LONG).show();
            }
        }
    }


*/


        public void analyze(Bitmap bitmap) {


            FirebaseVisionImage imageTaken = FirebaseVisionImage.fromBitmap(bitmap);

            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();

                detector.processImage(imageTaken)
                        .addOnSuccessListener(result -> {
                            // Task completed successfully
                            // ...
                            List<FirebaseVisionText.TextBlock> blocks = result.getTextBlocks();
                            StringBuilder recognisedText = new StringBuilder("");
                            for (int i = 0; i < blocks.size(); i++) {
                                recognisedText.append(blocks.get(i).getText() +"\n");
                            }
                            Log.i("Analyzer", "Hello " + recognisedText.toString());
                        })
                        .addOnFailureListener(e -> {
                            // Task failed with an exception
                            // ...
                            Log.w("Analyzer", "Image not got", e);
                        });


    }
}
