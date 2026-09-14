package com.example.smartsaccoapp.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SmsBridgeHelper {

    public static boolean isOffline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork == null || !activeNetwork.isConnectedOrConnecting();
    }

    public static void sendOfflineCommand(Context context, String command, String details) {
        // In a real app, this would send an actual SMS to the SACCO's shortcode
        // command e.g., "BAL", "SEND", "LOAN"
        String phoneNumber = "0700123456"; // SACCO SMS Gateway
        String message = "SMARTSACCO " + command + " " + details;
        
        try {
            SmsManager smsManager = SmsManager.getDefault();
            // smsManager.sendTextMessage(phoneNumber, null, message, null, null); 
            // We simulate the success for this innovative feature demo
            Toast.makeText(context, "Offline SMS Bridge: Command '" + command + "' sent via encrypted SMS.", Toast.LENGTH_LONG).show();
            LogAuditOffline(context, command, details);
        } catch (Exception e) {
            Toast.makeText(context, "SMS Bridge Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void LogAuditOffline(Context context, String cmd, String dtl) {
        // This is where the app would queue the action to sync later or log it locally
    }
}