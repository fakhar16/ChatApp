package com.samsung.whatsapp.fcm;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.samsung.whatsapp.ApplicationClass;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.model.Notification;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FCMNotificationSender {
    private static final String BASE_URL = " https://fcm.googleapis.com/fcm/send";
    private static final String TAG = "ConsoleFCMNotificationSender";

    public static void SendNotification(Context context, Notification notification) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        RequestQueue queue = Volley.newRequestQueue(context);

        try {
            JSONObject data = new JSONObject();
            data.put(ApplicationClass.context.getString(R.string.TITLE), notification.getTitle());
            data.put(ApplicationClass.context.getString(R.string.MESSAGE), notification.getMessage());
            data.put(ApplicationClass.context.getString(R.string.TYPE), notification.getType());
            data.put(ApplicationClass.context.getString(R.string.ICON), notification.getIcon());
            data.put(ApplicationClass.context.getString(R.string.SENDER_ID), notification.getSender_id());
            data.put(ApplicationClass.context.getString(R.string.RECEIVER_ID), notification.getReceiver_id());

            JSONObject receiverJsonObject = new JSONObject();
            receiverJsonObject.put(ApplicationClass.context.getString(R.string.TO), notification.getTo_token());
            receiverJsonObject.put(context.getString(R.string.DATA), data);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, BASE_URL,
                    receiverJsonObject,
                    response -> Log.i(TAG, "onResponse: FCM: " + response ),
                    error -> {}) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> map = new HashMap<>();
                    map.put(ApplicationClass.context.getString(R.string.CONTENT_TYPE), ApplicationClass.context.getString(R.string.APPLICATION_JSON));
                    map.put(ApplicationClass.context.getString(R.string.AUTHORIZATION), ApplicationClass.context.getString(R.string.SERVER_KEY));

                    return map;
                }
            };

            queue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Log.i(TAG, "SendNotification: exception");
            throw new RuntimeException(e);
        }
    }
}
