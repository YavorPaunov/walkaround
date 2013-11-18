package walk.around.layouts;

import android.content.Context;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MapSlidingPaneLayout extends SlidingPaneLayout {

    private boolean mSlidingEnabled;

	public MapSlidingPaneLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
        mSlidingEnabled = true;
	}

	public MapSlidingPaneLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
        mSlidingEnabled = true;
	}

	public MapSlidingPaneLayout(Context context) {
		super(context);
        mSlidingEnabled = true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mSlidingEnabled || (event.getX() > (getWidth() / 6) && !this.isOpen())) {
            return false;
        }
        return super.onInterceptTouchEvent(event);
    }

    public boolean isSlidingEnabled() {
        return mSlidingEnabled;
    }

    public void setSlidingEnabled(boolean slidingEnabled) {
        mSlidingEnabled = slidingEnabled;
    }
}
