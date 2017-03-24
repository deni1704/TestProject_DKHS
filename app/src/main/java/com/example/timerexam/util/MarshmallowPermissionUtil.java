package com.example.timerexam.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * Created by admin on 3/24/17.
 */

public class MarshmallowPermissionUtil {
    public static final int BODY_SENSORS_PERMISSION_REQUEST_CODE = 101;
    public static final int CALENDAR_PERMISSION_REQUEST_CODE = 102;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 103;
    public static final int CONTACTS_PERMISSION_REQUEST_CODE = 104;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 105;
    public static final int MICROPHONE_PERMISSION_REQUEST_CODE = 106;
    public static final int PHONE_PERMISSION_REQUEST_CODE = 107;
    public static final int SMS_PERMISSION_REQUEST_CODE = 108;
    public static final int STORAGE_PERMISSION_REQUEST_CODE = 109;
    public static final int ACCOUNT_PERMISSION_REQUEST_CODE = 110;
    public static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 111;
    public static final int SYSTEM_WINDOW_PERMISSION_REQUEST_CODE = 112;
    private static final String[] BODY_SENSORS_PERMS = { Manifest.permission.BODY_SENSORS };
    private static final String[] CALENDAR_STORAGE_PERMS = { Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR };
    private static final String[] CAMERA_PERMS = { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private static final String[] CONTACTS_PERMS = { Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS };
    private static final String[] LOCATION_PERMS = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION };
    private static final String[] MICROPHONE_PERMS = { Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private static final String[] PHONE_PERMS = { Manifest.permission.CALL_PHONE };
    private static final String[] SMS_PERMS = { Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS };
    private static final String[] STORAGE_PERMS = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private static final String[] ACCOUNT_PERMS = { Manifest.permission.GET_ACCOUNTS };
    private static final String[] SYSTEM_WINDOW_PERMS = { Manifest.permission.SYSTEM_ALERT_WINDOW };

    public static boolean checkPermissionForCamera(Activity activity){
        int result1 = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        int result2 = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public static void requestPermissionForCamera(Activity activity){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)){
            Toast.makeText(activity, "Camera permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, CAMERA_PERMS, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    public static boolean checkPermissionForContact(Activity activity){
        int result1 = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS);
        int result2 = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS);
        if (result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public static void requestPermissionForContact(Activity activity){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS)){
            Toast.makeText(activity, "Contact permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, CONTACTS_PERMS, CONTACTS_PERMISSION_REQUEST_CODE);
        }
    }

    public static boolean checkPermissionForLocation(Activity activity){
        int result1 = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        int result2 = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public static void requestPermissionForLocation(Activity activity){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(activity, "Location permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, LOCATION_PERMS, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public static boolean checkPermissionForStorage(Activity activity){
        int result1 = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public static void requestPermissionForStorage(Activity activity){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(activity, "External Storage permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, STORAGE_PERMS, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    public static boolean checkPermissionForAccount(Activity activity){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.GET_ACCOUNTS);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public static void requestPermissionForAccount(Activity activity){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.GET_ACCOUNTS)){
            Toast.makeText(activity, "Account permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, ACCOUNT_PERMS, ACCOUNT_PERMISSION_REQUEST_CODE);
        }
    }

    public static boolean checkPermissionForRecordAudio(Activity activity){
        int result1 = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        int result2 = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public static void requestPermissionForRecordAudio(Activity activity){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)){
            Toast.makeText(activity, "Audio Record and Write External Storage permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, MICROPHONE_PERMS, RECORD_AUDIO_PERMISSION_REQUEST_CODE);
        }
    }

    public static boolean checkPermissionForSystemWindow(Activity activity){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.SYSTEM_ALERT_WINDOW);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public static void requestPermissionForSystemWindow(Activity activity){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)){
            Toast.makeText(activity, "System Window permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity, SYSTEM_WINDOW_PERMS, SYSTEM_WINDOW_PERMISSION_REQUEST_CODE);
        }
    }
}
