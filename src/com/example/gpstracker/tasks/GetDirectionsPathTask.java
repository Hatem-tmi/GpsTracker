package com.example.gpstracker.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.example.gpstracker.model.RoutePath;
import com.example.gpstracker.util.Constants.MapDirectionsAPI;
import com.example.gpstracker.util.Constants.MapDirectionsAPI.StatusCode;
import com.example.gpstracker.util.Constants.MapDirectionsAPI.TravelMode;
import com.google.android.gms.maps.model.LatLng;

/**
 * Thread used to get directions path using Google Map api
 * 
 * @author "Hatem Toumi"
 */
public class GetDirectionsPathTask extends Thread {
	private static final String TAG = GetDirectionsPathTask.class
			.getSimpleName();

	private Activity activity;
	private TravelMode travelMode;
	private LatLng startPoint = null;
	private LatLng endPoint = null;
	private List<LatLng> wayPoints;
	private GetDirectionsPathTaskCallback callback;

	public GetDirectionsPathTask(Activity activity, LatLng startPoint,
			LatLng endPoint, GetDirectionsPathTaskCallback callback) {
		this.activity = activity;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.callback = callback;
	}

	public void setTravelMode(TravelMode travelMode) {
		this.travelMode = travelMode;
	}

	public void setWayPoints(List<LatLng> wayPoints) {
		this.wayPoints = wayPoints;
	}

	@Override
	public void run() {
		StringBuilder url = new StringBuilder(
				MapDirectionsAPI.MAP_DIRECTIONS_URL);
		url.append(String.format("?origin=%s,%s",
				Double.toString(startPoint.latitude),
				Double.toString(startPoint.longitude)));
		url.append(String.format("&destination=%s,%s",
				Double.toString(endPoint.latitude),
				Double.toString(endPoint.longitude)));

		if (wayPoints != null && wayPoints.size() > 0) {
			url.append("&waypoints=optimize:true");
			for (LatLng waypoint : wayPoints) {
				try {
					url.append(URLEncoder.encode(
							String.format("|%s,%s",
									Double.toString(waypoint.latitude),
									Double.toString(waypoint.longitude)),
							"UTF-8"));
				} catch (UnsupportedEncodingException e) {
				}
			}
		}

		if (travelMode != null) {
			url.append("&mode=" + travelMode.toString()); // driving, walkMode,
															// bicycling or
															// transit
		}
		url.append("&sensor=true");

		try {
			// Invoke ws to get data
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams,
					MapDirectionsAPI.CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParams,
					MapDirectionsAPI.CONNECTION_TIMEOUT);

			// Instantiate an HttpClient
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpGet httpGet = new HttpGet(url.toString());
			HttpResponse httpResponse = httpClient.execute(httpGet);

			// Get string data from response
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(httpResponse.getEntity().getContent()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = bufferedReader.readLine()) != null)
				sb.append(line);

			String jsonString = sb.toString();
			if (jsonString != null) {
				Log.d(TAG, "Google Map api, response : " + jsonString);

				try {
					RoutePath path = parseRoutePath(jsonString, startPoint,
							endPoint);
					path.setWayPoints(wayPoints);
					callbackOnUIThread(path);
				} catch (JSONException e) {
					Log.w(TAG, "", e);

					callbackOnUIThread(null);
				} catch (Exception e) {
					Log.w(TAG, "", e);

					callbackOnUIThread(null);
				}
			} else {
				callbackOnUIThread(null);
			}
		} catch (ClientProtocolException e1) {
			Log.w(TAG, "", e1);
			callbackOnUIThread(null);
		} catch (IOException e1) {
			Log.w(TAG, "", e1);
			callbackOnUIThread(null);
		}
	}

	private void callbackOnUIThread(final RoutePath path) {
		// callback
		if (activity != null && callback != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (path != null) {
						if (path.getPathPoints() != null)
							callback.onDoneGettingDirectionsPath(path);
						else
							callback.onFailureGettingDirectionsPath(path
									.getStatusCode());
					} else {
						callback.onFailureGettingDirectionsPath(StatusCode.UNKNOWN_ERROR);
					}
				}
			});
		}
	}

	/**
	 * Get Route Path from JSON String
	 * 
	 * @param jsonString
	 * @param startPoint
	 * @param endPoint
	 * @return
	 */
	public RoutePath parseRoutePath(String jsonString, LatLng startPoint,
			LatLng endPoint) throws JSONException, Exception {
		RoutePath path = new RoutePath();
		path.setStartPoint(startPoint);
		path.setEndPoint(endPoint);

		JSONObject resultWs = new JSONObject(jsonString);
		if (resultWs.has("status") && resultWs.getString("status") != null) {
			path.setStatusCode(StatusCode.valueOf(resultWs.getString("status")));
		} else {
			path.setStatusCode(StatusCode.UNKNOWN_ERROR);
		}

		if (path.getStatusCode().equals(StatusCode.OK)) {
			JSONArray routesArray = resultWs.getJSONArray("routes");

			if (routesArray != null && routesArray.length() > 0) {
				JSONObject routes = routesArray.getJSONObject(0);

				// Get distance and duration
				if (routes != null && routes.has("legs")) {
					JSONObject legs = routes.getJSONArray("legs")
							.getJSONObject(0);
					path.setDistance(legs.getJSONObject("distance").getString(
							"text"));
					path.setDuration(legs.getJSONObject("duration").getString(
							"text"));
				}

				// Get list pathPoints
				if (routes != null && routes.has("overview_polyline")) {
					JSONObject overviewPolylines = routes
							.getJSONObject("overview_polyline");
					path.setPathPoints(decodePoly(overviewPolylines
							.getString("points")));
				}
			}
		}

		return path;
	}

	/**
	 * Method to decode polyline points Courtesy :
	 * http://jeffreysambells.com/2010
	 * /05/27/decoding-polylines-from-google-maps-direction-api-with-java
	 */
	private List<LatLng> decodePoly(String encoded) {

		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((((double) lat / 1E5)),
					(((double) lng / 1E5)));
			poly.add(p);
		}

		return poly;
	}

	/**
	 * Get Directions-Path Task Callback
	 * 
	 * @author "Hatem Toumi"
	 */
	public interface GetDirectionsPathTaskCallback {
		public void onDoneGettingDirectionsPath(RoutePath path);

		public void onFailureGettingDirectionsPath(StatusCode statusCode);
	}
}
