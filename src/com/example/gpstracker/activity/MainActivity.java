package com.example.gpstracker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.gpstracker.R;
import com.example.gpstracker.service.GpsTrackerService;

public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	private Button registerReceiver;
	private Button unregisterReceiver;
	private Button openMapButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.e(TAG, "onCreate");

		registerReceiver = (Button) findViewById(R.id.registerReceiver);
		unregisterReceiver = (Button) findViewById(R.id.unregisterReceiver);
		openMapButton = (Button) findViewById(R.id.openMapButton);

		registerReceiver.setOnClickListener(this);
		unregisterReceiver.setOnClickListener(this);
		openMapButton.setOnClickListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Show GPS disabled alert
	 */
	private void showGPSDisabledAlert() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
				.setMessage("GPS is disabled in your device. Would you like to enable it?");
		alertDialogBuilder.setCancelable(false);

		alertDialogBuilder.setPositiveButton(
				"Goto Settings Page To Enable GPS",
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

			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Toast.makeText(getApplicationContext(), "GPS is Enabled",
						Toast.LENGTH_SHORT).show();

				// Start service
				Intent intent = new Intent(getApplicationContext(),
						GpsTrackerService.class);
				startService(intent);

			} else {
				showGPSDisabledAlert();
			}
			break;
		case R.id.unregisterReceiver:
			// Stop service
			Intent intent = new Intent(getApplicationContext(),
					GpsTrackerService.class);
			stopService(intent);
			break;
		case R.id.openMapButton:
			// TODO - Open Map View
			Intent intent1 = new Intent(getApplicationContext(), MapActivity.class);
			startActivity(intent1);
			break;
		default:
			break;
		}
	}
}
