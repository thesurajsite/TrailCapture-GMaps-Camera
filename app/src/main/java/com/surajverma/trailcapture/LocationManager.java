package com.surajverma.trailcapture;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import androidx.annotation.NonNull;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.concurrent.CompletableFuture;

public class LocationManager {

  @SuppressLint("MissingPermission")
  public CompletableFuture<LatLng> getCurrentLocation(Activity activity) {
    CompletableFuture<LatLng> future = new CompletableFuture<>();

    FusedLocationProviderClient fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity);

    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                 @Override
                                 public void onSuccess(Location location) {
                                   if (location != null) {
                                     future.complete(new LatLng(location.getLatitude(), location.getLongitude()));
                                   } else {
                                     future.complete(null);
                                   }
                                 }
                               })
                               .addOnFailureListener(new OnFailureListener() {
                                 @Override
                                 public void onFailure(@NonNull Exception e) {
                                   future.complete(null);
                                 }
                               });

    return future;
  }
}