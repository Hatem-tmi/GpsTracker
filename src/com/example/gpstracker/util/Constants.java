package com.example.gpstracker.util;

import com.google.android.gms.maps.model.LatLng;

public class Constants {

	public static final LatLng TUNIS = new LatLng(36.882638, 9.955312);
	public static final LatLng SOUSSE = new LatLng(35.852025, 10.619786);

	public static class MapDirectionsAPI {
		public static final String MAP_DIRECTIONS_URL = "http://maps.googleapis.com/maps/api/directions/json";
		public static final int CONNECTION_TIMEOUT = 5000;

		public enum TravelMode {
			driving, walking, bicycling
		}

		public enum StatusCode {
			OK, NOT_FOUND, ZERO_RESULTS, MAX_WAYPOINTS_EXCEEDED, INVALID_REQUEST, OVER_QUERY_LIMIT, REQUEST_DENIED, UNKNOWN_ERROR
		}
	}
}
