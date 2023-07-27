package com.samsung.whatsapp.utils;

import static com.samsung.whatsapp.ApplicationClass.context;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;

import com.samsung.whatsapp.R;
import com.samsung.whatsapp.model.User;

import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    @SuppressLint("Range")
    public static String getFilename(Context context, Uri uri) {
        String res = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
               if (cursor != null && cursor.moveToFirst()) {
                   res = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
               }
            } finally {
                assert cursor != null;
                cursor.close();
            }

            if (res == null) {
                res = uri.getPath();
                int cutIndex = res.lastIndexOf('/');
                if (cutIndex != -1) {
                    res = res.substring(cutIndex + 1);
                }
            }
        }
        return res;
    }

    public static String getFileType(Uri uri) {
        ContentResolver r = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(r.getType(uri));
    }

    public static String getFileSize(String url) throws ExecutionException, InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Future<Integer> size = pool.submit(() -> {
            URL obj_url = new URL(url);
            URLConnection connection = (URLConnection) obj_url.openConnection();
            return connection.getContentLength();
        });

        return humanReadableByteCountSI(size.get().longValue());
    }

    @SuppressLint("DefaultLocale")
    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
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

    public static void copyDoc(Uri uri, String message_id) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newRawUri(context.getString(R.string.USER_MESSAGE_FILE), uri);
        clipData.addItem(new ClipData.Item(message_id));
        clipData.addItem(new ClipData.Item(context.getString(R.string.PDF_FILES)));
        clipboardManager.setPrimaryClip(clipData);
    }
}
