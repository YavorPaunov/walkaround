package walk.around.view;

import android.content.Context;
import android.util.AttributeSet;

import android.widget.LinearLayout;

import walk.around.route.RouteStep;

public class RouteStepView extends LinearLayout {

    int mPosition;
    int mLegPosition;
    RouteStep mStep;

    public RouteStepView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs/*, defStyle*/);
    }

    public RouteStepView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RouteStepView(Context context) {
        super(context);
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public RouteStep getStep() {
        return mStep;
    }

    public void setStep(RouteStep step) {
        mStep = step;
    }

    public int getLegPosition() {
        return mLegPosition;
    }

    public void setLegPosition(int legPosition) {
        mLegPosition = legPosition;
    }
}
