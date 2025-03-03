package com.surajverma.trailcapture;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

public class StartActivity extends AppCompatActivity {

  private PermissionManager permissionManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_start);

    Button openMapButton = findViewById(R.id.openMapButton);

    // Animation
    LottieAnimationView lottieAnimation = findViewById(R.id.mapAnimation);
    lottieAnimation.setAnimation(R.raw.map);
    lottieAnimation.setRepeatCount(LottieDrawable.INFINITE);
    lottieAnimation.playAnimation();

    // Open Map Activity
    openMapButton.setOnClickListener(v -> {
      Intent intent = new Intent(this, MainActivity.class);
      startActivity(intent);
      finish();
    });

    // Check Permission using Permission Manager
    permissionManager = new PermissionManager(this);
    permissionManager.checkAndRequestPermissions();
  }

  @Override  // Callback for Check Permission
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 1001) {
      boolean allGranted = true;

      if (grantResults.length > 0) // If grantResults is less than 0 means user has not interacted properly with Permissions
      {
        // Check if all permissions are granted
        for (int result : grantResults) {
          if (result != PackageManager.PERMISSION_GRANTED) {
            allGranted = false;
            break;
          }
        }

        // Toast Message
        if (allGranted) {
          Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
        }
        else {
          Toast.makeText(this, "Permissions are required to use this app!", Toast.LENGTH_LONG).show();
          finish(); // Close Activity Preventing further Navigation
        }
      } else {
        Toast.makeText(this, "Permissions are required to use this app!", Toast.LENGTH_LONG).show();
        finish(); // Close Activity Preventing further Navigation
      }
    }
  }
}