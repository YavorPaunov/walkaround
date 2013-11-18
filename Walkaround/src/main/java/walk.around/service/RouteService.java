/*
 * Copyright (c) 2013. All Rights Reserved
 * Written by Yavor Paunov
 */

package walk.around.service;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask.Status;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.gms.maps.model.LatLng;

import walk.around.R;
import walk.around.route.LimitType;
import walk.around.route.Route;
import walk.around.route.RouteBuilder;
import walk.around.route.RouteCategory;
import walk.around.route.RouteConfig;

public class RouteService extends Service {

    public static final String CREATE_ROUTE = "walk.around.CREATE_ROUTE";
    public static final String GET_ROUTE = "walk.around.GET_ROUTE";
    public static final String SEND_ROUTE = "walk.around.SEND_ROUTE";
    private Route mRoute;
    private FullRouteBuilder mRouteBuilder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mRouteBuilder = new FullRouteBuilder();

        IntentFilter filter = new IntentFilter();
        filter.addAction(CREATE_ROUTE);
        filter.addAction(GET_ROUTE);

        RouteServiceReceiver receiver = new RouteServiceReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class FullRouteBuilder extends RouteBuilder {
        protected void onPostExecute(Route result) {
            mRoute = result;
            Intent sendRouteIntent = new Intent(SEND_ROUTE);

            if (mRoute == null) {
                // Building the route was not successful. Pass intent to activity saying why
                sendRouteIntent.putExtra("success", false);
                sendRouteIntent.putExtra("message", mRouteBuilder.getMessage());
            } else {
                // Building the route was successful
                sendRouteIntent.putExtra("success", true);
                sendRouteIntent.putExtra("route", mRoute);
            }
            mRouteBuilder = new FullRouteBuilder();
            LocalBroadcastManager.getInstance(RouteService.this).sendBroadcast(sendRouteIntent);
        }
    }

    class RouteServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null) {
                return;
            }

            if (action.equals(CREATE_ROUTE)) {
                if (mRouteBuilder.getStatus() == Status.PENDING) {
                    // The activity has requested a new route with the config in the intent. Build it.
                    LatLng currentLocation = intent.getParcelableExtra("startLocation");
                    RouteCategory category = (RouteCategory) intent.getSerializableExtra("category");
                    LimitType limitType = (LimitType) intent.getSerializableExtra("limitType");
                    int limit = intent.getIntExtra("limit", 0);

                    String trackingId = getResources().getString(R.string.ga_trackingId);

                    RouteConfig routeConfig = new RouteConfig(category, limitType, limit, currentLocation);
                    mRouteBuilder.execute(routeConfig);
                }
            } else if (action.equals(GET_ROUTE)) {
                // The activity has requested the last built route. Currently this is not being called.
                Intent sendRouteIntent = new Intent(SEND_ROUTE);
                sendRouteIntent.putExtra("route", mRoute);
                LocalBroadcastManager.getInstance(RouteService.this).sendBroadcast(sendRouteIntent);
            }

        }
    }
}
