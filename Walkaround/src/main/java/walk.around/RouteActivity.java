package walk.around;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.model.LatLng;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.Locale;

import walk.around.fragments.GenerateFragment;
import walk.around.fragments.RouteListFragment;
import walk.around.fragments.RouteMapFragment;
import walk.around.layouts.MapSlidingPaneLayout;
import walk.around.route.OnCategoryChangeListener;
import walk.around.route.OnStartChangeListener;
import walk.around.route.OnStepClickListener;
import walk.around.route.OnVenueClickListener;
import walk.around.route.Route;
import walk.around.route.RouteCategory;
import walk.around.route.RouteStep;
import walk.around.route.Venue;
import walk.around.service.RouteService;

public class RouteActivity extends SherlockFragmentActivity implements
        OnStartChangeListener, OnCategoryChangeListener,
        OnVenueClickListener, OnStepClickListener,
        OnMyLocationChangeListener {

    MapSlidingPaneLayout mMapFragmentContainer;
    RouteMapFragment mMapFragment;
    RouteListFragment mListFragment;
    GenerateFragment mGenerateFragment;
    Route mRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources res = getApplicationContext().getResources();

        Locale locale = new Locale("en");
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.locale = locale;

        res.updateConfiguration(config, res.getDisplayMetrics());

        setContentView(R.layout.activity_route);

        mMapFragmentContainer = (MapSlidingPaneLayout) findViewById(R.id.container_route_fragments);
        mMapFragmentContainer.setSlidingEnabled(false);
        mMapFragmentContainer.setShadowResource(R.drawable.slidingpane_shadow);

        mMapFragment = (RouteMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);
        mListFragment = (RouteListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_list);
        mGenerateFragment = (GenerateFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_generate);



        mListFragment.setOnVenueClickListener(this);
        mListFragment.setOnStepClickListener(this);

        mMapFragment.setStartChangedListener(this);
        mMapFragment.getMap().setOnMyLocationChangeListener(this);
        mGenerateFragment.setOnCategoryChangeListener(this);
        mGenerateFragment.showMessage("Getting your location...");
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(RouteService.SEND_ROUTE)) {
                    boolean success = intent.getExtras().getBoolean("success");
                    if (success) {

                        mRoute = intent.getExtras().getParcelable("route");
                        dispatchRouteCreated();

                    } else {
                        mListFragment.onRouteFailed();
                        mMapFragment.onRouteFailed();
                        String message = intent.getStringExtra("message");
                        Toast.makeText(RouteActivity.this, message, Toast.LENGTH_LONG).show();
                    }

                    ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.switcher_route_options);
                    if (switcher.getNextView().getId() == R.id.switcher_item_options) {
                        switcher.showNext();
                    }

                }
            }
        };

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RouteService.SEND_ROUTE);
        manager.registerReceiver(receiver, intentFilter);

        Intent routeServiceIntent = new Intent(this, RouteService.class);
        startService(routeServiceIntent);

        SlidingUpPanelLayout upSlider = (SlidingUpPanelLayout) findViewById(
                R.id.sliding_up_pane);
        upSlider.setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));

        upSlider.setEnableDragViewTouchEvents(true);
        upSlider.setSlidingEnabled(false);

        if(savedInstanceState != null) {
            mRoute = savedInstanceState.getParcelable("route");
            dispatchRouteCreated();
        }
    }

    @Override
    protected void onStart() {
        // TODO: Analytics not reporting anything. Find out why.
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("route", mRoute);
    }

    @Override
    public void onMyLocationChange(Location location) {
        mMapFragment.getMap().setOnMyLocationChangeListener(null);
        if(mRoute == null) {
            mGenerateFragment.showMessage(null);
        }
    }

    @Override
    public void onBackPressed() {
        // Let the list fragment handle it, if it does not continue
        if (mListFragment.onBackPressed()) {
            return;
        }

        SlidingUpPanelLayout upSlider =
                (SlidingUpPanelLayout) findViewById(R.id.sliding_up_pane);

        // If the generate fragment is visible, hide it
        if (upSlider.isExpanded()) {
            upSlider.collapsePane();
            mGenerateFragment.showMessage(null);
            return;
        }

        // If the sliding pane is open (list view is showing), hide it
        if (mMapFragmentContainer.isOpen()) {
            mMapFragmentContainer.closePane();
            return;
        }

        // If there is a route open, hide it and start from the beginning
        if (mRoute != null) {
            mListFragment.closeRoute();
            mGenerateFragment.closeRoute();
            mMapFragment.closeRoute();
            mMapFragmentContainer.setSlidingEnabled(false);
            mRoute = null;
            return;
        }

        // Default action
        super.onBackPressed();
    }

    @Override
    public void onStartChange(LatLng startLocation) {
        mGenerateFragment.setStartLocation(startLocation);
    }

    @Override
    public void onStartUnavailable() {
        mGenerateFragment.setStartLocation(null);
        mGenerateFragment.showMessage("Please select a start location.");
    }

    @Override
    public void onCategoryChange(RouteCategory category) {
        SlidingUpPanelLayout upSlider = (SlidingUpPanelLayout) findViewById(R.id.sliding_up_pane);
        upSlider.expandPane();
        findViewById(R.id.search_location).clearFocus();
    }

    @Override
    public void onStepClicked(RouteStep step) {
        mMapFragmentContainer.closePane();
        mMapFragment.animateToLocation(step.getStartPoint());
    }

    @Override
    public void onVenueClicked(Venue venue) {
        mMapFragmentContainer.closePane();
        mMapFragment.animateToLocation(venue.getLocation());
        mMapFragment.highlightVenueMarker(venue);
    }

    /**
     * Inform the fragments the route has been created and adjust the UI accordingly.
     */
    private void dispatchRouteCreated() {
        if (mRoute != null) {
            mListFragment.onRouteReceived(mRoute);
            mMapFragment.onRouteReceived(mRoute);
            mGenerateFragment.onRouteReceived(mRoute);

            mMapFragmentContainer.setSlidingEnabled(true);
            mMapFragmentContainer.openPane();

            // Collapse the vertical slider to reveal the map
            SlidingUpPanelLayout upSlider = (SlidingUpPanelLayout) findViewById(
                    R.id.sliding_up_pane);
            upSlider.collapsePane();
        }
    }

}
