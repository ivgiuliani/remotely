package pw.bitset.remotely.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Vibrator;
import android.support.annotation.WorkerThread;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pw.bitset.remotely.R;

public class ControlActivity extends Activity {
    private static final String TAG = "ControlActivity";

    private static final String INTENT_KEY_HOST = "intent_key_host";
    private static final String INTENT_KEY_PORT = "intent_key_port";

    private static final long NUDGE_DURATION_MS = 40;

    private Executor threadExecutor = Executors.newSingleThreadExecutor();

    private ImageButton buttonVolumeDown;
    private ImageButton buttonVolumeUp;
    private ImageButton buttonVolumeMute;
    private ImageButton buttonPlay;
    private ImageButton buttonPause;

    private String currentHost;
    private int currentPort;

    private View.OnClickListener commandClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String command = (String) v.getTag(R.integer.key_command);
            sendCommand(command);
            nudge();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        currentHost = intent.getStringExtra(INTENT_KEY_HOST);
        if (currentHost == null) {
            Log.e(TAG, "Expected host.");
            finish();
        }
        currentPort = intent.getIntExtra(INTENT_KEY_PORT, 0);
        if (currentPort <= 0) {
            Log.e(TAG, "Expected port.");
            finish();
        }

        buttonVolumeDown = (ImageButton) findViewById(R.id.btn_volume_down);
        buttonVolumeUp = (ImageButton) findViewById(R.id.btn_volume_up);
        buttonVolumeMute = (ImageButton) findViewById(R.id.btn_volume_mute);
        buttonPlay = (ImageButton) findViewById(R.id.btn_volume_play);
        buttonPause = (ImageButton) findViewById(R.id.btn_volume_pause);

        buttonVolumeDown.setTag(R.integer.key_command, "vol_down");
        buttonVolumeUp.setTag(R.integer.key_command, "vol_up");
        buttonVolumeMute.setTag(R.integer.key_command, "vol_mute");
        buttonPlay.setTag(R.integer.key_command, "mm_play");
        buttonPause.setTag(R.integer.key_command, "mm_pause");

        buttonVolumeDown.setOnClickListener(commandClickListener);
        buttonVolumeUp.setOnClickListener(commandClickListener);
        buttonVolumeMute.setOnClickListener(commandClickListener);
        buttonPlay.setOnClickListener(commandClickListener);
        buttonPause.setOnClickListener(commandClickListener);
    }

    @WorkerThread
    private void sendData(String host, int port, String data) throws IOException {
        Log.v(TAG, "Sending " + data);

        DatagramSocket client_socket = new DatagramSocket(port);
        InetAddress IPAddress =  InetAddress.getByName(host);

        DatagramPacket send_packet = new DatagramPacket(data.getBytes(), data.length(), IPAddress, port);
        client_socket.send(send_packet);
        client_socket.close();
    }

    private void sendCommand(String commandName) {
        final JSONObject obj = new JSONObject();
        try {
            obj.put("name", commandName);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        threadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    sendData(currentHost, currentPort, obj.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void nudge() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(NUDGE_DURATION_MS);
        }
    }

    static void show(Activity parentActivity, String host, int port) {
        Intent intent = new Intent(parentActivity, ControlActivity.class);
        intent.putExtra(INTENT_KEY_HOST, host);
        intent.putExtra(INTENT_KEY_PORT, port);
        parentActivity.startActivity(intent);
    }
}
