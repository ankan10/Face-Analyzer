package com.example.facedetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private Button cameraButton;
    private final static int REQUEST_IMAGE_CAPTURE = 2;
    private InputImage image;
    private FaceDetector detector;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FirebaseApp.initializeApp(this);
        cameraButton = findViewById(R.id.camera_button);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            detectFace(bitmap);
        }
    }

    void detectFace(Bitmap bitmap) {
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setMinFaceSize(0.15f)
                        .enableTracking()
                        .build();

        try {
            image = InputImage.fromBitmap(bitmap, 0);
            detector = FaceDetection.getClient(options);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Task<List<Face>> result = detector.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<Face>>() {
                            @Override
                            public void onSuccess(List<Face> faces) {
                                String resultText="";
                                int i=1;
                                for(Face face: faces){
                                    resultText = resultText.concat("P"+i+". ").concat("Smile: "+ face.getSmilingProbability()*100+"%")
                                    .concat("\n    Left Eye: "+ face.getLeftEyeOpenProbability()*100+"%")
                                            .concat("\n    Right Eye: "+ face.getRightEyeOpenProbability()*100);
                                    i++;
                                }

                                if(faces.size()==0){
                                    Toast.makeText(MainActivity.this, "NO FACES", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Bundle bundle = new Bundle();
                                    bundle.putString(com.example.facedetection.FaceDetection.RESULT_TEXT, resultText);
                                    DialogFragment resultDialog = new ResultDialog();
                                    resultDialog.setArguments(bundle);
                                    resultDialog.setCancelable(false);
                                    resultDialog.show(getSupportFragmentManager(), com.example.facedetection.FaceDetection.RESULT_DIALOG);
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                            }
                        });
    }
}