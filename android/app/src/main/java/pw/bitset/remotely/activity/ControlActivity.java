package pw.bitset.remotely.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import pw.bitset.remotely.R;
import pw.bitset.remotely.api.Api;
import pw.bitset.remotely.api.DeltaCoordinates;
import pw.bitset.remotely.api.Keycode;
import pw.bitset.remotely.api.RemotelyService;
import pw.bitset.remotely.data.Service;
import pw.bitset.remotely.trackpad.TrackpadListener;
import pw.bitset.remotely.trackpad.TrackpadView;

public class ControlActivity extends BaseActivity {
    private static final String TAG = "ControlActivity";

    private static final String INTENT_KEY_SERVICE = "intent_key_service";

    private Service service;
    private RemotelyService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        postponeActivityTransitions();

        Intent intent = getIntent();
        service = intent.getParcelableExtra(INTENT_KEY_SERVICE);
        if (service == null) {
            Log.e(TAG, "Expected service.");
            finish();
            return;
        }

        api = Api.get(service);

        setupUI();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                performVolumeUp();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                performVolumeDown();
                return true;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                performVolumeMute();
                return true;
        }

        return super.onKeyDown(keyCode, event);
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
                performVolumeDown();
            }
        });
        buttonVolumeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performVolumeUp();
            }
        });
        buttonVolumeMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performVolumeMute();
            }
        });
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPlay();
            }
        });
        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPause();
            }
        });

        TrackpadView trackpadView = (TrackpadView) findViewById(R.id.trackpad);
        trackpadView.addListener(new TrackpadListener() {
            @Override
            public void onMove(int deltaX, int deltaY) {
                api.mouseMove(new DeltaCoordinates(deltaX, deltaY)).enqueue(newFireAndForgetRequest());
            }

            @Override
            public void onClick() {
                api.mouseClickLeft().enqueue(newFireAndForgetRequest());
                nudge();
            }
        });
    }

    private void performVolumeDown() {
        api.mediaVolumeDown().enqueue(newFireAndForgetRequest());
        nudge();
    }

    private void performVolumeUp() {
        api.mediaVolumeUp().enqueue(newFireAndForgetRequest());
        nudge();
    }

    private void performVolumeMute() {
        api.mediaVolumeMute().enqueue(newFireAndForgetRequest());
        nudge();
    }

    private void performPlay() {
        api.mediaPlay().enqueue(newFireAndForgetRequest());
        nudge();
    }

    private void performPause() {
        api.mediaPause().enqueue(newFireAndForgetRequest());
        nudge();
    }

    private class SoftKeyboardListener implements View.OnKeyListener, MenuItem.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            showKeyboard();
            return true;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_UP) {
                return false;
            }

            // The keycode we receive is an internal android representation of the keycode, *not*
            // the ASCII equivalent (which might not even exist).
            int finalKeyCode;
            switch (keyCode) {
                case KeyEvent.KEYCODE_DEL:
                    finalKeyCode = '\b';
                    break;
                case KeyEvent.KEYCODE_TAB:
                    finalKeyCode = '\t';
                    break;
                case KeyEvent.KEYCODE_ENTER:
                    finalKeyCode = '\n';
                    break;
                default:
                    finalKeyCode = event.getUnicodeChar(event.getMetaState());
                    break;
            }

            if (keyCode <= 0 || keyCode > 255) {
                return false;
            }

            api.keyboardPress(new Keycode(finalKeyCode)).enqueue(newFireAndForgetRequest());

            return true;
        }

        private void showKeyboard() {
            View rootView = findViewById(R.id.root);
            rootView.setFocusable(true);
            rootView.setFocusableInTouchMode(true);

            InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            rootView.requestFocus();
            im.showSoftInput(rootView, InputMethodManager.SHOW_IMPLICIT);

            rootView.setOnKeyListener(this);
        }
    }

    static void show(Activity parentActivity, Service service, @Nullable Bundle transitionOptions) {
        Intent intent = new Intent(parentActivity, ControlActivity.class);
        intent.putExtra(INTENT_KEY_SERVICE, service);
        parentActivity.startActivity(intent, transitionOptions);
    }
}
