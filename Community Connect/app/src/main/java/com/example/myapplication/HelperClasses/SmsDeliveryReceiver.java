package com.example.myapplication.HelperClasses;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

public class SmsDeliveryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (getResultCode()) {
            case Activity.RESULT_OK:
                Log.d("SMS", "Delivered successfully");
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                Log.e("SMS", "Generic failure");
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                Log.e("SMS", "No service");
                break;
        }
    }
}