package pw.bitset.remotely.remotely;

import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Executor threadExecutor = Executors.newSingleThreadExecutor();

    private ImageButton buttonVolumeDown;
    private ImageButton buttonVolumeUp;
    private ImageButton buttonVolumeMute;
    private ImageButton buttonPlay;
    private ImageButton buttonPause;

    private View.OnClickListener commandClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String command = (String) v.getTag(R.integer.key_command);
            sendCommand(command);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    private void sendData(String data) throws IOException {
        Log.v(TAG, "Sending " + data);

        DatagramSocket client_socket = new DatagramSocket(5051);
        InetAddress IPAddress =  InetAddress.getByName("192.168.1.84");

        DatagramPacket send_packet = new DatagramPacket(data.getBytes(), data.length(), IPAddress, 5051);
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
                    sendData(obj.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
