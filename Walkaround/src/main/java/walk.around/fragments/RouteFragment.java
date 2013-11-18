/*
 * Copyright (c) 2013. All Rights Reserved
 * Written by Yavor Paunov
 */

package walk.around.fragments;

import com.actionbarsherlock.app.SherlockFragment;

import walk.around.route.Route;

/**
 * An abstract class describing the behaviour of a fragment displaying the route.
 */
public abstract class RouteFragment extends SherlockFragment {
    /**
     * The route has been received. This is called after RouteService sends an
     * intent to RouteActivty wth the route object.
     *
     * @param route
     */
    public abstract void onRouteReceived(Route route);

    /**
     * The route has failed to generate. This is called after the RouteService sends a fail message
     * to the RouteActivity.
     */
    public abstract void onRouteFailed();

    /**
     * The user has chosen to close the route. Clean up.
     */
    public abstract void closeRoute();
}
