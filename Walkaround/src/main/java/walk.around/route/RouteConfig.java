package walk.around.route;

import com.google.android.gms.maps.model.LatLng;

public class RouteConfig {
	
	private RouteCategory mCategory;
    private LimitType mLimitType;
	private int mLimit;
	private LatLng mStartLocation;
	
	public RouteConfig(RouteCategory category, LimitType limitType, int limit,  LatLng startLocation) {
		mCategory = category;
        mLimitType = limitType;
		mLimit = limit;
		mStartLocation = startLocation;
	}

	public RouteCategory getCategory() {
		return mCategory;
	}

	public void setCategory(RouteCategory category) {
		mCategory = category;
	}

    public LimitType getLimitType() {
        return mLimitType;
    }

    public void setLimitType(LimitType limitType) {
        mLimitType = limitType;
    }

    public int getLimit() {
        return mLimit;
    }

    public void setLimit(int limit) {
        mLimit = limit;
    }

    public LatLng getStartLocation() {
		return mStartLocation;
	}

	public void setStartLocation(LatLng startLocation) {
		mStartLocation = startLocation;
	}
	
}

