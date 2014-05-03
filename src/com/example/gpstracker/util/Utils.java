package com.example.gpstracker.util;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

public class Utils {
	/**
	 * Get address from latitude&longitude
	 * 
	 * @param context
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public static Address getAddressFromLatLong(Context context,
			double latitude, double longitude) {
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		List<Address> addressList = null;

		try {
			addressList = geocoder.getFromLocation(latitude, longitude, 1);
		} catch (Exception e) {
			Log.e("TAG", e.getStackTrace()[0].toString());
		}

		if (addressList != null && addressList.size() > 0) {
			return addressList.get(0);
		} else {
			return null;
		}
	}
}
