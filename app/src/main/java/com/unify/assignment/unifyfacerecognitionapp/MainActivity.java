package com.unify.assignment.unifyfacerecognitionapp;


import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private Button snapButton;
    private Camera myCamera;
    private SurfaceView preview;
    private SurfaceHolder previewHolder;
    private boolean inPreview=false;
    private boolean cameraConfigured=false;

    private Handler mHandler = new Handler();

    Encryption mEncryption = new Encryption();

    public int imageCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        snapButton = (Button) findViewById(R.id.snapButton);
        preview = (SurfaceView) findViewById(R.id.surfaceView);
        previewHolder = preview.getHolder();

        try {
            releaseCameraAndPreview();
            myCamera = Camera.open(1);

            previewHolder.addCallback(surfaceCallback);
            previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            myCamera.setDisplayOrientation(90);
        }catch (Exception e){
            //handle
            Toast.makeText(this, "COULDN'T FIND FRONT CAMERA", Toast.LENGTH_LONG).show();
        }

        snapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.post(mRunnable);
            }
        });
    }

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            myCamera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] bytes, Camera camera) {
                            if(imageCount < 10) {
                                Log.i("pictureCallback", "inside" + imageCount);

                                String s = new String(bytes);
                                byte[] encData = null;
                                try {
                                    encData = mEncryption.Encrypt(s);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                try {

                                    FileOutputStream outStream = new FileOutputStream(String.format(Environment.getExternalStorageDirectory().getPath() + "/TEST" + imageCount + ".enc"));
                                    outStream.write(encData);
                                    outStream.close();

                                    imageCount++;

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            camera.startPreview();
                        }
                    });

            Log.i("mRUNNABLE", "ImageCount:" + imageCount);

            if(imageCount < 10) {
                mHandler.postDelayed(mRunnable, 500);
            }else{
                Toast.makeText(MainActivity.this, "10 pictures taken.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private  void releaseCameraAndPreview() {
        if (myCamera != null) {
            myCamera.release();
            myCamera = null;
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width<=width && size.height<=height) {
                if (result==null) {
                    result=size;
                }
                else {
                    int resultArea=result.width*result.height;
                    int newArea=size.width*size.height;

                    if (newArea>resultArea) {
                        result=size;
                    }
                }
            }
        }

        return(result);
    }

    private void initPreview(int width, int height) {
        if (myCamera!=null && previewHolder.getSurface()!=null) {
            try {
                myCamera.setPreviewDisplay(previewHolder);
            }
            catch (Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }

            if (!cameraConfigured) {
                Camera.Parameters parameters=myCamera.getParameters();
                Camera.Size size=getBestPreviewSize(width, height,
                        parameters);

                if (size!=null) {
                    parameters.setPreviewSize(size.width, size.height);
                    myCamera.setParameters(parameters);
                    cameraConfigured=true;
                }
            }
        }
    }

    private void startPreview() {
        if (cameraConfigured && myCamera!=null) {
            myCamera.startPreview();
            inPreview=true;
        }
    }

    SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            // no-op -- wait until surfaceChanged()
        }

        public void surfaceChanged(SurfaceHolder holder,
                                   int format, int width,
                                   int height) {
            initPreview(width, height);
            startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };
}