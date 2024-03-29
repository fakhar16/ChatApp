package com.samsung.whatsapp.utils;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.CountDownTimer;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.samsung.whatsapp.ApplicationClass;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.model.User;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
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

    //For audio recording
    private static MediaRecorder recorder;
    private static MediaPlayer mPlayer;
    public static CountDownTimer countDownTimer = null;
    public static boolean isRecordingPlaying = false;

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
        ContentResolver r = ApplicationClass.application.getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(r.getType(uri));
    }

    @SuppressLint("Recycle")
    public static String getFileSize(Uri fileUri) {
        AssetFileDescriptor fileDescriptor;
        try {
            fileDescriptor = ApplicationClass.application.getApplicationContext().getContentResolver().openAssetFileDescriptor(fileUri , "r");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        long fileSize = fileDescriptor.getLength();

        return humanReadableByteCountSI(fileSize);
    }

    @SuppressLint("DefaultLocale")
    private static String humanReadableByteCountSI(long bytes) {
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
        ClipboardManager clipboardManager = (ClipboardManager) ApplicationClass.application.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(ApplicationClass.application.getApplicationContext().getString(R.string.USER_MESSAGE_TEXT), message);
        clipboardManager.setPrimaryClip(clipData);
    }

    public static void copyImage(Uri uri, String message_id) {
        ClipboardManager clipboardManager = (ClipboardManager) ApplicationClass.application.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newRawUri(ApplicationClass.application.getApplicationContext().getString(R.string.USER_MESSAGE_IMAGE), uri);
        clipData.addItem(new ClipData.Item(message_id));
        clipData.addItem(new ClipData.Item(ApplicationClass.application.getApplicationContext().getString(R.string.IMAGE)));
        clipboardManager.setPrimaryClip(clipData);
    }

    public static void copyVideo(Uri uri, String message_id) {
        ClipboardManager clipboardManager = (ClipboardManager) ApplicationClass.application.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newRawUri(ApplicationClass.application.getApplicationContext().getString(R.string.USER_MESSAGE_VIDEO), uri);
        clipData.addItem(new ClipData.Item(message_id));
        clipData.addItem(new ClipData.Item(ApplicationClass.application.getApplicationContext().getString(R.string.VIDEO)));
        clipboardManager.setPrimaryClip(clipData);
    }

    public static void copyDoc(Uri uri, String message_id, String fileName, String fileSize) {
        ClipboardManager clipboardManager = (ClipboardManager) ApplicationClass.application.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newRawUri(ApplicationClass.application.getApplicationContext().getString(R.string.USER_MESSAGE_FILE), uri);
        clipData.addItem(new ClipData.Item(message_id));
        clipData.addItem(new ClipData.Item(ApplicationClass.application.getApplicationContext().getString(R.string.PDF_FILES)));
        clipData.addItem(new ClipData.Item(fileName));
        clipData.addItem(new ClipData.Item(fileSize));
        clipboardManager.setPrimaryClip(clipData);
    }

    public static float dipToPixels(Context context, float dipValue){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,  dipValue, metrics);
    }

    public static void startRecording(String file_name) {
        String file_path=ApplicationClass.application.getApplicationContext().getFilesDir().getPath();
        File file= new File(file_path);

        if (!file.exists()){
            file.mkdirs();
        }

        String full_file_name=file+"/" + file_name + ".3gp";

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(full_file_name);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.i(TAG, "startRecording: recording Prepare() failed");
            Log.i(TAG, "startRecording: " + e.getMessage());
        }
        recorder.start();
    }

    public static void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    public static void playAudioRecording(String filename) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(filename);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    public static void stopPlayingRecording() {
        mPlayer.release();
        mPlayer = null;
    }

    public static boolean isRecordingFileExist(File file) {
        return file.exists();
    }

    public static String getDuration(File file) {
        String durationStr;
        try (MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever()) {
            mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
            durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Utils.formatMilliSecond(Long.parseLong(durationStr));
    }

    public static Long getDurationLong(File file) {
        String durationStr;
        try (MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever()) {
            mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
            durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Long.parseLong(durationStr);
    }

    public static String formatMilliSecond(long milliseconds) {
        String finalTimerString = "";
        String secondsString;

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) finalTimerString = hours + ":";

        if (seconds < 10) secondsString = "0" + seconds;
        else secondsString = "" + seconds;

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }

    public static void updateAudioDurationUI(long duration, TextView durationText, ImageView playPause, SeekBar seekBar) {
        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long l) {
                durationText.setText(formatMilliSecond(l));
                int seekBarValue = 100- (int) ((l/(duration * 1.0)) * 100.0);
                seekBar.setProgress(seekBarValue);
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void onFinish() {
                durationText.setText(formatMilliSecond(duration));
                playPause.setImageResource(R.drawable.baseline_play_arrow_24);
                Utils.stopPlayingRecording();
                isRecordingPlaying = false;
                seekBar.setProgress(0);
            }
        }.start();
    }
}
