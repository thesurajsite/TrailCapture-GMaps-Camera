# Trail Capture

Trail Capture is an Android application that integrates **Google Maps API** and **Camera functionality** to help users **search places, get directions, and capture photos along their journey**. The app visually differentiates between the **path already traveled (Green)** and the **path yet to be traveled (Blue)**.

## 📌 Features

- **Search & Navigate**: Use Google **Places API** to search locations and find recommended places.
- **Path Tracking**: The **Direction API** is used to draw the travel route with a polyline.
  - **Green Path**: Traveled path
  - **Blue Path**: Remaining path
- **Capture Photos with Location Markers**:
  - A **Camera Button** allows you to capture images while traveling.
  - The captured photo is saved in the **Gallery**.
  - A **Marker** is placed on the map where the photo was taken.
  - Clicking on the marker **opens the saved photo**.

## 🛠️ Tech Stack

- **Programming Language**: Java
- **UI Design**: XML
- **Google Map APIs Used**:
  1. **Basic Maps API** – Displays the map interface.
  2. **Places API** – Enables place search and recommendations.
  3. **Directions API** – Fetches and displays route polyline to a destination.
- **Camera Integration**: Captures photos and saves them to the gallery with location markers.

## 📸 How It Works

1. **Search for a place** using the search bar.
2. Get **directions** and start your journey.
3. The **traveled path appears in Green**, while the **remaining path appears in Blue**.
4. **Click a photo** using the camera button to mark a location.
5. The **photo gets saved to the gallery**, and a **marker is placed on the map**.
6. Tap the **marker** to **view the captured photo**.
