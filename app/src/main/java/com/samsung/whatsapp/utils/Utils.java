package com.samsung.whatsapp.utils;

import static com.samsung.whatsapp.ApplicationClass.context;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;

public class Utils {
//    public static ProgressDialog loadingBar;
    public static String MESSAGE_CHANNEL_ID = "MESSAGE";
    public static String INCOMING_CALL_CHANNEL_ID = "incoming_call";
    public static int INCOMING_CALL_NOTIFICATION_ID = 16;
    public static int INCOMING_MESSAGE_NOTIFICATION_ID = 17;
//    public static final String ACTION_ACCEPT_CALL = "accept_call";
    public static final String ACTION_REJECT_CALL = "reject_call";
//    public static final String ACTION_SHOW_INCOMING_CALL_SCREEN = "show_incoming_call_screen";
    public static final String TYPE_MESSAGE = "type_message";
    public static final String TYPE_VIDEO_CALL = "type_video_call";
    public static final String TYPE_DISCONNECT_CALL_BY_USER = "type_disconnect_call_user";
    public static final String TYPE_DISCONNECT_CALL_BY_OTHER_USER = "type_disconnect_call_other_user";

    public static void showLoadingBar(Activity activity, View view) {
        view.setVisibility(View.VISIBLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static void dismissLoadingBar(Activity activity, View view) {
        view.setVisibility(View.GONE);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public static String getFileType(Uri uri) {
        ContentResolver r = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(r.getType(uri));
    }
}
