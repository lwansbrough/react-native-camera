/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/4/16.
 * Android video recording support by Marc Johnson (me@marc.mn) 4/2016
 */

package com.lwansbrough.RCTCamera;

import android.media.MediaRecorder;
import com.facebook.react.bridge.*;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lwansbrough.JavaCamera.CameraModule;

public class RCTCameraModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static final String TAG = "RCTCameraModule";

    private CameraModule cameraModule;

    private static ReactApplicationContext _reactContext;

    public RCTCameraModule(ReactApplicationContext reactContext) {
        super(reactContext);
        _reactContext = reactContext;
        _reactContext.addLifecycleEventListener(this);

        //TODO: Here is the best place to call the constructor of camera, but here don't have all the infos needed
        //cameraModule = new CameraModule(_reactContext);
        //cameraModule = new CameraModule();
    }

    public static ReactApplicationContext getReactContextSingleton() {
        return _reactContext;
    }

    @Override
    public String getName() {
        return "RCTCameraModule";
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
            {
                put("Aspect", getAspectConstants());
                put("BarCodeType", getBarCodeConstants());
                put("Type", getTypeConstants());
                put("CaptureQuality", getCaptureQualityConstants());
                put("CaptureMode", getCaptureModeConstants());
                put("CaptureTarget", getCaptureTargetConstants());
                put("Orientation", getOrientationConstants());
                put("FlashMode", getFlashModeConstants());
                put("TorchMode", getTorchModeConstants());
            }

            private Map<String, Object> getAspectConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("stretch", RCTCameraUtils.RCT_CAMERA_ASPECT_STRETCH);
                        put("fit", RCTCameraUtils.RCT_CAMERA_ASPECT_FIT);
                        put("fill", RCTCameraUtils.RCT_CAMERA_ASPECT_FILL);
                    }
                });
            }

            private Map<String, Object> getBarCodeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        // @TODO add barcode types
                    }
                });
            }

            private Map<String, Object> getTypeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("front", RCTCameraUtils.RCT_CAMERA_TYPE_FRONT);
                        put("back", RCTCameraUtils.RCT_CAMERA_TYPE_BACK);
                    }
                });
            }

            private Map<String, Object> getCaptureQualityConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("low", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_LOW);
                        put("medium", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_MEDIUM);
                        put("high", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_HIGH);
                        put("photo", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_HIGH);
                        put("preview", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_PREVIEW);
                        put("480p", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_480P);
                        put("720p", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_720P);
                        put("1080p", RCTCameraUtils.RCT_CAMERA_CAPTURE_QUALITY_1080P);
                    }
                });
            }

            private Map<String, Object> getCaptureModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("still", RCTCameraUtils.RCT_CAMERA_CAPTURE_MODE_STILL);
                    }
                });
            }

            private Map<String, Object> getCaptureTargetConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("memory", RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_MEMORY);
                        put("disk", RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_DISK);
                        put("cameraRoll", RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL);
                        put("temp", RCTCameraUtils.RCT_CAMERA_CAPTURE_TARGET_TEMP);
                    }
                });
            }

            private Map<String, Object> getOrientationConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("landscapeLeft", RCTCameraUtils.RCT_CAMERA_ORIENTATION_LANDSCAPE_LEFT);
                        put("landscapeRight", RCTCameraUtils.RCT_CAMERA_ORIENTATION_LANDSCAPE_RIGHT);
                        put("portrait", RCTCameraUtils.RCT_CAMERA_ORIENTATION_PORTRAIT);
                        put("portraitUpsideDown", RCTCameraUtils.RCT_CAMERA_ORIENTATION_PORTRAIT_UPSIDE_DOWN);
                    }
                });
            }

            private Map<String, Object> getFlashModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("off", RCTCameraUtils.RCT_CAMERA_FLASH_MODE_OFF);
                        put("on", RCTCameraUtils.RCT_CAMERA_FLASH_MODE_ON);
                        put("auto", RCTCameraUtils.RCT_CAMERA_FLASH_MODE_AUTO);
                    }
                });
            }

            private Map<String, Object> getTorchModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("off", RCTCameraUtils.RCT_CAMERA_TORCH_MODE_OFF);
                        put("on", RCTCameraUtils.RCT_CAMERA_TORCH_MODE_ON);
                        put("auto", RCTCameraUtils.RCT_CAMERA_TORCH_MODE_AUTO);
                    }
                });
            }
        });
    }

    //region CAPTURE
    @ReactMethod
    public void capture(final ReadableMap options, final Promise promise) throws Exception {

        ObjectNode j_options = RCTCameraUtils.toJsonObject(options);
        cameraModule = new CameraModule(j_options);

        cameraModule.__capture(promise);
    }
    //endregion


    //region STOP CAPTURE
    @ReactMethod
    public void stopCapture(final Promise promise) {
        cameraModule.__stopCapture(promise);
    }
    //endregion


    //region HAS FLASH
    @ReactMethod
    public void hasFlash(ReadableMap options, final Promise promise) {
        //.__hasFlash(options.getInt("type"), promise);
        cameraModule.__hasFlash(RCTCameraUtils.MEDIA_TYPE_IMAGE, promise);
    }
    //endregion


    //region MEDIAFILE TREATMENTS
    private File getOutputMediaFile(int type) {
        return cameraModule.__getOutputMediaFile(type);
    }

    private File getOutputCameraRollFile(int type) {
        return cameraModule.__getOutputCameraRollFile(type);
    }

    private File getOutputFile(int type, File storageDir) {
        return cameraModule.__getOutputFile(type, storageDir);
    }

    private File getTempMediaFile(int type) {
        return cameraModule.__getTempMediaFile(type);
    }

    private void addToMediaStore(String path) {
        cameraModule.__addToMediaStore(path);
    }
    //endregion


    //region RESOLVE IMAGE
    private void resolveImage(final File imageFile, final Promise promise, boolean addToMediaStore) {
        cameraModule.__resolveImage(imageFile, promise, addToMediaStore);
    }
    //endregion


    /**
     * LifecycleEventListener overrides
     */
    @Override
    public void onHostResume() {
        // ... do nothing
    }

    @Override
    public void onHostPause() {
        // On pause, we stop any pending recording session
    }

    @Override
    public void onHostDestroy() {
        // ... do nothing
    }
}