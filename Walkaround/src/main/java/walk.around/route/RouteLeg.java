package walk.around.route;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class RouteLeg implements JSONObjectParser,
		Parcelable {
	private Venue mStartVenue;
	private Venue mEndVenue;

    private List<RouteStep> mSteps;

	private int mDistance;
    private String mDistanceString;

	public static Creator<RouteLeg> CREATOR = new Creator<RouteLeg>() {

		@Override
		public RouteLeg[] newArray(int size) {
			return new RouteLeg[size];
		}

		@Override
		public RouteLeg createFromParcel(Parcel source) {
			return new RouteLeg(source);
		}
	};

	public RouteLeg(Parcel in) {
		mStartVenue = in.readParcelable(RouteLeg.class.getClassLoader());
		mEndVenue = in.readParcelable(RouteLeg.class.getClassLoader());
				
		mDistance = in.readInt();
        mDistanceString = in.readString();

        mSteps = new ArrayList<RouteStep>();
        in.readList(mSteps, RouteLeg.class.getClassLoader());
	}

	public RouteLeg(JSONObject json, Venue startVenue, Venue endVenue) {
		mStartVenue = startVenue;
		mEndVenue = endVenue;
        mSteps = new ArrayList<RouteStep>();

		try {
			parseJSON(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void parseJSON(JSONObject json) throws JSONException {
		JSONObject jsonStartLocation = json.getJSONObject("start_location");
		JSONObject jsonEndLocation = json.getJSONObject("end_location");

		// Verify start location
		assert mStartVenue.getLocation().latitude == jsonStartLocation
				.getInt("lat")
				&& mStartVenue.getLocation().longitude == jsonStartLocation
						.getInt("lng");

		// Verify end location
		assert mEndVenue.getLocation().latitude == jsonEndLocation
				.getInt("lat")
				&& mEndVenue.getLocation().longitude == jsonEndLocation
						.getInt("lng");


        mDistance = json.getJSONObject("distance").getInt("value");
        mDistanceString = json.getJSONObject("distance").getString("text");

		JSONArray jsonSteps = json.getJSONArray("steps");
		for (int i = 0; i < jsonSteps.length(); i++) {
			JSONObject jsonStep = jsonSteps.getJSONObject(i);
			mSteps.add(new RouteStep(jsonStep));
		}
	}

    public Venue getStartVenue() {
        return mStartVenue;
    }

    public Venue getEndVenue() {
        return mEndVenue;
    }

    public List<RouteStep> getSteps() {
        return mSteps;
    }

    public int getDistance() {
        return mDistance;
    }

    public String getDistanceString() {
        return mDistanceString;
    }

    @Override
	public String toString() {
		return "RouteLeg [mStartVenue=" + mStartVenue + ", mEndVenue="
				+ mEndVenue + ", mDistance=" + mDistance + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(mStartVenue, flags);
		dest.writeParcelable(mEndVenue, flags);	

		dest.writeInt(mDistance);
        dest.writeString(mDistanceString);

        dest.writeList(mSteps);
	}

}
