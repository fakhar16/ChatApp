package com.samsung.whatsapp.webrtc.models;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class JavaScriptInterface {
    private static final String TAG = "JavaScriptInterface";

    @JavascriptInterface
    public void onPeerConnected(){
        Log.wtf(TAG, "onPeerConnected: called");
    }

}
