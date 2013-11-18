package walk.around.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;

import walk.around.R;

public class DirectionsListView extends ExpandableListView {

    private Paint mPaintGroup;
    private Paint mPaintChild;
    private Paint mPaintStroke;

    public DirectionsListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray typedAttrs = context.obtainStyledAttributes(attrs, R.styleable.DirectionsListView);
        init(typedAttrs);
    }

    public DirectionsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedAttrs = context.obtainStyledAttributes(attrs, R.styleable.DirectionsListView);
        init(typedAttrs);
    }

    public DirectionsListView(Context context) {
        super(context);
        init();
    }

    private void init(TypedArray typedAttrs) {
        init();
    }

    private void init() {
        mPaintGroup = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintGroup.setStyle(Paint.Style.FILL);
        mPaintGroup.setColor(Color.WHITE);
        mPaintGroup.setStrokeWidth(12);

        mPaintChild = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintChild.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintChild.setColor(Color.GRAY);
        mPaintChild.setStrokeWidth(8);

        mPaintStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintStroke.setStyle(Paint.Style.STROKE);
        mPaintStroke.setColor(Color.GRAY);
        mPaintStroke.setStrokeWidth(6);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int lineX = dpToPx(16);

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);

            if (view instanceof RouteLegView) {
                int top = view.getTop();
                int bottom = view.getBottom() + getDividerHeight();

                if(((RouteLegView) view).getPosition() == 0) {
                    top += view.getHeight() / 2;
                } else if (((RouteLegView) view).getPosition()
                        == getExpandableListAdapter().getGroupCount() - 1) {
                    bottom -= view.getHeight() / 2;
                }



                canvas.drawLine(lineX, top, lineX, bottom, mPaintStroke);

                canvas.drawCircle(lineX, view.getTop() + view.getHeight() / 2 , 8, mPaintGroup);
                canvas.drawCircle(lineX, view.getTop() + view.getHeight() / 2 , 8, mPaintStroke);
            } else if (view instanceof RouteStepView) {
                canvas.drawLine(lineX, view.getTop(), lineX, view.getBottom() + getDividerHeight(), mPaintStroke);

                canvas.drawCircle(lineX, view.getTop() + view.getHeight() / 2, 4, mPaintChild);
              //  canvas.drawCircle(20, view.getTop() + view.getHeight() / 2, 4, mPaintStroke);
            }

        }
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        assert adapter instanceof RouteAdapter;
        super.setAdapter(adapter);
    }

    @Override
    public void setAdapter(ExpandableListAdapter adapter) {
        assert adapter instanceof RouteAdapter;
        super.setAdapter(adapter);
    }
}
