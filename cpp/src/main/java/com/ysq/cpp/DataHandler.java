package com.ysq.cpp;

public class DataHandler {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("qrcode");
    }


    public native byte[] arrayFromJNI(byte[] data, int w, int h);
}
