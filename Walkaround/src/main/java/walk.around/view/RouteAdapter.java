package walk.around.view;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import walk.around.R;
import walk.around.route.OnDistanceClickListener;
import walk.around.route.OnStepClickListener;
import walk.around.route.OnVenueClickListener;
import walk.around.route.RouteLeg;
import walk.around.route.RouteStep;
import walk.around.route.Venue;

public class RouteAdapter extends BaseExpandableListAdapter {

    private Context mContext;

    private List<RouteLeg> mGroups;
    private List<List<RouteStep>> mChildren;

    private int[] mGroupHeights;
    private int[][] mChildHeights;

    private OnVenueClickListener mOnVenueClickListener;
    private OnStepClickListener mOnStepClickListener;
    private OnDistanceClickListener mOnDistanceClickListener;

    public RouteAdapter(Context context, List<RouteLeg> groups, List<List<RouteStep>> children) {
        mContext = context;

        mGroups = groups;
        mChildren = children;

        mGroupHeights = new int[groups.size()+1];
        mChildHeights = new int[children.size()][];

        for (int i = 0; i < mChildHeights.length; i++) {
            mChildHeights[i] = new int[children.get(i).size()];
        }
    }

    @Override
    public int getGroupCount() {
        if(getGroups().size() == 0){
            return 0;
        }
        return getGroups().size() + 1;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(groupPosition == getGroups().size()){
            return 0;
        }
        int childrenCount = getChildren().get(groupPosition).size();
        return childrenCount;
    }

    @Override
    public RouteLeg getGroup(int groupPosition) {
        // return last leg
        if(groupPosition == getGroups().size()) {
            return getGroups().get(groupPosition - 1);
        }

        // Return Leg
        return getGroups().get(groupPosition);
    }

    @Override
    public RouteStep getChild(int groupPosition, int childPosition) {
        if(groupPosition == getGroups().size()){
            return null;
        }
        return getChildren().get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition * 10000L + childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        RouteLegView view;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = (RouteLegView) inflater.inflate(R.layout.route_group, parent, false);
        } else {
            view = (RouteLegView) convertView;
        }

        view.setPosition(groupPosition);
        view.setStep(getGroup(groupPosition));

        Button nameView = (Button) view.findViewById(R.id.text_name);

        Button distanceView = (Button) view.findViewById(R.id.text_distance);
        if(groupPosition == getGroups().size()) {
            distanceView.setVisibility(View.GONE);
            nameView.setText(String.format(getGroup(groupPosition).getEndVenue().getName()));
        } else {
            distanceView.setVisibility(View.VISIBLE);
            nameView.setText(String.format(getGroup(groupPosition).getStartVenue().getName()));
            distanceView.setText(getGroup(groupPosition).getDistanceString());
        }

        nameView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnVenueClickListener != null) {
                    RouteLeg leg = getGroup(groupPosition);

                    Venue venue;
                    if(groupPosition == getGroupCount() - 1) {
                        venue = leg.getEndVenue();
                    } else {
                        venue = leg.getStartVenue();

                    }

                    mOnVenueClickListener.onVenueClicked(venue);
                }
            }
        });

        distanceView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if(mOnDistanceClickListener != null) {
                    mOnDistanceClickListener.onDistanceClicked(groupPosition);
                }

            }

        });

        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        mGroupHeights[groupPosition] = view.getMeasuredHeight();

        return view;
    }

    @Override
    public View getChildView(final int groupPosition,
                             final int childPosition,
                             boolean isLastChild,
                             View convertView,
                             ViewGroup parent) {

        if(groupPosition == getGroups().size()){
            return convertView;
        }

        RouteStepView view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = (RouteStepView) inflater.inflate(R.layout.route_child, parent, false);

            String instructions = String.format(
                    "%d. " + Html.fromHtml(getChild(groupPosition, childPosition).getInstructions()),
                    childPosition + 1);

            Button instructionsView = (Button) view.findViewById(R.id.button_instructions);
            instructionsView.setText(instructions);
            instructionsView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnStepClickListener != null) {
                        mOnStepClickListener.onStepClicked(getChild(groupPosition, childPosition));
                    }
                }
            });
        } else {
            view = (RouteStepView) convertView;
        }

        view.setPosition(childPosition);
        view.setLegPosition(groupPosition);
        view.setStep(getChild(groupPosition, childPosition));

        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        mChildHeights[groupPosition][childPosition] = view.getMeasuredHeight();

        Log.d("Child Height", String.valueOf(mChildHeights[groupPosition][childPosition]));
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public Context getContext() {
        return mContext;
    }

    public List<RouteLeg> getGroups() {
        return mGroups;
    }

    public List<List<RouteStep>> getChildren() {
        return mChildren;
    }

    public int[] getGroupHeights() {
        return mGroupHeights;
    }

    public int[][] getChildHeights() {
        return mChildHeights;
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


    public OnDistanceClickListener getOnDistanceClickListener() {
        return mOnDistanceClickListener;
    }

    public void setOnDistanceClickListener(OnDistanceClickListener onDistanceClickListener) {
        mOnDistanceClickListener = onDistanceClickListener;
    }
}
