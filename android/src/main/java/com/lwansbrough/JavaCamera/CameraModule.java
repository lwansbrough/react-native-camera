package com.lwansbrough.JavaCamera;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.facebook.react.bridge.Promise;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lwansbrough.RCTCamera.*;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraModule {
    private Context _reactContext;
    private static final String TAG = "CameraModule";

    public MediaActionSound sound = new MediaActionSound();

    Boolean safeToCapture = true;

    int _orientation;
    int _type;
    String _quality;
    Boolean _playSoundOnCapture;
    int _mode;
    Boolean _fixOrientation;
    int _jpegQuality;
    int _target;
    double _latitude;
    double _longitude;


    public CameraModule(ObjectNode options)
    {
        sound.load(MediaActionSound.SHUTTER_CLICK);
        this._reactContext = RCTCameraModule.getReactContextSingleton();

        InitCameraModuleByOptions(options);
    }

    public CameraModule(Context context, ObjectNode options)
    {
        sound.load(MediaActionSound.SHUTTER_CLICK);
        this._reactContext = context;

        InitCameraModuleByOptions(options);
    }

    private void InitCameraModuleByOptions(ObjectNode options)
    {
        if(options.has("orientation")) {
            JsonNode orientation = options.get("orientation");
            this._orientation = orientation.asInt();
        } else{
            this._orientation = RCTCameraUtils.RCT_CAMERA_ORIENTATION_PORTRAIT;
        }

        if(options.has("type")) {
            JsonNode type = options.get("type");
            this._type = type.asInt();
        } else{
            this._type = RCTCameraUtils.RCT_CAMERA_TYPE_BACK;
        }

        if(options.has("quality")) {
            JsonNode quality = options.get("quality");
            this._quality = quality.asText();
        } else{
            this._quality = RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_HIGH;
        }

        if(options.has("playSoundOnCapture")) {
            JsonNode playSoundOnCapture = options.get("playSoundOnCapture");
            this._playSoundOnCapture = playSoundOnCapture.asBoolean();
        } else{
            this._playSoundOnCapture = true;
        }

        if(options.has("mode")) {
            JsonNode mode = options.get("mode");
            this._mode = mode.asInt();
        } else{
            this._mode = RCTCameraUtils.RCT_CAMERA_CAPTURE_MODE_STILL;
        }

        if(options.has("fixOrientation")) {
            JsonNode fixOrientation = options.get("fixOrientation");
            this._fixOrientation = fixOrientation.asBoolean();
        } else{
            this._fixOrientation = false;
        }

        if(options.has("jpegQuality")) {
            JsonNode jpegQuality = options.get("jpegQuality");
            this._jpegQuality = jpegQuality.asInt();
        } else{
            this._jpegQuality = 85;
        }

        if(options.has("target")) {
            JsonNode target = options.get("target");
            this._target = target.asInt();
        } else{
            this._target = RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL;
        }

        if(options.has("latitude")) {
            JsonNode latitude = options.get("latitude");
            this._latitude = latitude.asDouble();
        } else{
            this._latitude = 0;
        }

        if(options.has("longitude")) {
            JsonNode longitude = options.get("longitude");
            this._longitude = longitude.asDouble();
        } else {
            this._longitude = 0;
        }
    }

    //TODO: REMOVE PROMISE!!!! HERE IS THE PLACE TO TREAT THE PICTURE DATA!!!
    private synchronized void __processImage(JavaMutableImage mutableImage, Promise promise){
        if(this._fixOrientation) {
            try {
                mutableImage.fixOrientation();
            } catch (JavaMutableImage.ImageMutationFailedException e) {
                promise.reject("Error fixing orientation image", e);
            }
        }

//        boolean shouldMirror = options.hasKey("mirrorImage") && options.getBoolean("mirrorImage");
//        if (shouldMirror) {
//            try {
//                mutableImage.mirrorImage();
//            } catch (JavaMutableImage.ImageMutationFailedException e) {
//                promise.reject("Error mirroring image", e);
//            }
//        }

        switch (this._target) {
            case RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_MEMORY:
                String encoded = mutableImage.toBase64(this._jpegQuality);

                //WritableMap response = new WritableNativeMap();

                JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
                final ObjectNode response = nodeFactory.objectNode();

                response.put("data", encoded);
                promise.resolve(response);
                break;
            case RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL: {
                File cameraRollFile = __getOutputCameraRollFile(RCTCameraUtils.MEDIA_TYPE_IMAGE);
                if (cameraRollFile == null) {
                    promise.reject("Error creating media file.");
                    return;
                }

                try {
                    mutableImage.writeDataToFile(cameraRollFile, this._latitude, this._longitude, this._jpegQuality);
                } catch (IOException | NullPointerException e) {
                    promise.reject("failed to save image file", e);
                    return;
                }

                __addToMediaStore(cameraRollFile.getAbsolutePath());

                __resolveImage(cameraRollFile, promise, true);

                break;
        }
            case RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_DISK: {
                File pictureFile = __getOutputMediaFile(RCTCameraUtils.MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    promise.reject("Error creating media file.");
                    return;
                }

                try {
                    mutableImage.writeDataToFile(pictureFile,  this._latitude, this._longitude, this._jpegQuality);
                } catch (IOException e) {
                    promise.reject("failed to save image file", e);
                    return;
                }

                __resolveImage(pictureFile, promise, false);

                break;
            }
            case RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_TEMP: {
                File tempFile = __getTempMediaFile(RCTCameraUtils.MEDIA_TYPE_IMAGE);
                if (tempFile == null) {
                    promise.reject("Error creating media file.");
                    return;
                }

                try {
                    mutableImage.writeDataToFile(tempFile,  this._latitude, this._longitude, 85);
                } catch (IOException e) {
                    promise.reject("failed to save image file", e);
                    return;
                }

                __resolveImage(tempFile, promise, false);

                break;
            }
        }
    }

    //TODO: REMOVE PROMISE!!!
    public void __capture(final Promise promise)throws Exception
    {
        Camera camera = RCTCamera.getInstance().acquireCameraInstance(this._type);
        if (null == camera) {
            throw new Exception("No camera found");
        }

        if (this._playSoundOnCapture) {
            sound.play(MediaActionSound.SHUTTER_CLICK);
        }

        if (this._quality != null) {
            RCTCamera.getInstance().setCaptureQuality(this._type, this._quality);
        }

        RCTCamera.getInstance().adjustCameraRotationToDeviceOrientation(this._type, this._orientation);
        camera.setPreviewCallback(null);

        Camera.PictureCallback captureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                camera.stopPreview();
                camera.startPreview();

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        __processImage(new JavaMutableImage(data), promise);
                    }
                });

                safeToCapture = true;
            }
        };

        if(safeToCapture) {
            try {
                camera.takePicture(null, null, captureCallback);
                safeToCapture = false;
            } catch(RuntimeException ex) {
                Log.e(TAG, "Couldn't capture photo.", ex);
            }
        }
    }

    //TODO: REMOVE PROMISE
    public  void __stopCapture(final Promise promise) {
        //        if (_mRecordingPromise != null) {
        //            __releaseMediaRecorder(); // release the MediaRecorder object
        //            promise.resolve("Finished recording.");
        //        } else {
        //            promise.resolve("Not recording.");
        //        }

        //TODO: HERE WE REMOVE THE CAPTURE VIDEO FUNCTION, SO EVER RETURN "NOT RECORDING"
        promise.resolve("Not recording");
    }

    //TODO: REMOVE PROMISE
    public void __hasFlash(int type, final Promise promise) {
        Camera camera = RCTCamera.getInstance().acquireCameraInstance(type);
        if (null == camera) {
            promise.reject("No camera found.");
        }
        List<String> flashModes = camera.getParameters().getSupportedFlashModes();
        promise.resolve(null != flashModes && !flashModes.isEmpty());
    }


    public File __getOutputMediaFile(int type) {
        // Get environment directory type id from requested media type.
        String environmentDirectoryType;
        if (type == RCTCameraUtils.MEDIA_TYPE_IMAGE) {
            environmentDirectoryType = Environment.DIRECTORY_PICTURES;
        }
        else {
            Log.e(TAG, "Unsupported media type:" + type);
            return null;
        }

        return __getOutputFile(
                type,
                Environment.getExternalStoragePublicDirectory(environmentDirectoryType)
        );
    }

    public File __getOutputCameraRollFile(int type) {
        return __getOutputFile(
                type,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        );
    }

    public File __getOutputFile(int type, File storageDir) {
        // Create the storage directory if it does not exist
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory:" + storageDir.getAbsolutePath());
                return null;
            }
        }

        // Create a media file name
        String fileName = String.format("%s", new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));

        if (type == RCTCameraUtils.MEDIA_TYPE_IMAGE) {
            fileName = String.format("IMG_%s.jpg", fileName);
        } else {
            Log.e(TAG, "Unsupported media type:" + type);
            return null;
        }

        return new File(String.format("%s%s%s", storageDir.getPath(), File.separator, fileName));
    }

    public File __getTempMediaFile(int type) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File outputDir = _reactContext.getCacheDir();
            File outputFile;

            if (type == RCTCameraUtils.MEDIA_TYPE_IMAGE) {
                outputFile = File.createTempFile("IMG_" + timeStamp, ".jpg", outputDir);
            } else {
                Log.e(TAG, "Unsupported media type:" + type);
                return null;
            }
            return outputFile;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    public void __addToMediaStore(String path) {
        MediaScannerConnection.scanFile(_reactContext, new String[] { path }, null, null);
    }

    //TODO: REMOVE PROMISE
    public void __resolveImage(final File imageFile, final Promise promise, boolean addToMediaStore) {
//        final WritableMap response = new WritableNativeMap();
//        response.putString("path", Uri.fromFile(imageFile).toString());

        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        final ObjectNode response = nodeFactory.objectNode();

        response.put("path", Uri.fromFile(imageFile).toString());


        if(addToMediaStore) {
            // borrowed from react-native CameraRollManager, it finds and returns the 'internal'
            // representation of the image uri that was just saved.
            // e.g. content://media/external/images/media/123
            MediaScannerConnection.scanFile(
                    _reactContext,
                    new String[]{imageFile.getAbsolutePath()},
                    null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            if (uri != null) {
                                //response.putString("mediaUri", uri.toString());
                                response.put("mediaUri", uri.toString());
                            }

                            promise.resolve(response);
                        }
                    });
        } else {

            promise.resolve(response);
        }
    }
}
