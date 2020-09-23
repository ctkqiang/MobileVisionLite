package com.johnmelodyme.tensorflowlite;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class VisionActivity extends AppCompatActivity {
    private static final String TAG = "VisionActivity";
    private static final String MODEL_PATH = "mobilenet_quant_v1_224.tflite";
    private static final boolean QUANT = true;
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 224;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Classifier classifier;
    private TextView text_Result;
    private Button btn_Detect_Object, btn_Camera;
    private ImageView image_Result;
    private CameraView cameraView;


    protected void user_permission() {
        Dexter.withActivity(this).withPermissions(
                // TODO 添加写入文件
                Manifest.permission.CAMERA,
                Manifest.permission.INTERNET
        ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    Log.d(TAG, "onPermissionsChecked:  TRUE");
                } else if (report.isAnyPermissionPermanentlyDenied()) {
                    Toast.makeText(VisionActivity.this, "需要用戶協議", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).onSameThread().check();
    }


    private void ui_component() {
        cameraView = findViewById(R.id.camera_view);
        image_Result = findViewById(R.id.image_result);
        text_Result = findViewById(R.id.text_result);
        text_Result.setMovementMethod(new ScrollingMovementMethod());
        btn_Camera = findViewById(R.id.buttonCamera);
        btn_Detect_Object = findViewById(R.id.buttonDetectObject);
    }

    private void initTensorFlowAndLoadModel() {

        executor.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    classifier = ImageClassifierTensorflow.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                show_btn();
            }
        });

    }

    private void show_btn() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn_Detect_Object.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ui_component();

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
                image_Result.setImageBitmap(bitmap);

                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
                text_Result.setText(results.toString());

                Log.d(TAG, "结果 => " + results.toString());
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        btn_Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.toggleFacing();
            }
        });

        btn_Detect_Object.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.captureImage();
            }
        });
        initTensorFlowAndLoadModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }
}