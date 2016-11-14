package com.unify.assignment.unifyfacerecognitionapp;


import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private Button snapButton;
    private Camera myCamera;
    private SurfaceView preview;
    private SurfaceHolder previewHolder;
    private boolean inPreview=false;
    private boolean cameraConfigured=false;

    public int imageCount = -1;

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
                //
                imageCount++;
                myCamera.takePicture(null, null, pictureCallback);
            }
        });
    }


    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {

            byte[] counter = ByteBuffer.allocate(4).putInt(1695609641).array();
            new SaveImageTask().execute(bytes, counter);

            /*
            FileOutputStream outStream = null;
            try {
                // Write to SD Card

                outStream = new FileOutputStream(String.format(Environment.getExternalStorageDirectory().getPath()+"/TEST"+imageCount+".jpg"));
                outStream.write(bytes);
                outStream.close();
            } catch (FileNotFoundException e) { // <10>
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.startPreview();
            */
        }
    };

    private  void releaseCameraAndPreview() {
        if (myCamera != null) {
            myCamera.release();
            myCamera = null;
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
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


class SaveImageTask extends AsyncTask<byte[], Void, Void> {

    byte[] keyBytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 }; //Choose a key wisely



    int count = 0;
    @Override
    protected Void doInBackground(byte[]... data) {
        count ++;

        // Write to SD Card
        try {
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            FileOutputStream outStream = null;
            //count = data[1].;

            outStream = new FileOutputStream(String.format(Environment.getExternalStorageDirectory().getPath() + "/TEST" + count + ".jpg"));
            outStream.write(data[0]);
            outStream.close();

            FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/TEST" + count + ".jpg");
            CipherInputStream cis = new CipherInputStream(fis, cipher);
            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/encTEST" + count + ".enc");
            byte[] b = new byte[8];
            int i = cis.read(b);
            while (i != -1) {
                fos.write(b, 0, i);
                i = cis.read(b);
            }
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } finally {
        }
        return null;
    }

}