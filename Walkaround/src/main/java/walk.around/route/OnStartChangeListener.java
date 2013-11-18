/*
 * Copyright (c) 2013. All Rights Reserved
 * Written by Yavor Paunov
 */

package walk.around.route;

import com.google.android.gms.maps.model.LatLng;

public interface OnStartChangeListener {

    public void onStartChange(LatLng startLocation);
    public void onStartUnavailable();

}
