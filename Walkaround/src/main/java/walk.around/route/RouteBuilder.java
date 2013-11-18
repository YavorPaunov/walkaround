/*
 * Copyright (c) 2013. All Rights Reserved
 * Written by Yavor Paunov
 */

package walk.around.route;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import walk.around.api.Foursquare;
import walk.around.api.GoogleMaps;

public abstract class RouteBuilder extends AsyncTask<RouteConfig, Void, Route> {

    private static final String COULD_NOT_LOAD = "Could not create a route with the parameters you specified. " +
            "Try a setting a longer distance or a different start location.";
    private final String TAG = "RouteBuilder";
    private String mMessage = "Unknown error.";
    private Venue mStartVenueDummy;

    @Override
    protected Route doInBackground(RouteConfig... config) {
        return generate(config[0].getCategory(), config[0].getLimitType(), config[0].getLimit(),
                config[0].getStartLocation());
    }

    /**
     * Generates a route with the given parameters.
     *
     * @param category
     * @param limitType     The type of the limit: distance or time (duration)
     * @param limit         The maximum time or distance the route should take.
     *                      Seconds if time or meters if distance.
     * @param startLocation The GPS coordinates of the starting location of the route.
     * @return
     */
    public Route generate(RouteCategory category, LimitType limitType, int limit,
                          LatLng startLocation) {
        mStartVenueDummy = new Venue(Venue.START);
        mStartVenueDummy.setName("Start location");
        mStartVenueDummy.setLocation(startLocation);

        String section;
        Route route = null;

        switch (category) {
            case WALKING:
                section = "outdoors";
                break;
            case SHOPPING:
                section = "shops";
                break;
            case DRINKS:
                section = "drinks";
                break;
            default:
                section = null;
                break;
        }

        int venuesLimit = 4;

        try {
            JSONObject jsonFoursquare = new JSONObject(
                    Foursquare.venuesExplore(startLocation, section,
                            venuesLimit));
            List<Venue> venues = buildVenues(jsonFoursquare);
            if (venues.size() <= 1) {
                mMessage = COULD_NOT_LOAD;
                return null;
            }

            JSONObject jsonDistanceMatrix = new JSONObject(
                    GoogleMaps.distanceMatrix(venues));
            Map<Venue, SortedMap<Venue, Integer>> distances = buildMatrix(
                    jsonDistanceMatrix, venues, limitType);

            List<Venue> orderedVenues = null;
            switch (limitType) {
                case DISTANCE:
                    orderedVenues = sortVenuesByDistance(distances, limit);
                    break;
                case TIME:
                    orderedVenues = sortVenuesByTime(distances, limit);
                    break;
            }

            if (orderedVenues == null) {
                mMessage = COULD_NOT_LOAD;
                return null;
            }

            Venue endVenueDummy = new Venue(mStartVenueDummy);
            endVenueDummy.setId(Venue.END);

            orderedVenues.add(endVenueDummy);

            JSONObject jsonDirections = new JSONObject(
                    GoogleMaps.directions(orderedVenues));

            route = new Route(jsonDirections, orderedVenues);
        } catch (JSONException e) {
            e.printStackTrace();
            mMessage = "Something went wrong.";
            return null;
        }

        return route;
    }

    protected List<Venue> sortVenuesByDistance(
            Map<Venue, SortedMap<Venue, Integer>> distances, int distanceLeft) {

        SortedMap<Venue, Integer> distancesToCurrent = distances
                .get(mStartVenueDummy);

        LinkedList<Venue> branchA = new LinkedList<Venue>();
        LinkedList<Venue> branchB = new LinkedList<Venue>();
        branchA.add(mStartVenueDummy);

        for (Venue venue : distancesToCurrent.keySet()) {

            int distance = distancesToCurrent.get(venue);
            // if (branchA.isEmpty() && distanceLeft > distance) {
            // branchA.add(venue);
            // distanceLeft -= distance;
            // } else
            if (branchB.isEmpty() && distanceLeft > distance) {
                if (!venue.equals(mStartVenueDummy)) {
                    branchB.add(venue);
                    distanceLeft -= distance;
                }
            } else {
                break;
            }
        }

        // TODO Check if branchB is empty. If it is, that means no venues were found within range.

        return sortVenuesByDistance(distances, branchA, branchB, distanceLeft);
    }

