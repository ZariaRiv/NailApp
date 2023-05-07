package com.example.cameraxinjava2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, ImageAnalysis.Analyzer  {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    PreviewView previewView;

    ImageView onTop;

    ImageView image_view;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch handSwitch;
    Button GALLERY, take_picture;
    private ImageCapture imageCapture;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GALLERY =findViewById(R.id.GALLERY);
        take_picture = findViewById(R.id.take_picture);
        previewView = findViewById(R.id.previewView);
        onTop = findViewById(R.id.onTop);
        handSwitch = findViewById(R.id.handSwitch);


        handSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // on below line we are checking
            // if switch is checked or not.
            if (isChecked) {
                // on below line we are setting text
                // if switch is checked.
                onTop.setVisibility(View.INVISIBLE);
            } else {
                // on below line we are setting text
                // if switch is unchecked.
                onTop.setVisibility(View.VISIBLE);
            }
        });

        take_picture.setOnClickListener(this);
        GALLERY.setOnClickListener(this);

        cameraProviderFuture = ProcessCameraProvider.getInstance( this);
        cameraProviderFuture.addListener(this::run, getExecutor());
    }
    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        //image capture use case
        imageCapture = new ImageCapture.Builder()
                // they have minimize latency
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build();

        // image analysis use case
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();


        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
    }



    @SuppressLint({"NonConstantResourceId", "Range"})
    @Override
    public void onClick(View view){
        if (view.getId() == R.id.take_picture) {
            capturePhoto();
        }
        if (view.getId() == R.id.GALLERY) {
            // load the last saved picture from the gallery
            String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    MediaStore.Images.Media.DATE_ADDED + " DESC"
            );
            String imagePath = null;
            if (cursor.moveToFirst()) {
                imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();

            if (imagePath != null) {
                 //display the picture on the screen
                ImageView imageView = findViewById(R.id.image_view);
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                imageView.setImageBitmap(bitmap);

                // Initialize Chaquopy
                if (! Python.isStarted()) {
                    Python.start(new AndroidPlatform(this));
                }
                // call a Python script that takes the picture as an input and outputs a picture
                Python py = Python.getInstance();
                //PyObject np = py.getModule("numpy");
                //PyObject cv2 = py.getModule("cv2");
                //PyObject tf = py.getModule("tensorflow");
                PyObject pymodule = py.getModule("script");
                //PyObject pymodule = py.getModule("script");
                PyObject result = pymodule.callAttr("run_model", imagePath);

                // display the picture that the Python script outputs
                byte[] byteArray = result.toJava(byte[].class);
                Bitmap outputBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                imageView.setImageBitmap(outputBitmap);
            }
        }
    }

    private void capturePhoto() {
        long timestamp = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(MainActivity.this, "Photo saved "+ MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Toast.LENGTH_SHORT).show();
                        Log.d("capturing","Photo saved");
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException exception)
                    {
                        Toast.makeText(MainActivity.this, "Error Akysz jest super"+ exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    private void run() {
        try {
            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
            startCameraX(cameraProvider);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    Toast.makeText(MainActivity.this, "Permissions granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permissions denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void analyze(@NonNull ImageProxy image) {
        //Image processing here for the current frame
        Log.d("main activity_analyze", "analyze: got the frame at: "+ image.getImageInfo().getTimestamp());
        image.close();
    }


}