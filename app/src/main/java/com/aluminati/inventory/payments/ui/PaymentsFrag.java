package com.aluminati.inventory.payments.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.impl.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.aluminati.inventory.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class PaymentsFrag extends Fragment implements View.OnClickListener, LifecycleOwner, CameraXConfig.Provider {

    private static final String TAG = PaymentsFrag.class.getName();
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private View imgCapture;
    private TextureView textureView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageAnalysis imageAnalysis;
    private ImageCapture imageCapture;
    private ImageButton picImage;
    private PreviewView previewView;
    private final int STORAGE_PERMISSION_CODE = 2001;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Executor executor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(getResources().getLayout(R.layout.payments), container, false);

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

        picImage = view.findViewById(R.id.imgCapture);
        picImage.setOnClickListener(this);
        //imageCapture();

        executor = Executors.newSingleThreadExecutor();
        previewView = view.findViewById(R.id.view_finder);

        /*
        if (allPermissionsGranted()) {
            previewView.post(startCamera());
        } else {
            askForPermision();
        }

        textureView.addOnLayoutChangeListener((view1, i, i1, i2, i3, i4, i5, i6, i7) -> {
            updateTransform();
        });

         */

        return view;
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




    /*
    val previewConfig =


    // Build the viewfinder use case
    val preview = Preview(previewConfig)

    // Every time the viewfinder is updated, recompute layout
    preview.setOnPreviewOutputUpdateListener {

        // To update the SurfaceTexture, we have to remove it and re-add it
        val parent = viewFinder.parent as ViewGroup
        parent.removeView(viewFinder)
        parent.addView(viewFinder, 0)

        viewFinder.surfaceTexture = it.surfaceTexture
        updateTransform()
    }

    // Bind use cases to lifecycle
    // If Android Studio complains about "this" being not a LifecycleOwner
    // try rebuilding the project or updating the appcompat dependency to
    // version 1.1.0 or higher.
    CameraX.bindToLifecycle(this, preview)

     */

    private void startCamera(){

        Preview preview =
                new Preview.Builder()
                        .setTargetRotation(getActivity().getWindowManager().getDefaultDisplay().getRotation())
                        .setTargetResolution(new Size(640, 480))
                        .build();

        preview.setSurfaceProvider(previewView.getPreviewSurfaceProvider());


    }

    private void updateTransform(){

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

                        Bundle bundle = new Bundle();
                        bundle.putString("card_result", extractText(firebaseVisionText));

                        Card card = new Card();
                             card.setArguments(bundle);
                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.nav_host_fragment,card, "scanner_frag")
                                .addToBackStack("card_frag")
                                .commit();


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
        String resultText = result.getText();
        for (FirebaseVisionText.TextBlock block: result.getTextBlocks()) {
            String blockText = block.getText();
            Float blockConfidence = block.getConfidence();
            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for (FirebaseVisionText.Line line: block.getLines()) {
                String lineText = line.getText();
                Float lineConfidence = line.getConfidence();
                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                for (FirebaseVisionText.Element element: line.getElements()) {
                    String elementText = element.getText();
                    Log.i(TAG, elementText);
                    Float elementConfidence = element.getConfidence();
                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                    stringBuilder.append("#" + elementText);
                }
            }
        }


        return stringBuilder.toString();

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                imageAnalysis();
            } else {
                Toast.makeText(getContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void askForPermision(){
        ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }

    public boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(getActivity(), REQUIRED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), REQUIRED_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

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
            //analyze(imageBitmap);
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.imgCapture) {
           imageAnalysis();
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



    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }



}
