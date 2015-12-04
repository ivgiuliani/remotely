package pw.bitset.remotely;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Simulates a trackpad.
 *
 * TODO
 */
public class TrackpadView extends View {
    public TrackpadView(Context context) {
        super(context);
    }

    public TrackpadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TrackpadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TrackpadView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(getResources().getColor(android.R.color.darker_gray));
    }
}
