package com.surajverma.trailcapture;

import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PolylineManager {

  public List<LatLng> fetchPolyline(LatLng start, LatLng destination, String apiKey) {
    String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                 "origin=" + start.latitude + "," + start.longitude +
                 "&destination=" + destination.latitude + "," + destination.longitude +
                 "&overview=full" +
                 "&key=" + apiKey;

    try {
      URL urlObj = new URL(url);
      HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
      connection.setRequestMethod("GET");

      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder response = new StringBuilder();
      String line;

      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();

      JSONObject jsonObject = new JSONObject(response.toString());
      JSONArray routes = jsonObject.getJSONArray("routes");

      if (routes.length() > 0) {
        String points = routes.getJSONObject(0)
                              .getJSONObject("overview_polyline")
                              .getString("points");
        return decodePolyline(points);
      } else {
        return new ArrayList<>();
      }
    } catch (Exception e) {
      Log.e("FetchPolyline", "Error: " + e.getMessage());
      return new ArrayList<>();
    }
  }

  // Complex Algorithm to Decode Polyline (Provided by Google)
  public List<LatLng> decodePolyline(String encoded) {
    List<LatLng> polyline = new ArrayList<>();
    int index = 0;
    int len = encoded.length();
    int lat = 0;
    int lng = 0;

    while (index < len) {
      int shift = 0;
      int result = 0;

      do {
        int b = encoded.charAt(index++) - 63;
        result |= (b & 0x1F) << shift;
        shift += 5;
      } while (index < len && encoded.charAt(index - 1) >= 0x20 + 63);

      int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
      lat += dlat;

      shift = 0;
      result = 0;

      do {
        int b = encoded.charAt(index++) - 63;
        result |= (b & 0x1F) << shift;
        shift += 5;
      } while (index < len && encoded.charAt(index - 1) >= 0x20 + 63);

      int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
      lng += dlng;

      polyline.add(new LatLng(lat / 1E5, lng / 1E5));
    }

    return polyline;
  }

  public void drawPolyline(GoogleMap map, List<LatLng> polylinePoints, int color) {
    if (!polylinePoints.isEmpty()) {
      map.addPolyline(
          new PolylineOptions()
              .addAll(polylinePoints)
              .width(10f)
              .color(color)
              .geodesic(true));
    }
  }

  public void updatePolyline(GoogleMap googleMap, List<LatLng> polylinePoints, LatLng currentLocation,
                             LatLng destination, Map<MarkerOptions, Object> markersToPreserve) {
    if (polylinePoints.isEmpty()) return;

    int closestIndex = 0;
    float minDistance = Float.MAX_VALUE;

    for (int index = 0; index < polylinePoints.size(); index++) {
      LatLng point = polylinePoints.get(index);
      float distance = distanceBetween(currentLocation, point);
      if (distance < minDistance) {
        minDistance = distance;
        closestIndex = index;
      }
    }

    googleMap.clear();

    // Traveled Path in Green
    List<LatLng> traveledPath = polylinePoints.subList(0, closestIndex + 1);
    new PolylineManager().drawPolyline(googleMap, traveledPath, Color.GREEN);

    // Remaining Path in Blue
    List<LatLng> remainingPath = polylinePoints.subList(closestIndex, polylinePoints.size());
    new PolylineManager().drawPolyline(googleMap, remainingPath, Color.BLUE);

    // Re-add destination marker
    googleMap.addMarker(new MarkerOptions().position(new LatLng(destination.latitude, destination.longitude)));

    // Re-add all preserved markers
    for (MarkerOptions markerOptions : markersToPreserve.keySet()) {
      googleMap.addMarker(markerOptions);
    }
  }

  private float distanceBetween(LatLng p1, LatLng p2) {
    float[] results = new float[1];
    Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results);
    return results[0];
  }
}