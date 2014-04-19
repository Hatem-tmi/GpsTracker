package com.example.gpstracker.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class GpsTrackerService extends Service {
	private static final String TAG = GpsTrackerService.class.getSimpleName();
	private static final int UPDATE_PERIODE = 1000 * 60 * 15;

	private LocationManager locationManager;
	private Location currentLocation;
	private boolean listening;
	private boolean gpsInUse;

	/**
	 * Defines a listener that responds to location updates
	 */
	private LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			listening = true;
			currentLocation = location;

			if (currentLocation != null) {
				String msg = String.format("Location changed: lat=%s,  lng=%s",
						currentLocation.getLatitude(),
						currentLocation.getLongitude());

				Log.d(TAG, msg);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
						.show();
			}
		}

		/**
		 * Called when the provider status changes. This method is called when a
		 * provider is unable to fetch a location or if the provider has
		 * recently become available after a period of unavailability.
		 */
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
				listening = false;
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		this.requestLastKnownLocation();

		if (currentLocation != null) {
			String msg = String.format("LastKnownLocation: lat=%s,  lng=%s",
					currentLocation.getLatitude(),
					currentLocation.getLongitude());

			Log.d(TAG, msg);
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
					.show();
		}

		this.startLocationUpdates();

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		stopLocationUpdates();

		this.locationManager = null;
		super.onDestroy();
	}

	/**
	 * Requesting last location from GPS or Network provider
	 */
	private void requestLastKnownLocation() {

		if (currentLocation != null) {
			return;
		}

		// get last known location from gps provider
		Location location = this.locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (location == null) {
			// try network provider
			location = this.locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}

		currentLocation = location;
	}

	/**
	 * Start location updates
	 */
	private void startLocationUpdates() {
		listening = false;

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				UPDATE_PERIODE, 0, locationListener);

		// setting gpsInUse to true, but listening is still false at this point
		// listening is set to true with first location update in
		// LocationListener.onLocationChanged
		gpsInUse = true;
	}

	/**
	 * stop location updates
	 */
	private void stopLocationUpdates() {
		locationManager.removeUpdates(locationListener);
		listening = false;
		gpsInUse = false;
	}
}
