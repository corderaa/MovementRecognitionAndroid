package com.example.cameratest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.osgi.OpenCVInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity {

    CameraBridgeViewBase cameraBridgeViewBase;
    Mat currGray, prevGray, rgb, thresh;
    List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();
    boolean isInit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();



        cameraBridgeViewBase = findViewById(R.id.javaCameraView);

        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                currGray = new Mat();
                prevGray = new Mat();
                rgb = new Mat();
                thresh = new Mat();
            }

            @Override
            public void onCameraViewStopped() {

            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {



                if (!isInit){
                    prevGray = inputFrame.gray();
                    isInit = true;
                    return prevGray;
                }

                rgb = inputFrame.rgba();
                currGray = inputFrame.gray();

                Imgproc.GaussianBlur(currGray, currGray, new Size(25,25),0);

                Core.absdiff(prevGray, currGray, thresh);
                Imgproc.threshold(thresh, thresh, 25, 255, Imgproc.THRESH_BINARY);

                Imgproc.dilate(thresh, thresh, new Mat(), new Point(-1, -1), 2);
                Imgproc.findContours(thresh, cnts, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

                for (MatOfPoint m: cnts){
                    if (Imgproc.contourArea(m) > 10000) {
                        Rect r = Imgproc.boundingRect(m);
                        Imgproc.rectangle(rgb, r, new Scalar(0, 0, 255), 3);
                    }

                }
                cnts.clear();

                prevGray = currGray.clone();
                return rgb;
            }
        });

        if (OpenCVLoader.initDebug()){
            Log.d("OPENCV:APP", "success...");
            cameraBridgeViewBase.enableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraBridgeViewBase.enableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }

    void getPermission(){
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length>0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
            getPermission();
        }
    }
}