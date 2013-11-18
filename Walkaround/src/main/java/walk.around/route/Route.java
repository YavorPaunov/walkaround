package walk.around.route;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import walk.around.utils.UnitFormatter;

public class Route implements JSONObjectParser, Parcelable {

	private List<Venue> mVenues;
	private List<RouteLeg> mLegs;
	private List<LatLng> mPolyline;

    private int mDistance;
    private String mDistanceString;

    private int mDuration;
    private String mDurationString;

	public static final Creator<Route> CREATOR = new Creator<Route>() {

		@Override
		public Route[] newArray(int size) {
			return new Route[size];
		}

		@Override
		public Route createFromParcel(Parcel source) {
			return new Route(source);
		}
	};

    /**
     * Create a route with the specified venues by parsing the JSONObject.
     *
     * NOTE: It is assumed that the venus contained in the JSON data are the same as in the venues
     * list.
     * @param content
     * @param venues
     */
	public Route(JSONObject content, List<Venue> venues) {
		mVenues = venues;
		mLegs = new ArrayList<RouteLeg>();
		try {
			parseJSON(content);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

    /**
     * Create a Route object by unpacking the Parcel.
     * @param in
     */
	public Route(Parcel in) {
		mVenues = new ArrayList<Venue>();
		in.readList(mVenues, Venue.class.getClass().getClassLoader());

		mLegs = new ArrayList<RouteLeg>();
		in.readList(mLegs, RouteLeg.class.getClass().getClassLoader());
		
		mPolyline = new ArrayList<LatLng>();
		in.readList(mPolyline, LatLng.class.getClass().getClassLoader());
	}

	@Override
	public void parseJSON(JSONObject json) throws JSONException {
        // Only 1 route expected
		JSONObject jsonRoute = json.getJSONArray("routes").getJSONObject(0);


		mPolyline = PolyUtil.decode(jsonRoute.getJSONObject("overview_polyline").getString("points"));

        mDistance = 0;
        mDuration = 0;

		JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
		for (int i = 0; i < jsonLegs.length(); i++) {
			JSONObject jsonLeg = jsonLegs.getJSONObject(i);
			mLegs.add(new RouteLeg(jsonLeg, mVenues.get(i), mVenues.get(i + 1)));
            mDistance += jsonLeg.getJSONObject("distance").getInt("value");
            mDuration += jsonLeg.getJSONObject("duration").getInt("value");
		}

        mDistanceString = UnitFormatter.metersToString(mDistance);
        mDurationString = UnitFormatter.secondsToString(mDuration);

		Log.d("Dir", "Great success!");
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeList(mVenues);
		dest.writeList(mLegs);
		dest.writeList(mPolyline);
	}

    public LatLngBounds getBounds(){
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (Venue venue: mVenues) {
            boundsBuilder.include(venue.getLocation());
        }
        return boundsBuilder.build();
    }

	public List<Venue> getVenues() {
		return mVenues;
	}

	public List<RouteLeg> getLegs() {
		return mLegs;
	}

	public List<LatLng> getPolyline() {
		return mPolyline;
	}

    public int getDistance() {
        return mDistance;
    }

    public String getDistanceString() {
        return mDistanceString;
    }

    public int getDuration() {
        return mDuration;
    }

    public String getDurationString() {
        return mDurationString;
    }
}
