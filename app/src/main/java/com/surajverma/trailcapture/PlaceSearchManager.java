package com.surajverma.trailcapture;


import android.content.Context;
import android.util.Log;
import androidx.fragment.app.FragmentManager;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import java.util.Arrays;

public class PlaceSearchManager {

  private final PlaceSelectionCallback callback;

  public PlaceSearchManager(Context context, FragmentManager fragmentManager, PlaceSelectionCallback callback) {
    this.callback = callback;

    if (!Places.isInitialized()) {
      Places.initialize(context, "AIzaSyB5tvtFU5vK7eaeUpBNug7waDt0bU0RZyE");
    }

    AutocompleteSupportFragment autocompleteFragment =
        (AutocompleteSupportFragment) fragmentManager.findFragmentById(R.id.autocomplete_fragment);

    if (autocompleteFragment != null) {
      autocompleteFragment.setPlaceFields(Arrays.asList(
          Place.Field.ID,
          Place.Field.NAME,
          Place.Field.LAT_LNG
                                                       ));

      autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
        @Override
        public void onPlaceSelected(Place place) {
          LatLng latLng = place.getLatLng();
          if (latLng != null) {
            callback.onPlaceSelected(latLng);
          }
        }

        @Override
        public void onError(Status status) {
          Log.e("PlaceSearch", "Error: " + status.getStatusMessage());
        }
      });
    }
  }
}

interface PlaceSelectionCallback {
  void onPlaceSelected(LatLng latLng);
}