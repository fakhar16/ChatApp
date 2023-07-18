package com.samsung.whatsapp.utils;

import static com.samsung.whatsapp.ApplicationClass.context;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;

import com.samsung.whatsapp.R;
import com.samsung.whatsapp.model.User;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static String MESSAGE_CHANNEL_ID = "MESSAGE";
    public static String INCOMING_CALL_CHANNEL_ID = "incoming_call";
    public static int INCOMING_CALL_NOTIFICATION_ID = 16;
    public static int INCOMING_MESSAGE_NOTIFICATION_ID = 17;
    public static final String ACTION_REJECT_CALL = "reject_call";
    public static final String TYPE_MESSAGE = "type_message";
    public static final String TYPE_VIDEO_CALL = "type_video_call";
    public static final String TYPE_DISCONNECT_CALL_BY_USER = "type_disconnect_call_user";
    public static final String TYPE_DISCONNECT_CALL_BY_OTHER_USER = "type_disconnect_call_other_user";
    public static User currentUser = null;
    public static final int ITEM_SENT = 1;
    public static final int ITEM_RECEIVE = 2;

    public static final String TAG = "Console";

    public static void showLoadingBar(Activity activity, View view) {
        view.setVisibility(View.VISIBLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static void dismissLoadingBar(Activity activity, View view) {
        view.setVisibility(View.GONE);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getFileType(Uri uri) {
        ContentResolver r = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(r.getType(uri));
    }


    public static boolean isSameDay(long date1) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(new Date(date1));
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(new Date(new Timestamp(System.currentTimeMillis()).getTime()));
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
                && calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
    }

    public static String getDateTimeString(long time) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        if (Utils.isSameDay(time))
            return timeFormat.format(new Date(time));
        else {
            return dateFormat.format(new Date(time));
        }
    }

    public static String getDateString(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        return dateFormat.format(new Date(time));
    }

    public static String getTimeString(long time) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        return timeFormat.format(new Date(time));
    }

    public static void copyMessage(String message) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(context.getString(R.string.USER_MESSAGE_TEXT), message);
        clipboardManager.setPrimaryClip(clipData);
    }

    public static void copyImage(Uri uri, String message_id) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newRawUri(context.getString(R.string.USER_MESSAGE_IMAGE), uri);
        clipData.addItem(new ClipData.Item(message_id));
        clipData.addItem(new ClipData.Item(context.getString(R.string.IMAGE)));
        clipboardManager.setPrimaryClip(clipData);
    }

    public static void copyVideo(Uri uri, String message_id) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newRawUri(context.getString(R.string.USER_MESSAGE_VIDEO), uri);
        clipData.addItem(new ClipData.Item(message_id));
        clipData.addItem(new ClipData.Item(context.getString(R.string.VIDEO)));
        clipboardManager.setPrimaryClip(clipData);
    }
}
