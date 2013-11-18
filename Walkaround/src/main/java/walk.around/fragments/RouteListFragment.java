package walk.around.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

import walk.around.R;
import walk.around.route.OnDistanceClickListener;
import walk.around.route.OnStepClickListener;
import walk.around.route.OnVenueClickListener;
import walk.around.route.Route;
import walk.around.route.RouteLeg;
import walk.around.route.RouteStep;
import walk.around.view.RouteAdapter;

public class RouteListFragment extends RouteFragment implements OnDistanceClickListener {

    private OnVenueClickListener mOnVenueClickListener;
    private OnStepClickListener mOnStepClickListener;

    private Route mRoute;

    private ExpandableListView mExpandableListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route_list, container);
        mExpandableListView = (ExpandableListView) view.findViewById(R.id.directions_list);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        if(mRoute != null) {
//            onRouteReceived(mRoute);
//        }
    }

    public void onRouteReceived(Route route) {
        mRoute = route;

        List<RouteLeg> groups = route.getLegs();

        List<List<RouteStep>> children = new ArrayList<List<RouteStep>>();
        for (int i = 0; i < groups.size(); i++) {
            RouteLeg group = groups.get(i);
            List<RouteStep> childList = group.getSteps();
            children.add(childList);
        }

        RouteAdapter stepsAdapter = new RouteAdapter(getActivity(), groups, children);
        mExpandableListView.setAdapter(stepsAdapter);
        stepsAdapter.setOnStepClickListener(getOnStepClickListener());
        stepsAdapter.setOnVenueClickListener(getOnVenueClickListener());
        stepsAdapter.setOnDistanceClickListener(this);

    }

    @Override
    public void onRouteFailed() {

    }

    @Override
    public void closeRoute() {
        mRoute = null;
        mExpandableListView.setAdapter((ExpandableListAdapter) null);
    }

    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onDistanceClicked(int index) {
        if (mExpandableListView.isGroupExpanded(index)) {
            mExpandableListView.collapseGroup(index);
        } else {
            mExpandableListView.expandGroup(index);
        }
    }

    public OnVenueClickListener getOnVenueClickListener() {
        return mOnVenueClickListener;
    }

    public void setOnVenueClickListener(OnVenueClickListener onVenueClickListener) {
        mOnVenueClickListener = onVenueClickListener;
    }

    public OnStepClickListener getOnStepClickListener() {
        return mOnStepClickListener;
    }

    public void setOnStepClickListener(OnStepClickListener onStepClickListener) {
        mOnStepClickListener = onStepClickListener;
    }
}
