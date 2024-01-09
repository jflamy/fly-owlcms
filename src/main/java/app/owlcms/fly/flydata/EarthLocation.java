package app.owlcms.fly.flydata;

public class EarthLocation {
	private static final double EARTH_RADIUS = 6371000; // meters
	public double distance;
	public double latitude;
	public double longitude;
	public String code;
	String name;

	public EarthLocation(String name, String code, double latitude, double longitude) {
		this.name = name;
		this.code = code;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double calculateDistance(double lat1, double lon1) {
		// Convert latitude and longitude from degrees to radians
		double dLat = Math.toRadians(latitude - lat1);
		double dLon = Math.toRadians(longitude - lon1);

		// Haversine formula
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
		        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(latitude)) *
		                Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		// Calculate the distance
		return EARTH_RADIUS * c;
	}

	@Override
	public String toString() {
		return "[distance=" + distance + ", latitude=" + latitude + ", longitude=" + longitude + ", code="
				+ code + ", name=" + name + "]";
	}

	public double calculateDistance(EarthLocation otherLocation) {
		double calculateDistance = calculateDistance(otherLocation.latitude, otherLocation.longitude);
		setDistance(calculateDistance);
		return distance;
	}

	public String getCode() {
		return code;
	}

	public double getDistance() {
		return distance;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getName() {
		return name;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setName(String name) {
		this.name = name;
	}

}