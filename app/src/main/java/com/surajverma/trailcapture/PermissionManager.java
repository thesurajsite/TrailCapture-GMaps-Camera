package com.surajverma.trailcapture;


import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
  private final Activity activity;

  // ArrayList to store Permissions
  private static final String[] REQUIRED_PERMISSIONS = {
      android.Manifest.permission.ACCESS_FINE_LOCATION,
      android.Manifest.permission.ACCESS_COARSE_LOCATION,
      android.Manifest.permission.INTERNET,
      android.Manifest.permission.CAMERA
  };

  private static final int REQUEST_CODE_PERMISSIONS = 1001;

  public PermissionManager(Activity activity) {
    this.activity = activity;
  }

  public boolean checkAndRequestPermissions() {
    List<String> missingPermissions = new ArrayList<>();

    // Store not granted permissions in ArrayList
    for (String permission : REQUIRED_PERMISSIONS) {
      if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
        missingPermissions.add(permission);
      }
    }

    // If not granted, Prompt Permission Request and Return False
    if (!missingPermissions.isEmpty()) {
      ActivityCompat.requestPermissions(activity, missingPermissions.toArray(new String[0]), REQUEST_CODE_PERMISSIONS);
      return false;
    }
    else {
      return true; // else Return True
    }
  }
}