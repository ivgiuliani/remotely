package pw.bitset.remotely.activity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.drawable.TransitionDrawable;
import android.os.Vibrator;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.RequiresPermission;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import pw.bitset.remotely.R;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Base activity that provides utility methods common to all the other activities in the app.
 */
 abstract class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity";
    private static final long DEFAULT_NUDGE_DURATION_MS = 40;
    private static final int TRANSITION_FAIL_MODE_MS = 300;
    private boolean isFailMode = false;

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
                Log.e(TAG, "Couldn't complete request.", t);
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
     * Enters the fail mode for the current activity.
     *
     * The fail mode consists in switching the toolbar to a different background that visually
     * indicatese a failure, and adding a subtitle that explains the problem. Disabling of the
     * individual controls is delegated to the activity's implementation.
     *
     * Note that this method is idempotent, and won't cause any changes if the activity is already
     * in fail mode.
     *
     * @param reason    a string resource with the reason of the failure.
     */
    protected void startFailMode(@StringRes int reason) {
        if (!isFailMode) {
            @ColorRes final int colorPrimaryDark = getResources().getColor(R.color.colorPrimaryDark);
            @ColorRes final int colorErrorDark = getResources().getColor(R.color.errorPrimaryDark);

            Log.d(TAG, "Entering fail mode.");
            isFailMode = true;
            Toolbar toolbar = (Toolbar) findViewById(getToolbarId());
            toolbar.setSubtitle(reason);
            TransitionDrawable transition = (TransitionDrawable) toolbar.getBackground();
            transition.startTransition(TRANSITION_FAIL_MODE_MS);

            View statusBar = findViewById(android.R.id.statusBarBackground);
            ObjectAnimator
                    .ofArgb(statusBar, "backgroundColor", colorPrimaryDark, colorErrorDark)
                    .setDuration(TRANSITION_FAIL_MODE_MS)
                    .start();
        }
    }

    /**
     * Leaves the fail mode for the current activity.
     *
     * The fail mode consists in switching the toolbar to a different background that visually
     * indicatese a failure, and adding a subtitle that explains the problem. Enabling of the
     * individual controls is delegated to the activity's implementation.
     *
     * Note that this method is idempotent, and won't cause any changes if the activity is already
     * in fail mode.
     */
    protected void stopFailMode() {
        if (isFailMode) {
            @ColorRes final int colorPrimaryDark = getResources().getColor(R.color.colorPrimaryDark);
            @ColorRes final int colorError = getResources().getColor(R.color.errorPrimaryDark);

            Log.d(TAG, "Leaving fail mode.");
            isFailMode = false;
            Toolbar toolbar = (Toolbar) findViewById(getToolbarId());
            toolbar.setSubtitle("");
            TransitionDrawable transition = (TransitionDrawable) toolbar.getBackground();
            transition.reverseTransition(TRANSITION_FAIL_MODE_MS);

            View statusBar = findViewById(android.R.id.statusBarBackground);
            ObjectAnimator
                    .ofArgb(statusBar, "backgroundColor", colorError, colorPrimaryDark)
                    .setDuration(TRANSITION_FAIL_MODE_MS)
                    .start();
        }
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

    /**
     * Returns the id of the toolbar for the current view.
     *
     * Used by accessory methods such as {@link #startFailMode(int)} or {@link #stopFailMode()}.
     *
     * @return the id of the toolbar for the current activity.
     */
    @IdRes
    protected int getToolbarId() {
        return R.id.toolbar;
    }
}
