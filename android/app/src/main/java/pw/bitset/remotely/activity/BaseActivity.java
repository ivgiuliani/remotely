package pw.bitset.remotely.activity;

import android.Manifest;
import android.app.Activity;
import android.os.Vibrator;
import android.support.annotation.RequiresPermission;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Base activity that provides utility methods common to all the other activities in the app.
 */
 abstract class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity";
    private static final long DEFAULT_NUDGE_DURATION_MS = 40;

    /**
     * Creates a retrofit request whose result is never checked.
     */
    protected static Callback<Void> newFireAndForgetRequest() {
        return new Callback<Void>() {
            @Override
            public void onResponse(Response<Void> response, Retrofit retrofit) {
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Coudln't complete request.", t);
            }
        };
    }

    /**
     * Postpone activity transitions until the window's decor view has finished its layout.
     * This avoids the flickering of the shared items (for example the status bar and the
     * navigation bar).
     */
    protected void postponeActivityTransitions() {
        postponeEnterTransition();
        final View decor = getWindow().getDecorView();
        decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                decor.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });
    }

    /**
     * Nudge the device a bit.
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    protected void nudge() {
        nudge(DEFAULT_NUDGE_DURATION_MS);
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    protected void nudge(long duration) {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(duration);
        }
    }
}
