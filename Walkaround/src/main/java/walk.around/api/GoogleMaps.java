package walk.around.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import walk.around.route.Venue;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class GoogleMaps {

	/* Static constants */
	private static final String BASE_URL = "https://maps.googleapis.com/maps/api/";
	private static final String DISTANCE_MATRIX = "distancematrix/json";
	private static final String DIRECTIONS = "directions/json";
	private static final String AUTOCOMPLETE = "place/autocomplete/json";

    private static  final String KEY = "AIzaSyAMTU0lhEWpNSspcoAOiIQMGRiNr82Pfmc";

    private static String PLACE_TYPE_GEOCODE = "geocode";
    private static String PLACE_TYPE_ESTABLISHMENT = "establishent";

    public static LocationBiasSettings locationBiasSettings = new LocationBiasSettings();

	public static String distanceMatrix(List<Venue> venues) {
		String url = getAbsoluteUrl(DISTANCE_MATRIX);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mode", "walking"));
		params.add(new BasicNameValuePair("units", "metric"));

		StringBuilder destinations = new StringBuilder();
		for (Venue venue : venues) {
			LatLng location = venue.getLocation();
			destinations.append(String.format("%f,%f|", location.latitude,
					location.longitude));
		}
		destinations.deleteCharAt(destinations.length() - 1);

		params.add(new BasicNameValuePair("origins", destinations.toString()));
		params.add(new BasicNameValuePair("destinations", destinations
				.toString()));
		params.add(new BasicNameValuePair("sensor", "true"));

		Log.d("Matrix", "Matrix called");
		Log.d("Matrix", url);
		Log.d("Matrix", params.toString());

		return Http.get(url, params);
	}

	public static String directions(List<Venue> venues) {
		String url = getAbsoluteUrl(DIRECTIONS);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mode", "walking"));
		params.add(new BasicNameValuePair("units", "metric"));

		StringBuilder waypoints = new StringBuilder();
		for (Venue venue : venues) {
			LatLng location = venue.getLocation();
			waypoints.append(String.format("%f,%f|", location.latitude,
					location.longitude));
		}

		params.add(new BasicNameValuePair("waypoints", waypoints.toString()));
		params.add(new BasicNameValuePair("sensor", "true"));

		return Http.get(url, params);
	}

    public static String autocomplete(String input) {
        String url = getAbsoluteUrl(AUTOCOMPLETE);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("input", input));
        params.add(new BasicNameValuePair("key", KEY));
        params.add(new BasicNameValuePair("sensor", "true"));

        if(locationBiasSettings.isEnabled()) {
            LatLng location = locationBiasSettings.getLocation();
            int radius = locationBiasSettings.getRadius();
            String placeType = locationBiasSettings.getPlaceType();

            if(location != null) {
                params.add(new BasicNameValuePair("location", String.format("%f,%f", location.latitude, location.longitude)));
                params.add(new BasicNameValuePair("radius", String.valueOf(radius)));
            }

            if(placeType != null) {
                params.add(new BasicNameValuePair("types", placeType));
            }
        }

        return Http.get(url, params);
    }

	private static String getAbsoluteUrl(String relativeUrl) {
		return BASE_URL + relativeUrl;
	}

    public static class LocationBiasSettings {

        private LatLng mLocation;
        private int radius;
        private String placeType;
        private boolean mEnabled = true;

        public LatLng getLocation() {
            return mLocation;
        }

        public void setLocation(LatLng location) {
            mLocation = location;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public boolean isEnabled() {
            return mEnabled;
        }

        public void setEnabled(boolean enabled) {
            mEnabled = enabled;
        }

        public String getPlaceType() {
            return placeType;
        }

        public void setPlaceType(String placeType) {
            this.placeType = placeType;
        }
    }

}
