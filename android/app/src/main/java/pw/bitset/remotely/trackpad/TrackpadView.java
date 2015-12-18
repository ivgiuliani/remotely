package pw.bitset.remotely.trackpad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import pw.bitset.remotely.R;

/**
 * Simulates a trackpad.
 */
public class TrackpadView extends View {
    private static final int DRAW_POINTER_RADIUS = 40;
    private static final int CORNER_RADIUS = 25;

    private List<TrackpadListener> listeners = new ArrayList<>();
    private GestureDetector gestureDetector;
    private boolean isFingerDown = false;
    private float lastKnownViewX = -1;
    private float lastKnownViewY = -1;

    private Paint pointerPaint;
    private Paint backgroundPaint;

    public TrackpadView(Context context) {
        super(context);
        init();
    }

    public TrackpadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrackpadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        pointerPaint = new Paint();
        pointerPaint.setStyle(Paint.Style.FILL);
        pointerPaint.setColor(getResources().getColor(R.color.colorAccent));

        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(getResources().getColor(R.color.trackpadBackground));
        backgroundPaint.setAntiAlias(true);

        GestureDetector.OnGestureListener gestureListener = new GestureListener();
        gestureDetector = new GestureDetector(getContext(), gestureListener);
    }

    public void addListener(TrackpadListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TrackpadListener listener) {
        listeners.remove(listener);
    }

    private void notifyOnMove(int deltaX, int deltaY) {
        for (TrackpadListener listener : listeners) {
            listener.onMove(deltaX, deltaY);
        }
    }

    private void notifyClick() {
        for (TrackpadListener listener : listeners) {
            listener.onClick();
        }
    }

    private void notifyDoubleClick() {
        for (TrackpadListener listener : listeners) {
            listener.onDoubleClick();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                lastKnownViewX = event.getX();
                lastKnownViewY = event.getY();
                invalidate();
                break;
            case MotionEvent.ACTION_DOWN:
                lastKnownViewX = event.getX();
                lastKnownViewY = event.getY();
                isFingerDown = true;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                isFingerDown = false;
                invalidate();
                break;
        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRoundRect(0, 0,
                getWidth(), getHeight(),
                CORNER_RADIUS, CORNER_RADIUS,
                backgroundPaint);

        if (isFingerDown) {
            drawPointer(canvas, lastKnownViewX, lastKnownViewY);
        }
    }

    private void drawPointer(Canvas canvas, float x, float y) {
        canvas.drawCircle(x, y, DRAW_POINTER_RADIUS, pointerPaint);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // The coordinate set here is not compatible with what we want, as will return -y when
            // we scroll down, but if we want to move the mouse pointer down we want to increment
            // its coordinates by y (same for scrolling horizontally on the x axis). Therefore we
            // simply invert the coordinates received here, as between the two, using a positive
            // delta for scrolling down makes more sense.
            notifyOnMove((int) -distanceX, (int) -distanceY);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            notifyClick();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            notifyDoubleClick();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
}
