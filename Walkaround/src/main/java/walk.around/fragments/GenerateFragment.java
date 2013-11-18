/*
* Copyright (c) 2013. All Rights Reserved
* Written by Yavor Paunov
*/

package walk.around.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.gms.maps.model.LatLng;

import walk.around.R;
import walk.around.route.LimitType;
import walk.around.route.OnCategoryChangeListener;
import walk.around.route.Route;
import walk.around.route.RouteCategory;
import walk.around.service.RouteService;

public class GenerateFragment extends RouteFragment implements
        OnClickListener,
        OnLongClickListener,
        OnSeekBarChangeListener {

    private final String TAG = "GenerateFragment";

    private final int MINUTE_PER_PROGRESS = 15;
    private final int METERS_PER_PROGRESS = 300;
    private final int PROGRESS_MIN = 10;

    private LatLng mStartLocation;
    private LimitType mLimitType = LimitType.DISTANCE;
    private RouteCategory mRouteCategory;
    private int mLimit;

    private Route mRoute;

    private OnCategoryChangeListener mOnCategoryChangeListener;

    private boolean mDrawOnAttach;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View topView = inflater.inflate(R.layout.fragment_route_generate, container, false);

        SeekBar seekBarLimit = (SeekBar) topView.findViewById(R.id.seek_limit);
        seekBarLimit.setOnSeekBarChangeListener(this);

        TextView textLimit;
        switch (mLimitType) {
            case DISTANCE:
                topView.findViewById(R.id.button_limit_distance).setBackgroundResource(
                        R.drawable.abs__list_pressed_holo_light);
                textLimit = (TextView) topView.findViewById(R.id.text_limit);
                setDistanceText(convertProgressToMeters(seekBarLimit.getProgress()), textLimit);
                mLimit = convertProgressToMeters(seekBarLimit.getProgress());
                break;
            case TIME:
                topView.findViewById(R.id.button_limit_time).setBackgroundResource(
                        R.drawable.abs__list_pressed_holo_light);
                textLimit = (TextView) topView.findViewById(R.id.text_limit);
                setTimeText(convertProgressToMinutes(seekBarLimit.getProgress()), textLimit);
                mLimit = convertProgressToMinutes(seekBarLimit.getProgress());
                break;
        }

        LinearLayout buttonsContainer = (LinearLayout) topView.findViewById(R.id.category_buttons_container);
        for (int i = 0; i < buttonsContainer.getChildCount(); i++) {
            buttonsContainer.getChildAt(i).setOnClickListener(this);
            buttonsContainer.getChildAt(i).setOnLongClickListener(this);
        }

        topView.findViewById(R.id.button_limit_time).setOnClickListener(this);
        topView.findViewById(R.id.button_limit_distance).setOnClickListener(this);
        topView.findViewById(R.id.button_create_route).setOnClickListener(this);

        return topView;
    }

    /**
     * Sends an intent to the RouteService requesting a new route.
     */
    private void requestRoute() {
        Intent requestIntent = new Intent(RouteService.CREATE_ROUTE);
        requestIntent.putExtra("startLocation", mStartLocation);
        requestIntent.putExtra("category", mRouteCategory);
        requestIntent.putExtra("limitType", mLimitType); // Enums are serializable. No probs here.
        requestIntent.putExtra("limit", mLimitType == LimitType.DISTANCE ? mLimit : mLimit * 60);

        LocalBroadcastManager.getInstance(getSherlockActivity()).sendBroadcast(requestIntent);

        ViewSwitcher bottomSwitcher = (ViewSwitcher) getView().findViewById(R.id.switcher_route_options);
        if (bottomSwitcher.getNextView().getId() == R.id.switcher_item_progress) {
            bottomSwitcher.showNext();
        }

        ViewSwitcher topSwitcher = (ViewSwitcher) getView().findViewById(R.id.switcher_route_category);
        if (topSwitcher.getNextView().getId() == R.id.route_info_container) {
            topSwitcher.showNext();
        }

        // Set Image
        String categoryString;
        switch (mRouteCategory) {
            case WALKING:
                categoryString = "walking";
                break;
            case DRINKS:
                categoryString = "drinks";
                break;
            case SHOPPING:
                categoryString = "shopping";
                break;
            default:
                categoryString = "";
                break;
        }

        setRouteInfo(mRouteCategory, "", "", String.format("Creating a %s route...", categoryString));

        ViewGroup buttonsContainer = (ViewGroup) topSwitcher.findViewById(R.id.category_buttons_container);
        for (int i = 0; i < buttonsContainer.getChildCount(); i++) {
            if (buttonsContainer.getChildAt(i) instanceof ImageButton) {
                ImageButton button = (ImageButton) buttonsContainer.getChildAt(i);
                button.setBackgroundResource(R.drawable.abs__list_selector_holo_light);
            }
        }
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
    public void onRouteReceived(Route route) {
        mRoute = route;
        // Display progressbar
        if (getView() == null) {
            mDrawOnAttach = true;
            return;
        }
        ViewSwitcher topSwitcher = (ViewSwitcher) getView().findViewById(R.id.switcher_route_category);

        // Exclude start (and end) location venue
        int venuesNum = route.getVenues().size() - 2;
        String venuesNumString;
        if (venuesNum == 1) {
            venuesNumString = String.format("%d venue", venuesNum);
        } else {
            venuesNumString = String.format("%d venues", venuesNum);
        }

        setRouteInfo(
                mRouteCategory,
                mRoute.getDistanceString(),
                mRoute.getDurationString(),
                venuesNumString);

        ViewSwitcher bottomSwitcher = (ViewSwitcher) getView().findViewById(R.id.switcher_route_options);
        if (bottomSwitcher.getNextView().getId() == R.id.switcher_item_options) {
            bottomSwitcher.showNext();
        }

        if (topSwitcher.getNextView().getId() == R.id.route_info_container) {
            topSwitcher.showNext();
        }
    }

    /**
     * Displays info for a newly shown route on top of the fragment.
     *
     * @param category
     * @param distanceString
     * @param durationString
     * @param venuesNum
     */
    private void setRouteInfo(RouteCategory category, String distanceString, String durationString, String venuesNum) {
        ViewSwitcher topSwitcher = (ViewSwitcher) getView().findViewById(R.id.switcher_route_category);
        // Set Image
        ImageView routeCategoryImage = (ImageView) topSwitcher.findViewById(R.id.image_route_category);
        int imageId = 0;
        if (category != null) {
            switch (category) {
                case WALKING:
                    imageId = R.drawable.icon_large_walking;
                    break;
                case DRINKS:
                    imageId = R.drawable.icon_large_drinks;
                    break;
                case SHOPPING:
                    imageId = R.drawable.icon_large_shopping;
                    break;
                default:
                    break;
            }
        }
        if (imageId == 0) {
            routeCategoryImage.setImageResource(android.R.color.transparent);
        } else {
            routeCategoryImage.setImageResource(imageId);
        }

        TextView distanceView = (TextView) topSwitcher.findViewById(R.id.text_route_distance);
        distanceView.setText(distanceString);

        TextView durationView = (TextView) topSwitcher.findViewById(R.id.text_route_time);
        durationView.setText(durationString);

        TextView numVenuesView = (TextView) topSwitcher.findViewById(R.id.text_num_venues);
        numVenuesView.setText(venuesNum);
    }

    @Override
    public void onRouteFailed() {
        // Display progressbar
        ViewSwitcher bottomSwitcher = (ViewSwitcher) getView().findViewById(R.id.switcher_route_options);
        if (bottomSwitcher.getNextView().getId() == R.id.switcher_item_options) {
            bottomSwitcher.showNext();
        }

        ViewSwitcher topSwitcher = (ViewSwitcher) getView().findViewById(R.id.switcher_route_category);
        if (topSwitcher.getNextView().getId() == R.id.category_buttons_container) {
            topSwitcher.showNext();
        }
    }

    @Override
    public void closeRoute() {
        ViewSwitcher topSwitcher = (ViewSwitcher) getView().findViewById(R.id.switcher_route_category);
        if (topSwitcher.getNextView().getId() == R.id.category_buttons_container && mStartLocation != null) {
            topSwitcher.showNext();
        } else if (mStartLocation != null) {
            showMessage("Please select a start location.");
        }
        TextView nameView = (TextView) topSwitcher.findViewById(R.id.text_route_distance);
        nameView.setText("");
        TextView durationView = (TextView) topSwitcher.findViewById(R.id.text_route_time);
        durationView.setText("");
        TextView numVenuesView = (TextView) topSwitcher.findViewById(R.id.text_num_venues);
        numVenuesView.setText("");
    }

    /**
     * Changes the content of the specified text view to a time based one.
     *
     * @param minutes
     * @param textTime
     */
    private void setTimeText(int minutes, TextView textTime) {
        String timeString = String.format("%d minutes", minutes +
                convertProgressToMeters(PROGRESS_MIN));
        textTime.setText(timeString);
    }

    /**
     * Changes the content of the default text view displaying the limit to a time based one.
     *
     * @param minutes
     */
    private void setTimeText(int minutes) {
        TextView textTime = (TextView) getView().findViewById(R.id.text_limit);
        setTimeText(minutes, textTime);
    }

    /**
     * Changes the content of the specified text view to a distance based one.
     *
     * @param meters
     * @param textDistance
     */
    private void setDistanceText(int meters, TextView textDistance) {
        String timeString = String.format("%d meters", meters +
                convertProgressToMeters(PROGRESS_MIN));
        textDistance.setText(timeString);
    }

    /**
     * Changes the content of the default text view displaying the limit to a distance based one.
     *
     * @param meters
     */
    private void setDistanceText(int meters) {
        TextView textDistance = (TextView) getView().findViewById(R.id.text_limit);
        setDistanceText(meters, textDistance);
    }

    /**
     * Converts the progress bar's progress value to minutes.
     *
     * @param progress
     * @return
     */
    private int convertProgressToMinutes(int progress) {
        return progress * MINUTE_PER_PROGRESS;
    }

    /**
     * Converts the progress bar's progress value to meters.
     *
     * @param progress
     * @return
     */
    private int convertProgressToMeters(int progress) {
        return progress * METERS_PER_PROGRESS;
    }

    /**
     * Displays or hides a message on the top of the fragment.
     * If message is null, the buttons are revealed.
     *
     * @param message
     */
    public void showMessage(String message) {
        ViewSwitcher topSwitcher =
                (ViewSwitcher) getView().findViewById(R.id.switcher_route_category);
        if (message != null) {
            setRouteInfo(null, "", "", message);
        }
        if (message == null && topSwitcher.getNextView().getId() == R.id.category_buttons_container) {
            topSwitcher.showNext();
        } else if (message != null && topSwitcher.getNextView().getId() == R.id.route_info_container) {
            topSwitcher.showNext();
        }
    }

    @Override
    public void onClick(View v) {
        LinearLayout buttonsContainer = (LinearLayout) getView().findViewById(R.id.category_buttons_container);
        if (v.getParent().equals(buttonsContainer)) {
            for (int i = 0; i < buttonsContainer.getChildCount(); i++) {
                buttonsContainer.getChildAt(i).setBackgroundResource(
                        R.drawable.abs__list_selector_holo_light);
            }
            v.setBackgroundResource(R.drawable.abs__list_pressed_holo_light);
            buttonsContainer.invalidate();
        }

        SeekBar seekBarLimit;

        switch (v.getId()) {
            case R.id.icon_walking:
                mRouteCategory = RouteCategory.WALKING;
                if (mOnCategoryChangeListener != null) {
                    mOnCategoryChangeListener.onCategoryChange(mRouteCategory);
                }
                break;
            case R.id.icon_shopping:
                mRouteCategory = RouteCategory.SHOPPING;
                if (mOnCategoryChangeListener != null) {
                    mOnCategoryChangeListener.onCategoryChange(mRouteCategory);
                }
                break;
            case R.id.icon_drinks:
                mRouteCategory = RouteCategory.DRINKS;
                if (mOnCategoryChangeListener != null) {
                    mOnCategoryChangeListener.onCategoryChange(mRouteCategory);
                }
                break;
            case R.id.button_create_route:
                requestRoute();
                break;
            case R.id.button_limit_distance:
                mLimitType = LimitType.DISTANCE;

                seekBarLimit = (SeekBar) getView().findViewById(R.id.seek_limit);
                setDistanceText(convertProgressToMeters(seekBarLimit.getProgress()));

                v.setBackgroundResource(R.drawable.abs__list_pressed_holo_light);
                getView().findViewById(R.id.button_limit_time).setBackgroundResource(
                        R.drawable.abs__list_selector_holo_light);
                break;
            case R.id.button_limit_time:
                mLimitType = LimitType.TIME;

                seekBarLimit = (SeekBar) getView().findViewById(R.id.seek_limit);
                setTimeText(convertProgressToMinutes(seekBarLimit.getProgress()));

                v.setBackgroundResource(R.drawable.abs__list_pressed_holo_light);
                getView().findViewById(R.id.button_limit_distance).setBackgroundResource(
                        R.drawable.abs__list_selector_holo_light);
                break;
            default:
                return;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (mLimitType) {
            case DISTANCE:
                setDistanceText(convertProgressToMeters(progress));
                mLimit = (PROGRESS_MIN + progress) * METERS_PER_PROGRESS;
                break;
            case TIME:
                setTimeText(convertProgressToMinutes(progress));
                mLimit = (PROGRESS_MIN + progress) * MINUTE_PER_PROGRESS;
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public LatLng getStartLocation() {
        return mStartLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        mStartLocation = startLocation;
        if (mStartLocation == null) {
            showMessage("Please select a start location.");
        } else {
            // Switch to buttons
            showMessage(null);
        }
    }

    public OnCategoryChangeListener getOnCategoryChangeListener() {
        return mOnCategoryChangeListener;
    }

    public void setOnCategoryChangeListener(OnCategoryChangeListener onCategoryChangeListener) {
        mOnCategoryChangeListener = onCategoryChangeListener;
    }


}
