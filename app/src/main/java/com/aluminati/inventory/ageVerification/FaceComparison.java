package com.aluminati.inventory.ageVerification;

import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

import static com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions.ALL_CONTOURS;

public class FaceComparison {

    private FirebaseVisionFaceDetector firebaseVisionFaceDetector;
    private FirebaseVisionFaceDetectorOptions firebaseVisionFaceDetectorOptions;

    public FaceComparison(){
        this.firebaseVisionFaceDetectorOptions = buildFireBaseVisionFaceDetector();
    }

    private FirebaseVisionFaceDetectorOptions buildFireBaseVisionFaceDetector(){
        return new FirebaseVisionFaceDetectorOptions.Builder()
                .setContourMode(ALL_CONTOURS)
                .build();
    }

    private class FaceAnalyzer implements ImageAnalysis.Analyzer {

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

        @Override
        public void analyze(ImageProxy imageProxy, int degrees) {
            if (imageProxy == null || imageProxy.getImage() == null) {
                return;
            }
            Image mediaImage = imageProxy.getImage();
            int rotation = degreesToFirebaseRotation(degrees);
            FirebaseVisionImage image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation);
            FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(firebaseVisionFaceDetectorOptions);

            Task<List<FirebaseVisionFace>> result =
                    detector.detectInImage(image)
                            .addOnSuccessListener(
                                    faces -> {

                                   })
                            .addOnFailureListener(
                                    e -> {
                                        // Task failed with an exception
                                        // ...
                                    });
        }
    }
}
