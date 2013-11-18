package walk.around.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import walk.around.route.RouteLeg;

public class RouteLegView extends LinearLayout {

    int mPosition;
    RouteLeg mStep;

    public RouteLegView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs/*, defStyle*/);
    }

    public RouteLegView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public RouteLegView(Context context){
        super(context);
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public RouteLeg getStep() {
        return mStep;
    }

    public void setStep(RouteLeg step) {
        mStep = step;
    }
}
