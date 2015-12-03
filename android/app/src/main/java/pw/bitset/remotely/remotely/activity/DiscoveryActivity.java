package pw.bitset.remotely.remotely.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import pw.bitset.remotely.remotely.R;

public class DiscoveryActivity extends Activity {
    private ArrayAdapter<String> hostListAdapter;
    private List<String> availableHostsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_discovery);

        // TODO hardcoded for now.
        availableHostsList.add("192.168.1.84");

        hostListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        hostListAdapter.addAll(availableHostsList);

        final ListView hostList = (ListView) findViewById(R.id.lst_hosts);
        hostList.setAdapter(hostListAdapter);
        hostList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ControlActivity.show(DiscoveryActivity.this, availableHostsList.get(position), 5051);
            }
        });
    }
}
