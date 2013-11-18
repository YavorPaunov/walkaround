package walk.around.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.bricolsoftconsulting.geocoderplus.Address;
import com.bricolsoftconsulting.geocoderplus.Area;
import com.bricolsoftconsulting.geocoderplus.Geocoder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import walk.around.R;
import walk.around.api.GoogleMaps;
import walk.around.route.OnStartChangeListener;
import walk.around.route.Route;
import walk.around.route.Venue;
import walk.around.view.LocationSearchAdapter;

public class RouteMapFragment extends RouteFragment implements
        OnMapLongClickListener,
        OnMapClickListener,
        ConnectionCallbacks,
        OnConnectionFailedListener,
        OnItemClickListener,
        OnFocusChangeListener,
        TextWatcher {

    private final String TAG = "RouteMapFragment";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private LocationClient mLocationClient;

    private MapView mMapView;
    private GoogleMap mMap;

    private Bundle mBundle;
    private Marker mStartMarker;
    private Route mRoute;
    private OnStartChangeListener mStartChangedListener;

    private BiMap<Marker, Venue> mMarkerVenueMap;
    private AlertDialog mNoLocationDialog;

    private int mPolylineColor;
    private boolean mDrawOnAttach;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (!hasPlayServices()) {
            throw new RuntimeException("Google Play Services required.");
        }

        mBundle = savedInstanceState;
        mLocationClient = new LocationClient(this.getSherlockActivity(), this, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View topView = inflater.inflate(R.layout.fragment_route_map, container, false);

        mMapView = (MapView) topView.findViewById(R.id.map_view);
        mMapView.onCreate(mBundle);
        mMap = mMapView.getMap();
        mMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                LocationManager manager = (LocationManager) getActivity()
                        .getSystemService(Context.LOCATION_SERVICE);
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                        && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    showNoLocationDialog();
                    return true;
                }
                return false;
            }
        });
        AutoCompleteTextView searchView = (AutoCompleteTextView)
                topView.findViewById(R.id.search_location);
        if (mRoute == null) {
            searchView.setVisibility(View.VISIBLE);
            searchView.startAnimation(new ShowSearchAnimation(searchView));
        }

        getMap().setInfoWindowAdapter(new InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Venue venue = mMarkerVenueMap.get(marker);
                if (venue == null) {
                    return null;
                }

                View view;

                if (venue.getId().equals(Venue.START) || venue.getId().equals(Venue.END)) {
                    view = getLayoutInflater(null).inflate(
                            R.layout.default_venue_info_window, null);
                } else {
                    view = getLayoutInflater(null).inflate(
                            R.layout.venue_info_window, null);
                    TextView textCategory = (TextView) view.findViewById(R.id.text_venue_category);
                    textCategory.setText(venue.getMainCategory());
                }

                TextView textName = (TextView) view.findViewById(R.id.text_venue_name);
                textName.setText(venue.getName());

                return view;
            }
        });

        getMap().setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Venue venue = mMarkerVenueMap.get(marker);
                if (venue.getId().equals(Venue.START) || venue.getId().equals(Venue.END)) {
                    return;
                }

                String uri = "http://foursquare.com/venue/" + venue.getId();

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });

        return topView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity());
            getMap().setMyLocationEnabled(true);
            getMap().getUiSettings().setZoomControlsEnabled(false);
        } catch (GooglePlayServicesNotAvailableException e) {
            throw new RuntimeException("No Play.");
        }

        getView().findViewById(R.id.search_location).setOnFocusChangeListener(this);

        mMapView = (MapView) getView().findViewById(R.id.map_view);
        mMap = mMapView.getMap();

        getMap().setOnMapLongClickListener(this);
        getMap().setOnMapClickListener(this);

        AutoCompleteTextView searchView = (AutoCompleteTextView) getView().findViewById(R.id.search_location);
        searchView.setAdapter(new LocationSearchAdapter(getActivity(), android.R.layout.simple_list_item_1));
        searchView.setSelectAllOnFocus(true);
        searchView.setOnItemClickListener(this);
        searchView.addTextChangedListener(this);

        createNoLocationDialog();

        mLocationClient.connect();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (mDrawOnAttach && mRoute != null) {
            mDrawOnAttach = false;
            onRouteReceived(mRoute);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mLocationClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPolylineColor = getResources().getColor(
                R.color.route_polyline_red);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mRoute != null) {
            return;
        }

        if (mStartMarker != null) {
            mStartMarker.remove();
        }

        if (mLocationClient.getLastLocation() != null) {
            LatLng startLocation = new LatLng(mLocationClient.getLastLocation().getLatitude(),
                    mLocationClient.getLastLocation().getLongitude());
            dispatchStartChange(startLocation);
        } else {
            dispatchStartUnavailable();
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (mRoute != null) {
            return;
        }

        if (mStartMarker != null) {
            mStartMarker.remove();
        }

        animateToLocation(latLng, 15f);

        mStartMarker = getMap().addMarker(new MarkerOptions().
                draggable(true).
                position(latLng).
                icon(BitmapDescriptorFactory.defaultMarker()).
                title("Start Location"));

        dispatchStartChange(latLng);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.search_location:
                if (!hasFocus) {
                    InputMethodManager imm =
                            (InputMethodManager) getActivity().getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        LatLngBounds bounds = getMap().getProjection().getVisibleRegion().latLngBounds;
        float[] distanceResutlts = new float[1];
        Location.distanceBetween(
                bounds.northeast.latitude,
                bounds.northeast.longitude,
                bounds.southwest.latitude,
                bounds.southwest.longitude, distanceResutlts);
        int radius = (int) (distanceResutlts[0] / 2);

        GoogleMaps.locationBiasSettings.setLocation(getMap().getCameraPosition().target);
        GoogleMaps.locationBiasSettings.setRadius(radius);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        if (mRoute != null) {
            moveToBounds(mRoute.getBounds(), 150);
            return;
        }

        if (mStartMarker != null) {
            animateToLocation(mStartMarker.getPosition(), 15f);
            dispatchStartChange(mStartMarker.getPosition());
            return;
        }

        LocationManager manager = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (mRoute != null || mStartMarker != null) {
                showNoLocationDialog();
            }
            dispatchStartUnavailable();
            return;
        }
        mLocationClient.requestLocationUpdates(LocationRequest.create(), new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                dispatchStartChange(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        });

        if (mLocationClient.getLastLocation() != null) {
            animateToLastLocation(15f);
            LatLng latLng = new LatLng(
                    mLocationClient.getLastLocation().getLatitude(),
                    mLocationClient.getLastLocation().getLongitude());
            dispatchStartChange(latLng);
        }
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(getSherlockActivity(), "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this.getSherlockActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        String locationName = (String) textView.getText();

        AsyncTask<String, Void, LatLngBounds> getAddressTask = new AsyncTask<String, Void, LatLngBounds>() {

            private double latitude;
            private double longitude;

            @Override
            protected LatLngBounds doInBackground(String... params) {
                Geocoder geocoder = new Geocoder();

                LatLngBounds bounds = null;
                List<Address> addresses = null;

                try {
                    addresses = geocoder.getFromLocationName(params[0], 1);

                    if (addresses.size() > 0) {
                        Area area = addresses.get(0).getViewPort();

                        LatLng northeast = new LatLng(
                                area.getNorthEast().getLatitude(),
                                area.getNorthEast().getLongitude());
                        LatLng southwest = new LatLng(
                                area.getSouthWest().getLatitude(),
                                area.getSouthWest().getLongitude());
                        bounds = new LatLngBounds(southwest, northeast);

                        latitude = addresses.get(0).getLatitude();
                        longitude = addresses.get(0).getLongitude();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return bounds;
            }

            @Override
            protected void onPostExecute(LatLngBounds bounds) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 35);
                getMap().animateCamera(cameraUpdate);

                getView().findViewById(R.id.search_location).clearFocus();

                onMapLongClick(new LatLng(latitude, longitude));
            }
        };
        getAddressTask.execute(locationName);
    }

    /**
     * Create a dialog asking the user to enable location providers, if they are disabled.
     */
    private void createNoLocationDialog() {
        Dialog.OnClickListener positiveClickListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        };

        Dialog.OnClickListener negativeClickListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mRoute == null) {
                    dispatchStartUnavailable();
                }
            }
        };

        OnCancelListener cancelListener = new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mRoute == null) {
                    dispatchStartUnavailable();
                }
            }
        };

        mNoLocationDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Location Services Disabled")
                .setMessage(R.string.location_missing)
                .setPositiveButton("OK", positiveClickListener)
                .setNegativeButton("Cancel", negativeClickListener)
                .setOnCancelListener(cancelListener)
                .create();

    }

    /**
     * Show the location enable asking dialog.
     */
    private void showNoLocationDialog() {
        if (mNoLocationDialog != null) {
            mNoLocationDialog.show();
        }
    }

    /**
     * Animate the map to the last known location.
     *
     * @param zoom
     */
    private void animateToLastLocation(float zoom) {
        LatLng location = new LatLng(mLocationClient.getLastLocation().getLatitude(),
                mLocationClient.getLastLocation().getLongitude());
        animateToLocation(location, zoom);
    }

    /**
     * Animate the map to a location.
     *
     * @param location
     */
    public void animateToLocation(LatLng location) {
        if (mMapView.getWidth() == 0) {
            return;
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(location);
        getMap().animateCamera(cameraUpdate);
    }

    /**
     * Animate the map to a location, and zoom.
     *
     * @param location
     * @param zoom
     */
    public void animateToLocation(LatLng location, float zoom) {
        if (mMapView.getWidth() == 0) {
            return;
        }
        getMap().animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition
                .fromLatLngZoom(location, zoom)));
    }

    /**
     * Animate the map so that what is within the bounds is shown
     *
     * @param bounds
     * @param padding
     */
    public void animateToBounds(LatLngBounds bounds, int padding) {
        if (mMapView.getWidth() == 0) {
            return;
        }
        CameraUpdate cameraShowRoute = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        getMap().animateCamera(cameraShowRoute);
    }

    /**
     * Move the camera to the last known location, without animation.
     *
     * @param zoom
     */
    private void moveToLastLocation(float zoom) {
        if (mMapView.getWidth() == 0) {
            return;
        }
        LatLng location = new LatLng(mLocationClient.getLastLocation().getLatitude(),
                mLocationClient.getLastLocation().getLongitude());
        moveToLocation(location, zoom);
    }

    /**
     * Move the camera to the location and zoom. Without animation.
     *
     * @param location
     * @param zoom
     */
    public void moveToLocation(LatLng location, float zoom) {
        if (mMapView.getWidth() == 0) {
            return;
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(location);
        getMap().moveCamera(cameraUpdate);
    }

    /**
     * Move the camera to the bounds. Without animation.
     *
     * @param bounds
     * @param padding
     */
    public void moveToBounds(LatLngBounds bounds, int padding) {
        if (mMapView.getWidth() == 0) {
            return;
        }
        CameraUpdate cameraShowRoute = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        getMap().moveCamera(cameraShowRoute);
    }

    /**
     * Highlight the marker of the specified venue.
     *
     * @param venue
     */
    public void highlightVenueMarker(Venue venue) {
        Marker venueMarker = mMarkerVenueMap.inverse().get(venue);
        venueMarker.showInfoWindow();
    }

    /**
     * Check if the device is running Google Play Services.
     *
     * @return
     */
    private boolean hasPlayServices() {

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getSherlockActivity());
        if (status != ConnectionResult.SUCCESS) {
            if (!GooglePlayServicesUtil.isUserRecoverableError(status)) {
                Toast.makeText(getSherlockActivity(), "This device is not supported.",
                        Toast.LENGTH_LONG).show();
                getSherlockActivity().finish();
            }
            return false;
        }
        return true;

    }

    /**
     * @param startLocation
     */
    private void dispatchStartChange(LatLng startLocation) {
        AutoCompleteTextView searchView =
                (AutoCompleteTextView) getView().findViewById(R.id.search_location);

        if (mStartMarker != null && mStartMarker.getPosition().equals(startLocation)) {
            searchView.setHint(R.string.search_hint_marker);
        } else {
            searchView.setHint(R.string.search_hint);
        }

        if (mStartChangedListener != null) {
            mStartChangedListener.onStartChange(startLocation);
        }
    }

    /**
     *
     */
    private void dispatchStartUnavailable() {
        AutoCompleteTextView searchView =
                (AutoCompleteTextView) getView().findViewById(R.id.search_location);
        searchView.setHint("Search for start location.");

        if (mStartChangedListener != null) {
            mStartChangedListener.onStartUnavailable();
        }
    }

    @Override
    public void onRouteReceived(Route route) {
        mRoute = route;

        if (getView() == null) {
            mDrawOnAttach = true;
            return;
        }

        List<Venue> venues = route.getVenues();

        if (mStartMarker != null) {
            mStartMarker.remove();
            mStartMarker = null;
        }

        // Remove everything from the map
        getMap().clear();

        animateToBounds(route.getBounds(), 150);

        mMarkerVenueMap = HashBiMap.create();
        // Add markers for each venue
        Iterator<Venue> venueIterator = venues.iterator();
        while (venueIterator.hasNext()) {
            Venue venue = venueIterator.next();
            LatLng loc = venue.getLocation();
            Marker marker = getMap().addMarker(new MarkerOptions().position(loc).title(
                    venue.getName()));
            mMarkerVenueMap.put(marker, venue);
        }

        // Draw the polyline for the route
        List<LatLng> polyPoints = route.getPolyline();
        Polyline polyline = getMap().addPolyline(new PolylineOptions()
                .addAll(polyPoints));
        polyline.setColor(mPolylineColor);

        AutoCompleteTextView searchView =
                (AutoCompleteTextView) getView().findViewById(R.id.search_location);
        searchView.startAnimation(new HideSearchAnimation(searchView));
    }

    @Override
    public void onRouteFailed() {

    }

    @Override
    public void closeRoute() {
        mRoute = null;

        getMap().clear();
        AutoCompleteTextView searchView =
                (AutoCompleteTextView) getView().findViewById(R.id.search_location);

        searchView.setVisibility(View.VISIBLE);
        searchView.startAnimation(new ShowSearchAnimation(searchView));

        getMap().setInfoWindowAdapter(null);
        getMap().setOnInfoWindowClickListener(null);

        if (mStartMarker != null) {
            dispatchStartChange(mStartMarker.getPosition());
        } else if (mLocationClient.getLastLocation() != null) {
            dispatchStartChange(
                    new LatLng(
                            mLocationClient.getLastLocation().getLatitude(),
                            mLocationClient.getLastLocation().getLongitude()));
        } else {
            dispatchStartUnavailable();
        }
    }

    public GoogleMap getMap() {
        return mMap;
    }

    public OnStartChangeListener getStartChangedListener() {
        return mStartChangedListener;
    }

    public void setStartChangedListener(OnStartChangeListener startChangedListener) {
        mStartChangedListener = startChangedListener;
    }

    class HideSearchAnimation extends Animation {

        View mSearcView;

        int mMarginDelta;
        int mMarginStart;

        public HideSearchAnimation(View searcView) {
            mSearcView = searcView;

            mMarginDelta = -mSearcView.getHeight();
            mMarginStart = 0;

            setDuration(500);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            FrameLayout.LayoutParams params = (LayoutParams) mSearcView.getLayoutParams();
            params.topMargin = (int) (mMarginStart + mMarginDelta * interpolatedTime);
            mSearcView.setLayoutParams(params);
            mSearcView.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

    class ShowSearchAnimation extends Animation {

        View mSearcView;

        int mMarginDelta;
        int mMarginStart;

        public ShowSearchAnimation(View searcView) {
            mSearcView = searcView;

            mMarginDelta = mSearcView.getHeight();
            mMarginStart = -mSearcView.getHeight();

            setDuration(500);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            FrameLayout.LayoutParams params = (LayoutParams) mSearcView.getLayoutParams();
            params.topMargin = (int) (mMarginStart + mMarginDelta * interpolatedTime);
            mSearcView.setLayoutParams(params);
            mSearcView.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

}
