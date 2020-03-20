package com.aluminati.inventory.payments.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.aluminati.inventory.HomeActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.ageVerification.FaceComparison;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class PaymentsFrag extends Fragment implements View.OnClickListener, LifecycleOwner, CameraXConfig.Provider {

    private static final String TAG = PaymentsFrag.class.getName();
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageAnalysis imageAnalysis;
    private ImageButton picImage;
    private PreviewView previewView;
    private Executor executor;
    private sendBackResult sendBackResult;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(getResources().getLayout(R.layout.scan_card), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        picImage = view.findViewById(R.id.imgCapture);
        picImage.setOnClickListener(this);

        executor = Executors.newSingleThreadExecutor();
        previewView = view.findViewById(R.id.view_finder);

        setCamera();


    }

    private void setCamera(){
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(getContext()));


    }


    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder()
                .setTargetName("Preview")
                .build();

        preview.setSurfaceProvider(previewView.getPreviewSurfaceProvider());

        CameraSelector cameraSelector =
                new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(720, 360))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();


        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }





    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private void imageAnalysis(){




        imageAnalysis.setAnalyzer(executor, image -> {
            if (image == null || image.getImage() == null) {
                return;
            }
            Image mediaImage = image.getImage();

            FirebaseVisionImage firebaseVisionImage =
                    FirebaseVisionImage.fromMediaImage(mediaImage, degreesToFirebaseRotation(image.getImageInfo().getRotationDegrees()));

            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();

            detector.processImage(firebaseVisionImage)
                    .addOnSuccessListener(firebaseVisionText -> {
                        Log.i(TAG, "Image analyzing");

                        if(getActivity() instanceof HomeActivity) {
                            String det = getArguments().getString("card_details");

                            det = det + extractText(firebaseVisionText);
                            Bundle bundle = new Bundle();
                            bundle.putString("card_details", det);
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.nav_host_fragment, Card.class, bundle, "card_frag")
                                    .commit();
                        }else if(getActivity() instanceof FaceComparison){
                        }

                    })
                    .addOnFailureListener(
                            e -> {
                                imageAnalysis.clearAnalyzer();
                                Log.w(TAG,"Failed to analyze image", e);
                            });
        });

    }

    private String extractText(FirebaseVisionText result){

        StringBuilder stringBuilder = new StringBuilder();
        for (FirebaseVisionText.TextBlock block: result.getTextBlocks()) {
            for (FirebaseVisionText.Line line: block.getLines()) {
                for (FirebaseVisionText.Element element: line.getElements()) {
                    String elementText = element.getText();
                    stringBuilder.append("#" + elementText);
                }
            }
        }
        Log.i(TAG, stringBuilder.toString());
        return stringBuilder.toString();
    }
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.imgCapture) {
                imageAnalysis();
        }
    }

    public interface sendBackResult<T extends AppCompatActivity> extends Serializable{
        void sendResult(String result);
    }

    public void setSendBackResult(sendBackResult sendBackResult){
        this.sendBackResult = sendBackResult;
    }

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }



}
