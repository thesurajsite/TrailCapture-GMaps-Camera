package com.surajverma.trailcapture;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PlaceSelectionCallback {

  private GoogleMap googleMap;
  private FusedLocationProviderClient fusedLocationClient;
  private List<LatLng> polylinePoints;

  private FloatingActionButton cameraButton;
  private Uri capturedImageUri;
  private Map<Marker, Uri> markerImageMap = new HashMap<>();
  private Map<MarkerOptions, Uri> markerOptionsMap = new HashMap<>();
  private LatLng destinationLatLng; // Store the destination

  private static final int REQUEST_IMAGE_CAPTURE = 1;
  private Handler mainHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_main);

    mainHandler = new Handler(Looper.getMainLooper());

    cameraButton = findViewById(R.id.cameraButton);
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    if (mapFragment != null) {
      mapFragment.getMapAsync(this);
    }

    new PlaceSearchManager(this, getSupportFragmentManager(), this);

    cameraButton.setOnClickListener(v -> {
      openCamera();
    });
  }

  @SuppressLint("MissingPermission")
  @Override
  public void onMapReady(GoogleMap map) {
    googleMap = map;
    googleMap.setMyLocationEnabled(true);

    googleMap.setOnMarkerClickListener(marker -> {
      Uri uri = markerImageMap.get(marker);
      if (uri != null) {
        showImageDialog(uri);
      } else {
        Toast.makeText(MainActivity.this, "No image associated with this marker", Toast.LENGTH_SHORT).show();
      }
      return true;
    });

    View mapView = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment)).getView();
    if (mapView != null) {
      View locationButton = mapView.findViewWithTag("GoogleMapMyLocationButton");
      if (locationButton != null) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin + 250, params.rightMargin, params.bottomMargin);
        locationButton.setLayoutParams(params);
      }
    }
  }

  @Override
  public void onPlaceSelected(LatLng destination) {
    this.destinationLatLng = destination;

    new Thread(() -> {
      LocationManager locationManager = new LocationManager();
      CompletableFuture<LatLng> locationFuture = locationManager.getCurrentLocation(MainActivity.this);

      locationFuture.thenAccept(currentLatLng -> {
        if (currentLatLng != null) {
          // Fetch the polyline from the Directions API
          new Thread(() -> {
            PolylineManager polylineManager = new PolylineManager();
            polylinePoints = polylineManager.fetchPolyline(
                new LatLng(currentLatLng.latitude, currentLatLng.longitude),
                new LatLng(destination.latitude, destination.longitude),
                "AIzaSyB5tvtFU5vK7eaeUpBNug7waDt0bU0RZyE"
                                                          );

            Log.d("Polyline", "Points: " + polylinePoints);

            mainHandler.post(() -> {
              Toast.makeText(MainActivity.this, polylinePoints.toString(), Toast.LENGTH_SHORT).show();

              // Draw the Polyline on the map and Add marker
              PolylineManager uiPolylineManager = new PolylineManager();
              uiPolylineManager.drawPolyline(googleMap, polylinePoints, Color.BLUE);
              googleMap.addMarker(new MarkerOptions().position(new LatLng(destination.latitude, destination.longitude)));

              startLocationUpdates(new LatLng(destination.latitude, destination.longitude));
            });
          }).start();
        } else {
          mainHandler.post(() -> {
            Toast.makeText(MainActivity.this, "Failed to get current location", Toast.LENGTH_SHORT).show();
          });
        }
      }).exceptionally(ex -> {
        mainHandler.post(() -> {
          Toast.makeText(MainActivity.this, "Error getting location: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        });
        return null;
      });
    }).start();
  }

  @SuppressLint("MissingPermission")
  private void startLocationUpdates(LatLng destination) {
    LocationRequest locationRequest = LocationRequest.create()
                                                     .setInterval(2000)
                                                     .setFastestInterval(1000)
                                                     .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        new LocationCallback() {
          @Override
          public void onLocationResult(@NonNull LocationResult locationResult) {
            for (android.location.Location location : locationResult.getLocations()) {
              new PolylineManager().updatePolyline(
                  googleMap,
                  polylinePoints,
                  new LatLng(location.getLatitude(), location.getLongitude()),
                  new LatLng(destination.latitude, destination.longitude),
                  (Map<MarkerOptions, Object>) (Map<?, ?>) markerOptionsMap);

              restoreMarkerImageAssociations();
            }
          }
        },
        null);
  }

  private void restoreMarkerImageAssociations() {
    markerImageMap.clear();

    for (MarkerOptions options : markerOptionsMap.keySet()) {
      Uri uri = markerOptionsMap.get(options);
      Marker marker = googleMap.addMarker(options);
      if (marker != null && uri != null) {
        markerImageMap.put(marker, uri);
      }
    }
  }

  private void openCamera() {
    ContentValues values = new ContentValues();
    values.put(MediaStore.Images.Media.TITLE, "Captured Image");
    values.put(MediaStore.Images.Media.DESCRIPTION, "Image captured by Camera");
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
    values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
    values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp"); // Saves to Pictures/MyApp

    capturedImageUri = getContentResolver().insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                                                  );

    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);

    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
      if (capturedImageUri != null) {
        Toast.makeText(this, "Image Saved to Gallery", Toast.LENGTH_SHORT).show();
        addMarkerAtCurrentLocation(capturedImageUri);
      }
    }
  }

  private void addMarkerAtCurrentLocation(Uri imageUri) {
    new Thread(() -> {
      LocationManager locationManager = new LocationManager();
      CompletableFuture<LatLng> locationFuture = locationManager.getCurrentLocation(MainActivity.this);

      locationFuture.thenAccept(location -> {
        if (location != null) {
          mainHandler.post(() -> {
            LatLng latLng = new LatLng(location.latitude, location.longitude);

            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.camera_marker);
            int width = 80;
            int height = 80;
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);

            MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Captured Photo")
                .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));

            markerOptionsMap.put(markerOptions, imageUri);

            Marker marker = googleMap.addMarker(markerOptions);
            if (marker != null) {
              markerImageMap.put(marker, imageUri);
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
          });
        } else {
          mainHandler.post(() -> {
            Toast.makeText(MainActivity.this, "Failed to get current location for marker", Toast.LENGTH_SHORT).show();
          });
        }
      }).exceptionally(ex -> {
        mainHandler.post(() -> {
          Toast.makeText(MainActivity.this, "Error getting location for marker: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        });
        return null;
      });
    }).start();
  }

  private void showImageDialog(Uri imageUri) {
    try {
      ImageView imageView = new ImageView(this);
      imageView.setAdjustViewBounds(true);
      imageView.setImageURI(imageUri);

      // Show Image Dialog
      new AlertDialog.Builder(this)
          .setView(imageView)
          .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
          .show();
    } catch (Exception e) {
      Toast.makeText(this, "Error showing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }
}