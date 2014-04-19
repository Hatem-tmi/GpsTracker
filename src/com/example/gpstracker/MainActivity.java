package com.example.gpstracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	private LocationManager locationManager;
	private Location currentLocation;
	private boolean listening;
	private boolean gpsInUse;

	private Button registerReceiver;
	private Button unregisterReceiver;

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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.e(TAG, "onCreate");

		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		registerReceiver = (Button) findViewById(R.id.registerReceiver);
		unregisterReceiver = (Button) findViewById(R.id.unregisterReceiver);

		registerReceiver.setOnClickListener(this);
		unregisterReceiver.setOnClickListener(this);
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

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);

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

	/**
	 * Show GPS disabled alert
	 */
	private void showGPSDisabledAlert() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?");
		alertDialogBuilder.setCancelable(false);
		
		alertDialogBuilder.setPositiveButton("Goto Settings Page To Enable GPS",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent callGPSSettingIntent = new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(callGPSSettingIntent);
							}
						});
		
		alertDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.registerReceiver:
			
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Toast.makeText(getApplicationContext(), "GPS is Enabled", Toast.LENGTH_SHORT)
						.show();

				this.requestLastKnownLocation();

				if (currentLocation != null) {
					String msg = String.format(
							"LastKnownLocation: lat=%s,  lng=%s",
							currentLocation.getLatitude(),
							currentLocation.getLongitude());

					Log.d(TAG, msg);
					Toast.makeText(getApplicationContext(), msg,
							Toast.LENGTH_SHORT).show();
				}

				this.startLocationUpdates();

			} else {
				showGPSDisabledAlert();
			}
			break;
		case R.id.unregisterReceiver:
			this.stopLocationUpdates();
			break;
		default:
			break;
		}
	}
}
