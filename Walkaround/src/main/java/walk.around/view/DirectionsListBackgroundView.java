package walk.around.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class DirectionsListBackgroundView extends View {

    private Paint mPaintGroup;
    private Paint mPaintChild;


    public DirectionsListBackgroundView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public DirectionsListBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DirectionsListBackgroundView(Context context) {
        super(context);
        init();
    }


    private void init() {
        mPaintGroup = new Paint();
        mPaintGroup.setStrokeWidth(4);
        mPaintGroup.setColor(Color.BLUE);

        mPaintChild = new Paint();
        mPaintChild.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("Back", "Draw Draw");
        canvas.drawLine(10, 20, 10, 100, mPaintGroup);
    }
}