    protected List<Venue> sortVenuesByDistance(
            Map<Venue, SortedMap<Venue, Integer>> distances,
            LinkedList<Venue> branchA, LinkedList<Venue> branchB,
            int distanceLeft) {

        if (branchB.size() == 0) {
            // Failed loading a route
            mMessage = COULD_NOT_LOAD;
            return null;
        }

        SortedMap<Venue, Integer> distancesA = distances.get(branchA.getLast());
        SortedMap<Venue, Integer> distancesB = distances.get(branchB.getLast());

        int aToB = distances.get(branchA.getLast()).get(branchB.getLast());
        boolean changed = false;

        for (Venue venueA : distancesA.keySet()) {
            int distance = distancesA.get(venueA);
            if (!branchA.contains(venueA) && !branchB.contains(venueA)
                    && distanceLeft - aToB > distance) {
                branchA.add(venueA);
                distanceLeft -= distance;
                changed = true;
                break;
            }
        }

        aToB = distances.get(branchA.getLast()).get(branchB.getLast());
        for (Venue venueB : distancesB.keySet()) {
            int distance = distancesB.get(venueB);
            if (!branchA.contains(venueB) && !branchB.contains(venueB)
                    && distanceLeft - aToB > distance) {
                branchB.add(venueB);
                distanceLeft -= distance;
                changed = true;
                break;
            }
        }

        if (changed) {
            return sortVenuesByDistance(distances, branchA, branchB, distanceLeft);
        } else {
            List<Venue> orderedVenues = new ArrayList<Venue>();
            orderedVenues.addAll(branchA);
            Collections.reverse(branchB);
            orderedVenues.addAll(branchB);

            Log.d(TAG, orderedVenues.toString());

            return orderedVenues;
        }
    }

    protected List<Venue> sortVenuesByTime(Map<Venue, SortedMap<Venue, Integer>> distances,
                                           int timeLeft) {
        SortedMap<Venue, Integer> timesToCurrent = distances
                .get(mStartVenueDummy);

        LinkedList<Venue> branchA = new LinkedList<Venue>();
        LinkedList<Venue> branchB = new LinkedList<Venue>();
        branchA.add(mStartVenueDummy);

        for (Venue venue : timesToCurrent.keySet()) {

            int time = timesToCurrent.get(venue);
            // if (branchA.isEmpty() && distanceLeft > distance) {
            // branchA.add(venue);
            // distanceLeft -= distance;
            // } else
            if (branchB.isEmpty() && timeLeft > time) {
                if (!venue.equals(mStartVenueDummy)) {
                    branchB.add(venue);
                    timeLeft -= time;
                }
            } else {
                break;
            }

        }

        return sortVenuesByTime(distances, branchA, branchB, timeLeft);
    }

    protected List<Venue> sortVenuesByTime(Map<Venue, SortedMap<Venue, Integer>> distances,
                                           LinkedList<Venue> branchA, LinkedList<Venue> branchB,
                                           int timeLeft) {
        return null;
    }

    private List<Venue> buildVenues(JSONObject content) {
        // Successfully (or not?) acquired list of nearby venues
        Log.d(TAG, content.toString());

        List<Venue> venues = new LinkedList<Venue>();
        venues.add(mStartVenueDummy);

        try {
            JSONObject response = content.getJSONObject("response");
//			JSONObject meta = response.getJSONObject("meta");

            JSONArray groups = response.getJSONArray("groups");

            for (int i = 0; i < groups.length(); i++) {
                JSONArray items = groups.getJSONObject(i).getJSONArray("items");

                for (int j = 0; j < items.length(); j++) {
                    venues.add(new Venue(items.getJSONObject(j).getJSONObject(
                            "venue")));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            mMessage = "We are having issues connecting to the Foursquare API.";
            return null;
        }

        return venues;
    }

    public Map<Venue, SortedMap<Venue, Integer>> buildMatrix(
            JSONObject content, List<Venue> venues, LimitType limitType) {
        Map<Venue, SortedMap<Venue, Integer>> distances = null;
        try {
            distances = new HashMap<Venue, SortedMap<Venue, Integer>>();

            JSONArray rows = content.getJSONArray("rows");
            for (int i = 0; i < rows.length(); i++) {
                JSONArray rowElements = rows.getJSONObject(i).getJSONArray(
                        "elements");

                Map<Venue, Integer> distancesRow = new HashMap<Venue, Integer>();

                if (limitType == LimitType.DISTANCE) {
                    for (int j = 0; j < rowElements.length(); j++) {
                        JSONObject element = rowElements.getJSONObject(j);
                        distancesRow.put(venues.get(j),
                                element.getJSONObject("distance").getInt("value"));
                    }
                } else {
                    for (int j = 0; j < rowElements.length(); j++) {
                        JSONObject element = rowElements.getJSONObject(j);
                        distancesRow.put(venues.get(j),
                                element.getJSONObject("duration").getInt("value"));
                    }
                }

                // Sort the map based on its values in ascending order
                Comparator<Venue> distanceComparator = Ordering.natural()
                        .onResultOf(Functions.forMap(distancesRow))
                        .compound(Ordering.natural());

                SortedMap<Venue, Integer> sortedDistancesRow = ImmutableSortedMap
                        .copyOf(distancesRow, distanceComparator);

                distances.put(venues.get(i), sortedDistancesRow);
            }

            Log.d(TAG, "Matrix loaded");
        } catch (JSONException e) {
            e.printStackTrace();
            mMessage = "We are having issues connecting to the Google APIs.";
            return null;
        }

        return distances;
    }

    public String getMessage() {
        return mMessage;
    }
}
