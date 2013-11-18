package walk.around.route;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

public class RouteStep implements JSONObjectParser, Parcelable {

	private LatLng mStartPoint;
	private LatLng mEndPoint;

	private int mDistance;

	private List<LatLng> mPolyline;
	private String mInstructions;

	public RouteStep(JSONObject json) {
		try {
			parseJSON(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

    private RouteStep(Parcel in) {
        mStartPoint = in.readParcelable(RouteStep.class.getClassLoader());
        mEndPoint = in.readParcelable(RouteStep.class.getClassLoader());
        mDistance = in.readInt();
        mPolyline = new ArrayList<LatLng>();
        in.readList(mPolyline, RouteStep.class.getClassLoader());
        mInstructions = in.readString();
    }

	public void parseJSON(JSONObject json) throws JSONException {
		JSONObject jsonStartLocation = json.getJSONObject("start_location");
		JSONObject jsonEndLocation = json.getJSONObject("start_location");
		
		mStartPoint = new LatLng(
                jsonStartLocation.getDouble("lat"),
                jsonStartLocation.getDouble("lng"));

		mEndPoint = new LatLng(
                jsonEndLocation.getDouble("lat"),
                jsonEndLocation.getDouble("lng"));
		
		JSONObject jsonDistanceValue = json.getJSONObject("distance");

		mDistance = jsonDistanceValue.getInt("value");
		mPolyline = PolyUtil.decode(json.getJSONObject("polyline").getString("points"));

		mInstructions = json.getString("html_instructions");
	}

	public LatLng getStartPoint() {
		return mStartPoint;
	}

	public LatLng getEndPoint() {
		return mEndPoint;
	}

	public int getDistance() {
		return mDistance;
	}

	public List<LatLng> getPolyline() {
		return mPolyline;
	}

	public String getInstructions() {
		return mInstructions;
	}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mStartPoint, flags);
        dest.writeParcelable(mEndPoint, flags);

        dest.writeInt(mDistance);

        dest.writeList(mPolyline);
        dest.writeString(mInstructions);
    }

    public static Creator CREATOR = new Creator<RouteStep>() {

        @Override
        public RouteStep[] newArray(int size) {
            return new RouteStep[size];
        }

        @Override
        public RouteStep createFromParcel(Parcel source) {
            return new RouteStep(source);
        }
    };
}
