package walk.around.route;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Venue implements Comparable<Venue>, JSONObjectParser, Parcelable {

    public static String START = "start";
    public static String END = "end";

	private String mId = "";

	private String mName = "";
	private String mDescription = "";

	private LatLng mLocation;
	private List<String> mCategories;
    private String mMainCategory;

	public static Creator<Venue> CREATOR = new Creator<Venue>() {

		@Override
		public Venue[] newArray(int size) {
			return new Venue[size];
		}

		@Override
		public Venue createFromParcel(Parcel source) {
			return new Venue(source);
		}
	};

    /**
     * Creates an empty venue object with the specified ID.
      * @param id
     */
	public Venue(String id) {
        setId(id);
    }

    /**
     * Creates a venue object with data copied from the one given as argument.
     * @param venue
     */
    public Venue(Venue venue) {
        setId(venue.getId());
        setName(venue.getName());
        setDescription(venue.getDescription());
        setLocation(venue.getLocation());
        setCategories(venue.getCategories());
    }

    /**
     * Create a venue object by unpacking the Parcel.
     * @param in
     */
	private Venue(Parcel in) {
		mId = in.readString();
		mName = in.readString();
		mDescription = in.readString();
		
		mLocation = in.readParcelable(getClass().getClassLoader());
		
		mCategories = new ArrayList<String>();
		in.readStringList(mCategories);
	}

    /**
     * Create a venue object by parsing the JSON object.
     * @param json
     */
    public Venue(JSONObject json) {
		try {
			parseJSON(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

    @Override
	public void parseJSON(JSONObject json) throws JSONException {
		Log.d("Venues", json.toString());

		setCategories(new ArrayList<String>());

		setId(json.getString("id"));
		setName(json.getString("name"));

		double latitude = json.getJSONObject("location").getDouble("lat");
		double longitude = json.getJSONObject("location").getDouble("lng");

		setLocation(new LatLng(latitude, longitude));

		JSONArray categories = json.getJSONArray("categories");
		for (int i = 0; i < categories.length(); i++) {
			String category = categories.getJSONObject(i).getString("name");
			getCategories().add(category);

            boolean isPrimary = categories.getJSONObject(i).getBoolean("primary");
            if(isPrimary) {
                setMainCategory(category);
            }
		}
		Log.d("Venues", "Venue created");
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public LatLng getLocation() {
		return mLocation;
	}

	public void setLocation(LatLng location) {
		mLocation = location;
	}

	public List<String> getCategories() {
		return mCategories;
	}

	public void setCategories(List<String> categories) {
		mCategories = categories;
	}

    public String getMainCategory() {
        return mMainCategory;
    }

    public void setMainCategory(String mainCategory) {
        mMainCategory = mainCategory;
    }

    @Override
	public String toString() {
		return "Venue [mId=" + mId + ", mName=" + mName + ", mDescription="
				+ mDescription + ", mLocation=" + mLocation + ", mCategories="
				+ mCategories + "]";
	}

	@Override
	public int compareTo(Venue other) {
		if (mId != null && other.mId != null) {
			return mId.compareTo(other.mId);
		}
		if (mName != null && other.mName != null) {
			return mName.compareTo(other.mName);
		}
		return 0;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Venue)) return false;

        Venue venue = (Venue) o;

        if (!mDescription.equals(venue.mDescription)
            || !mId.equals(venue.mId)
            || !mName.equals(venue.mName))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mId.hashCode();
        result = 31 * result + mName.hashCode();
        result = 31 * result + mDescription.hashCode();

        return result;
    }

    @Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mId);
		dest.writeString(mName);
		dest.writeString(mDescription);

		dest.writeParcelable(mLocation, flags);

		dest.writeStringList(mCategories);
	}

}
