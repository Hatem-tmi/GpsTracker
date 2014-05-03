package com.example.gpstracker.activity;

import android.content.IntentSender;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.gpstracker.R;
import com.example.gpstracker.util.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener, OnMapClickListener {
	public static final String TAG = MapActivity.class.getSimpleName();

	public static final LatLng TUNIS = new LatLng(36.882638, 9.955312);
	public static final LatLng SOUSSE = new LatLng(35.852025, 10.619786);

	// Update frequency in milliseconds
	public static final long UPDATE_INTERVAL = 1000 * 5;

	private GoogleMap googleMap;

	private LocationClient locationClient;
	private LocationRequest mLocationRequest;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_layout);
		Log.d(TAG, "onCreate");

		initViews();

		locationClient = new LocationClient(getApplicationContext(), this, this);
		createLocationRequest();
	}

	@Override
	public void onStart() {
		if (locationClient != null) {
			locationClient.connect();
		}
		super.onStart();
	}

	@Override
	public void onStop() {
		if (locationClient != null) {
			locationClient.disconnect();
		}
		super.onStop();
	}

	private void initViews() {
		Log.d(TAG, "onCreateView");

		initMap();
	}

	private void initMap() {
		Log.d(TAG, "initMapView");
		googleMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		if (googleMap != null) {
			UiSettings settings = googleMap.getUiSettings();
			settings.setAllGesturesEnabled(true);
			settings.setCompassEnabled(true);

			googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			googleMap.setOnMapClickListener(this);

			Marker tunis = googleMap.addMarker(new MarkerOptions().position(
					TUNIS).title("Tunis"));
			Marker sousse = googleMap.addMarker(new MarkerOptions()
					.position(SOUSSE)
					.title("Sousse")
					.snippet("Ville du Sahel")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.ic_launcher)));

			// Move the camera instantly to sousse with a zoom of 15.
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SOUSSE, 15));

			// Zoom in, animating the camera.
			googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
		} else {
			Toast.makeText(getApplicationContext(),
					"Google Maps not available", Toast.LENGTH_LONG).show();
		}
	}

	private void createLocationRequest() {
		// Create the LocationRequest object
		mLocationRequest = LocationRequest.create();

		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		// Set the update interval to 5 seconds
		mLocationRequest.setInterval(UPDATE_INTERVAL);
	}

	private void animateCamera(LatLng latLng, int zoomLevel) {
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(latLng).zoom(zoomLevel).build();
		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
	}

	private void registerLocationListener() {
		locationClient.requestLocationUpdates(mLocationRequest, this);
	}

	private void unregisterLocationListener() throws IllegalStateException {
		locationClient.removeLocationUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged");

		try {
			unregisterLocationListener();
		} catch (IllegalStateException e) {
		}

		LatLng userLatLng = new LatLng(location.getLatitude(),
				location.getLongitude());

		animateCamera(userLatLng, 17);
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d(TAG, "Location Services request finished with error");

		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this, 9000);

				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			// If no resolution is available, display a dialog to the user with
			// the error.
			Log.e(TAG, "" + connectionResult.getErrorCode());
		}

	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.d(TAG, "Location Services is connected");
		registerLocationListener();
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "Location client drops because of an error");
		unregisterLocationListener();
	}

	@Override
	public void onMapClick(LatLng location) {
		String msg = "Address unavailbale";

		if (location != null) {
			Address address = Utils.getAddressFromLatLong(
					getApplicationContext(), location.latitude,
					location.longitude);

			if (address != null) {
				msg = String.format("Location : %s, Address : %s, %s, %s",
						location.toString(), address.getAddressLine(0),
						address.getAddressLine(1), address.getAddressLine(2));
			} else {
				msg = String.format("Location : %s", location.toString());
			}
		}

		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
	}
}