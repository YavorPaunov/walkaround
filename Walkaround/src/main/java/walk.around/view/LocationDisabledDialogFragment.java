/*
 * Copyright (c) 2013. All Rights Reserved
 * Written by Yavor Paunov
 */

package walk.around.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import walk.around.R;

public class LocationDisabledDialogFragment extends DialogFragment {
    OnClickListener mOnPositiveClickListener;
    OnClickListener mOnNegativeClickListener;

    public LocationDisabledDialogFragment() {
    }

    public static LocationDisabledDialogFragment newInstance(
            OnClickListener positive,
            OnClickListener negative) {
        LocationDisabledDialogFragment dialog = new LocationDisabledDialogFragment();
        dialog.setOnPositiveClickListener(positive);
        dialog.setOnNegativeClickListener(negative);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog alert = new AlertDialog.Builder(getActivity())
                .setPositiveButton("OK", mOnPositiveClickListener)
                .setNegativeButton("Cancel", mOnNegativeClickListener)
                .create();
        alert.show();
        return alert;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_location_disabled, container, false);
    }

    public OnClickListener getOnPositiveClickListener() {
        return mOnPositiveClickListener;
    }

    public void setOnPositiveClickListener(OnClickListener onPositiveClickListener) {
        mOnPositiveClickListener = onPositiveClickListener;
    }

    public OnClickListener getOnNegativeClickListener() {
        return mOnNegativeClickListener;
    }

    public void setOnNegativeClickListener(OnClickListener onNegativeClickListener) {
        mOnNegativeClickListener = onNegativeClickListener;
    }

}
