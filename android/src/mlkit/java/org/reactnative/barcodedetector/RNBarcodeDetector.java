package org.reactnative.barcodedetector;

import android.content.Context;
import android.util.Log;

import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeDetectorOptions;


public class RNBarcodeDetector {

    public static int NORMAL_MODE = 0;
    public static int ALTERNATE_MODE = 1;
    public static int INVERTED_MODE = 2;
    public static int ALL_FORMATS = Barcode.FORMAT_ALL_FORMATS;

    private BarcodeDetector mBarcodeDetector = null;
    private BarcodeDetectorOptions.Builder  mBuilder;

    private int mBarcodeType = Barcode.FORMAT_ALL_FORMATS;

    public RNBarcodeDetector(Context context) {
        mBuilder = new BarcodeDetectorOptions.Builder().setBarcodeFormats(mBarcodeType);
    }

    public boolean isOperational() {
        // Legacy api from GMV
        return true;
    }

    public BarcodeDetector getDetector() {

        if (mBarcodeDetector == null) {
            createBarcodeDetector();
        }
        return mBarcodeDetector;
    }

    public void setBarcodeType(int barcodeType) {
        if (barcodeType != mBarcodeType) {
            release();
            mBuilder.setBarcodeFormats(barcodeType);
            mBarcodeType = barcodeType;
        }
    }


    public void release() {
        if (mBarcodeDetector != null) {
            try {
                mBarcodeDetector.close();
            } catch (Exception e) {
                Log.e("RNCamera", "Attempt to close BarcodeDetector failed");
            }
            mBarcodeDetector = null;
        }
    }

    private void createBarcodeDetector() {
        BarcodeDetectorOptions options = mBuilder.build();
        mBarcodeDetector = .getInstance()
                .getVisionBarcodeDetector(options);

    }
}
