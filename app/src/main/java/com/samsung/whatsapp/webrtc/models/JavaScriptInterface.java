package com.samsung.whatsapp.webrtc.models;

import static com.samsung.whatsapp.utils.Utils.TAG;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class JavaScriptInterface {
    @JavascriptInterface
    public void onPeerConnected(){
        Log.i(TAG, "onPeerConnected: called");
    }
}
