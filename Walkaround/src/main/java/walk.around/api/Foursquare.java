package walk.around.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Foursquare {

	private static final String BASE_URL = "https://api.foursquare.com/v2/";
	private static final String VENUES_EXPLORE = "venues/explore/";
	private static final String CLIENT_ID = "WR1XAA5I3OIM1BPFTAPSY3KTWVKTVJY5OZ4Q0PWKMUB4KPOG";
	private static final String CLIENT_SECRET = "ZB0ATSQBBMFWTNOJ5OMSTWCVZVGMBSH0GDXZZ2GPY3IHRTPK";

	public static String venuesExplore(LatLng location, String section,
			int limit) {
		String url = getAbsoluteUrl(VENUES_EXPLORE);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		// Put the location
		String locationString = String.format(Locale.getDefault(), "%f, %f",
				location.latitude, location.longitude);

		Log.d("Formatted Location", locationString);

		params.add(new BasicNameValuePair("limit", Integer.toString(limit)));
		params.add(new BasicNameValuePair("ll", locationString));

		// Put the section parameter
		params.add(new BasicNameValuePair("section", section));

		// Put client id and secret if user is not logged in
		params.add(new BasicNameValuePair("client_id", CLIENT_ID));
		params.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));

		return Http.get(url, params);
	}

	private static String getAbsoluteUrl(String relativeUrl) {
		return BASE_URL + relativeUrl;
	}

}
