package pw.bitset.remotely.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import pw.bitset.remotely.R;
import pw.bitset.remotely.api.DeltaCoordinates;
import pw.bitset.remotely.api.RemotelyService;
import pw.bitset.remotely.data.Service;
import pw.bitset.remotely.trackpad.TrackpadListener;
import pw.bitset.remotely.trackpad.TrackpadView;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class ControlActivity extends Activity {
    private static final String TAG = "ControlActivity";

    private static final String INTENT_KEY_SERVICE = "intent_key_service";

    private static final long NUDGE_DURATION_MS = 40;

    private Service service;
    private RemotelyService api;

    private static final Callback<Void> FIRE_AND_FORGET_REQUEST = new Callback<Void>() {
        @Override
        public void onResponse(Response<Void> response, Retrofit retrofit) {
        }

        @Override
        public void onFailure(Throwable t) {
            Log.e(TAG, "Coudln't complete request.", t);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        Intent intent = getIntent();
        service = intent.getParcelableExtra(INTENT_KEY_SERVICE);
        if (service == null) {
            Log.e(TAG, "Expected service.");
            finish();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.format("http://%s:%d", service.host, service.port))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(RemotelyService.class);

        setupUI();
    }

    private void setupUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(service.name);
        toolbar.inflateMenu(R.menu.control_menu);
        toolbar.getMenu().findItem(R.id.menu_show_keyboard).setOnMenuItemClickListener(new SoftKeyboardListener());

        ImageButton buttonVolumeDown = (ImageButton) findViewById(R.id.btn_volume_down);
        ImageButton buttonVolumeUp = (ImageButton) findViewById(R.id.btn_volume_up);
        ImageButton buttonVolumeMute = (ImageButton) findViewById(R.id.btn_volume_mute);
        ImageButton buttonPlay = (ImageButton) findViewById(R.id.btn_volume_play);
        ImageButton buttonPause = (ImageButton) findViewById(R.id.btn_volume_pause);

        buttonVolumeDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.mediaVolumeDown().enqueue(FIRE_AND_FORGET_REQUEST);
                nudge();
            }
        });
        buttonVolumeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.mediaVolumeUp().enqueue(FIRE_AND_FORGET_REQUEST);
                nudge();
            }
        });
        buttonVolumeMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.mediaVolumeMute().enqueue(FIRE_AND_FORGET_REQUEST);
                nudge();
            }
        });
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.mediaPlay().enqueue(FIRE_AND_FORGET_REQUEST);
                nudge();
            }
        });
        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.mediaPause().enqueue(FIRE_AND_FORGET_REQUEST);
                nudge();
            }
        });

        TrackpadView trackpadView = (TrackpadView) findViewById(R.id.trackpad);
        trackpadView.addListener(new TrackpadListener() {
            @Override
            public void onMove(int deltaX, int deltaY) {
                api.mouseMove(new DeltaCoordinates(deltaX, deltaY)).enqueue(FIRE_AND_FORGET_REQUEST);
            }

            @Override
            public void onClick() {
                api.mouseClickLeft().enqueue(FIRE_AND_FORGET_REQUEST);
                nudge();
            }
        });
    }

    private void nudge() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(NUDGE_DURATION_MS);
        }
    }

    private class SoftKeyboardListener implements View.OnKeyListener, MenuItem.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            showKeyboard();
            return true;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            char pressedKey = event.getDisplayLabel();

            if (pressedKey == 0 || event.getAction() != KeyEvent.ACTION_UP) {
                return true;
            }

            api.keyboardPress(pressedKey).enqueue(FIRE_AND_FORGET_REQUEST);

            return true;
        }

        private void showKeyboard() {
            View rootView = findViewById(R.id.root);
            rootView.setFocusable(true);
            rootView.setFocusableInTouchMode(true);

            InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            rootView.requestFocus();
            im.showSoftInput(rootView, InputMethodManager.SHOW_FORCED);

            rootView.setOnKeyListener(this);
        }
    }

    static void show(Activity parentActivity, Service service) {
        Intent intent = new Intent(parentActivity, ControlActivity.class);
        intent.putExtra(INTENT_KEY_SERVICE, service);
        parentActivity.startActivity(intent);
    }
}
