package team_silatra.silatra;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by ABC on 30-Jan-18.
 */

public class CameraActivity extends AppCompatActivity{

    private Button btnCapture;
    private TextureView textureView;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static{
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;

    //Save to file
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_WRITE_STORAGE = 201;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private Handler uHandler;

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);

        textureView=(TextureView)findViewById(R.id.textureView);
        assert textureView != null;

        textureView.setSurfaceTextureListener(textureListener);

        btnCapture=(Button)findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Starting the thread to establish connection with server for further transmission
                takePicture();
            }
        });
    }

    private void takePicture() {

        if(cameraDevice == null){
            return;
        }
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            CameraCharacteristics characteristics= manager.getCameraCharacteristics(cameraDevice.getId());
            //Size[] jpegSizes = null;
            //if(characteristics!=null)
              //  jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                //        .getOutputSizes(ImageFormat.JPEG);

            //Capturing image with size 640*480
            int width=640;
            int height=480;
            /*if(jpegSizes!=null && jpegSizes.length>0)
            {
                width=jpegSizes[0].getWidth();
                height=jpegSizes[0].getHeight();
                Log.d("Height & Width", image.getHeight()+"");
            }*/
            final ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
            List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //Check Orientation based on Device
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation));

            File dir = new File(Environment.getExternalStorageDirectory() + "/SiLaTra");
            if(!(dir.exists() && dir.isDirectory()))
                dir.mkdirs();
            file = new File(Environment.getExternalStorageDirectory()+"/SiLaTra/IMGSilatra"+SilatraDetails.nextImageFileNumber()+".jpg");

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = null;
                    try{
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

                        // Code to change the orientation of the captured image


                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 98, out);
                        byte[] byteArray = out.toByteArray();
                        //Log.e("ByteArray","Length of BytesArray:"+byteArray.length);
                        save(byteArray);

                        // Trying something new here
                       /*final Image.Plane[] planes = image.getPlanes();
                        final ByteBuffer buffer = planes[0].getBuffer();
                        int offset = 0;
                        int pixelStride = planes[0].getPixelStride();
                        int rowStride = planes[0].getRowStride();
                        int rowPadding = rowStride - pixelStride * mWidth;
                        // create bitmap
                        Bitmap bitmap = Bitmap.createBitmap(mWidth+rowPadding/pixelStride, mHeight, Bitmap.Config.RGB_565);
                        bitmap.copyPixelsFromBuffer(buffer);
                        image.close();*/

                        new UDPClientActivity().execute(byteArray);

                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally{
                        {
                            if(image!=null)
                                image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException{
                    OutputStream outputStream = null;
                    try{
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    }
                    finally {
                        if(outputStream != null)
                            outputStream.close();
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener,mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(CameraActivity.this,"Saved "+file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try{
                        cameraCaptureSession.capture(captureBuilder.build(),captureListener, mBackgroundHandler);
                    }
                    catch(Exception e){}
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            },mBackgroundHandler);
        }
        catch (CameraAccessException e){
            e.printStackTrace();
        }

    }

    private void createCameraPreview() {
        try{
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            },null);
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
        catch (NullPointerException e){
            e.printStackTrace();
            Toast.makeText(this, "Width:"+imageDimension.getWidth()+"Height:"+imageDimension.getHeight(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePreview() {
        if(cameraDevice == null)
            Toast.makeText(CameraActivity.this, "Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(),null,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera(){
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            //Check Realtime permissions if run higher API 23
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        //Manifest.permission.WRITE_EXTERNAL_STORAGE,
                },REQUEST_CAMERA_PERMISSION);
                return;
            }

            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "No Storage Permission", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                },REQUEST_WRITE_STORAGE);
                return;
            }

            manager.openCamera(cameraId,stateCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "You can't use camera without permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        if(requestCode == REQUEST_WRITE_STORAGE)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "You can't use storage without permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if(textureView.isAvailable())
            openCamera();
        else
            textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();

    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }


    //AsyncTask Activity to establish connection with Server
    private class UDPClientActivity extends AsyncTask<byte[],String,String> {

        private DatagramSocket udpSocket;
        private InetAddress serverAddr;

        @Override
        public String doInBackground(byte[]... params){
            byte[] bytes=params[0];
            DatagramPacket packet;
            Boolean run=true;
            String translatedText=null;
            byte[] buffer = new byte[65507]; //Buffer
            //Toast.makeText(CameraActivity.this, "Length:"+bytes.length, Toast.LENGTH_SHORT).show();
            Log.d("Length","Length of bytes array:"+bytes.length);
           /* try {
                byte[] buf = ("files").getBytes();
                udpSocket = new DatagramSocket();
                serverAddr = InetAddress.getByName(SilatraDetails.serverIP);
                packet = new DatagramPacket(buf, buf.length, serverAddr, 9001);
                udpSocket.send(packet);
                Log.d("Files sent","Sent the string Files");
            } catch (IOException e) {}*/

            try
            {
                // Transmission of byte array to Server through UDP Socket
                udpSocket = new DatagramSocket();
                serverAddr = InetAddress.getByName(SilatraDetails.serverIP);
                packet = new DatagramPacket(bytes, bytes.length, serverAddr, 9001);
                udpSocket.send(packet);
                Log.d("Transmission", "Message Sent successfully");
            }catch (SocketException e) {
                Log.e("Socket Open:", "Error:", e);
            }catch (UnknownHostException e) {
                Log.e("Wrong IP:", "Error:", e);
            }catch (IOException e) {
                e.printStackTrace();
            }
        /*finally{
            while(run) {
                try {
                    byte[] message = new byte[8000];
                    packet = new DatagramPacket(message, message.length);
                    Log.i("UDP client: ", "about to wait to receive");
                    udpSocket.setSoTimeout(90000);
                    udpSocket.receive(packet);

                    translatedText = new String(message, 0, packet.getLength());
                    Log.d("Received text", translatedText);
                } catch (SocketTimeoutException e) {
                    Log.e("Timeout Exception", "UDP Connection:", e);
                    run = false;
                    udpSocket.close();
                } catch (IOException e) {
                    Log.e("UDP client - IOExcept.", "error: ", e);
                    run = false;
                    udpSocket.close();
                }
            }
        }*/
            return "Hello World";
        }

        @Override
        protected void onPreExecute() {}

        protected void onProgressUpdate(){}

        protected void onPostExecute(String translatedText) {
            //Set textview
        }
    }

}
