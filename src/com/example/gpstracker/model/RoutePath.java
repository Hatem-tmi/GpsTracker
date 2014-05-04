package com.example.gpstracker.model;

import java.util.List;

import com.example.gpstracker.util.Constants.MapDirectionsAPI.StatusCode;
import com.google.android.gms.maps.model.LatLng;

public class RoutePath {
	private static long index = 0;
	private long id;
	private List<LatLng> pathPoints;
	private String distance;
	private String duration;
	private boolean walkMode = false;
	private LatLng startPoint;
	private LatLng endPoint;
	private List<LatLng> wayPoints;
	private StatusCode statusCode = StatusCode.OK;

	public RoutePath() {
		id = index++;
	}

	public long getId() {
		return id;
	}

	public List<LatLng> getPathPoints() {
		return pathPoints;
	}

	public void setPathPoints(List<LatLng> pathPoints) {
		this.pathPoints = pathPoints;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public boolean isWalkMode() {
		return walkMode;
	}

	public void setWalkMode(boolean walkMode) {
		this.walkMode = walkMode;
	}

	public LatLng getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(LatLng startPoint) {
		this.startPoint = startPoint;
	}

	public LatLng getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(LatLng endPoint) {
		this.endPoint = endPoint;
	}

	public StatusCode getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(StatusCode statusCode) {
		if (statusCode != null)
			this.statusCode = statusCode;
	}

	public List<LatLng> getWayPoints() {
		return wayPoints;
	}

	public void setWayPoints(List<LatLng> wayPoints) {
		this.wayPoints = wayPoints;
	}

	@Override
	public String toString() {
		return "RoutePath [id=" + id + ", pathPoints=" + pathPoints
				+ ", distance=" + distance + ", duration=" + duration
				+ ", walkMode=" + walkMode + ", startPoint=" + startPoint
				+ ", endPoint=" + endPoint + ", wayPoints=" + wayPoints
				+ ", statusCode=" + statusCode + "]";
	}
}
