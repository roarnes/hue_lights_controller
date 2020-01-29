package arnes.respati.mqtt_app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, MonitorNotifier {
    private final String BEACON_1 = "f6ff4c55aab77cdabd23";
    private final String BEACON_2 = "ea54002fc29c538f5fb8";
    private final String BEACON_3 = "5ebc3fab1ab2261ca0ee";

    private MQTTHelper mqttHelper;

    private TextView dataReceived, tvConnectedTo, logText;
    private Button buttonOn, buttonOff, buttonAll;
    private SeekBar seekBarHue, seekBarBright, seekBarSat;
    private ListView listView;

    private int hue, brightness, saturation;
    private boolean foundBeacon = false;
    private MyAdapter adapter;
    private ArrayList<String> beaconID = new ArrayList<>() ;
    private String lamp_id;
    String[] values = new String[5];

    private boolean connected = false;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();
        regListeners();

        startMqtt();

        handler = new Handler();


        //beacon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            } else {
                Log.d(TAG, "Location permission already granted.");
            }
        }

        handler.post(disc);
    }

    public void displayData (){
        adapter = new MyAdapter(MainActivity.this, beaconID);
        listView.setAdapter(adapter);
    }

    //////////////////

    protected static final String TAG = "MonitoringActivity";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private BeaconManager mBeaconManager;

    @Override
    public void onResume() {
        super.onResume();
        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.URI_BEACON_LAYOUT));
        mBeaconManager.setDebug(true);
        mBeaconManager.bind(this);
    }
    @Override
    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);
        mBeaconManager.addMonitorNotifier(this);
        try {
            mBeaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Log.d(TAG, "??" + beacons.toString());

                Iterator<Beacon> iterator = beacons.iterator();

                String[] beaconStrings = new String[beacons.size()];
                String beaconsToString = beacons.toString();

                String[] arrayBeacons = beaconsToString.split(",");
                if(arrayBeacons[0]!=null) arrayBeacons[0]=arrayBeacons[0].substring(1);

                int lastElement = (arrayBeacons.length-1);

                arrayBeacons[lastElement] = arrayBeacons[lastElement].substring(0, arrayBeacons[lastElement].length()-1);
                for (int i = 0; i < arrayBeacons.length; i++) {
                    Log.d(TAG, "didRangeBeaconsInRegion: "+ arrayBeacons[i]);
                }
                if (beaconID != null) {
                    beaconID.clear();
                }
                for (int i = 0; i < beacons.size(); i++) {

                    Beacon beacon = iterator.next();
                    String loggg = "The first beacon I see is about " + beacon.getDistance() + " meters away.";
                    Log.i(TAG, loggg);
                    Log.i(TAG, "ID: " + beacon.getId1());
//                    logText.setText(loggg);
//                    Log.i(TAG, "ID: " + beacon.getId2());
                    beaconID.add(arrayBeacons[i]);
//                    beaconID.add(beacon.getId1().toString());
                    foundBeacon = true;
                }
//                beaconID.add(String.valueOf(beacons.toArray()));
                displayData();
            }
        });
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {    }
    }
    public void didEnterRegion(Region region) {
        Log.d(TAG, "I detected a beacon in the region with namespace id " + region.getId1() +
                " and instance id: " + region.getId2() + "; unique id: " + region.getUniqueId());
        foundBeacon = true;
    }
    public void didExitRegion(Region region) {
        Log.d(TAG, "Exited region?: " + region.getUniqueId() + " - " + region);
        foundBeacon = false;
    }
    public void didDetermineStateForRegion(int state, Region region) {
        Log.d(TAG, "Something something: " + state + " : " + region);
    }
    @Override
    public void onPause() {
        super.onPause();
        mBeaconManager.unbind(this);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }
                        });
                    }
                    builder.show();
                }
                return;
            }
        }
    }



    //////////////////

    private void initComponents(){
        dataReceived = (TextView) findViewById(R.id.dataReceived);
        tvConnectedTo = (TextView) findViewById(R.id.connectedTo);
//        logText = (TextView) findViewById(R.id.logText);
        buttonOn = (Button) findViewById(R.id.buttonOn);
        buttonOff = (Button) findViewById(R.id.buttonOff);
        buttonAll = (Button) findViewById(R.id.buttonAll);
//        buttonGetStatus = (Button) findViewById(R.id.buttonGetStatus);
        seekBarHue = (SeekBar) findViewById(R.id.seekBarHue);
        seekBarBright = (SeekBar) findViewById(R.id.seekBarBrightness);
        seekBarSat = (SeekBar) findViewById(R.id.seekBarSat);

        listView = (ListView) findViewById(R.id.lvBeaconList);
    }


    private void regListeners() {
        buttonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttHelper.publish(lamp_id + "=\"on\":true");
            }
        });

        buttonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttHelper.publish(lamp_id + "=\"on\":false");
            }
        });

        buttonAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lamp_id = "all";
                String temp = getString(R.string.connected_to) + lamp_id;
                tvConnectedTo.setText(temp);
                connected = true;
            }
        });

//        buttonSet.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mqttHelper.publish(lamp_id + "=\"hue\":" + hue +
//                        "; \"bri\":" + brightness +
//                        "; \"sat\": " + saturation);
//
////                mqttHelper.publish(lamp_id + "=\"hue\":" + hue +
////                        "; \"bri\":" + brightness);
////                mqttHelper.publish(lamp_id + "=\"sat\": " + saturation);
//
////                mqttHelper.publish(lamp_id + "=\"hue\":" + hue);
////                mqttHelper.publish(lamp_id + "=\"bri\":" + brightness);
//            }
//        });

        getHueValue();
        getBrightnessValue();
        getSatValue();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String lamp = listView.getItemAtPosition(position).toString();
                String lamp = adapter.getItem(position).toString();
                Log.d(TAG, "onItemClick: " + lamp);
                System.out.println(lamp);
                if (lamp.contains(BEACON_1)){
                    lamp_id =  "1";
                    String temp = getString(R.string.connected_to) + lamp_id;
                    tvConnectedTo.setText(temp);
                    handler.post(runnableCode);
                    connected = true;
                }
                else if (lamp.contains(BEACON_2)){
                    lamp_id = "2";
                    String temp = getString(R.string.connected_to) + lamp_id;
                    tvConnectedTo.setText(temp);
                    handler.post(runnableCode);
                    connected = true;
                }
                else if (lamp.contains(BEACON_3)){
                    lamp_id = "3";
                    String temp = getString(R.string.connected_to) + lamp_id;
                    tvConnectedTo.setText(temp);
                    handler.post(runnableCode);
                    connected = true;
                }
                else {
                    String temp = getString(R.string.connected_to) + lamp_id;
                    tvConnectedTo.setText(temp);
                }
            }
        });

//        buttonGetStatus.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                handler.post(runnableCode);
//                buttonGetStatus.setEnabled(false);
//            }
//        });

        // Create the Handler object (on the main thread by default)

// Start the initial runnable task by posting through the handler
    }



    // Define the code block to be executed
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            Log.d("Handlers", "Called on main thread");
            mqttHelper.publish(lamp_id + "=status");
            // Repeat this the same runnable code block again another 20 seconds
            handler.postDelayed(runnableCode, 20000);
        }
    };

    private Runnable disc = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            Log.d("Handlers", "Called on main thread");
            if (!connected) {
                try {
                    mqttHelper.disconnect();
                    tvConnectedTo.setText(R.string.not_connected);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            // Repeat this the same runnable code block again another 20 seconds
            handler.postDelayed(runnableCode, 10000);
        }
    };

    private void startMqtt() {
        mqttHelper = new MQTTHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug", mqttMessage.toString());

                String message = mqttMessage.toString();

                values = message.split(";");

                if (values.length != 4){
                    return;
                }

                values[1] = values[1].substring(6);
                Log.d(TAG, "messageArrived: bri " + values[1]);
                values[2] = values[2].substring(6);
                Log.d(TAG, "messageArrived: hue " + values[2]);
                values[3] = values[3].substring(6);
                Log.d(TAG, "messageArrived: sat " + values[3]);


                dataReceived.setText(mqttMessage.toString());

                if (!values[1].isEmpty()){
                    seekBarBright.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!values[1].equals("")) {
                                seekBarBright.setProgress(Integer.parseInt(values[1]));
                            }
                        }
                    });
                }

                if (!values[2].isEmpty()){
                    seekBarHue.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!values[2].equals("")) {
                                seekBarHue.setProgress(Integer.parseInt(values[2]));
                            }
                        }
                    });
                }

                if (!values[3].isEmpty()) {
                    seekBarSat.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!values[3].equals("")) {
                                seekBarSat.setProgress(Integer.parseInt(values[3]));
                            }
                        }
                    });
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    public void getHueValue(){
        seekBarHue.setMax(65535);
        seekBarHue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                hue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mqttHelper.publish(lamp_id + "=\"hue\":" + hue);
            }
        });

    }

    public void getBrightnessValue(){
        seekBarBright.setMax(254);
        seekBarBright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightness = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mqttHelper.publish(lamp_id + "=\"bri\":" + brightness);
            }
        });
    }

    public void getSatValue(){
        seekBarSat.setMax(254);
        seekBarSat.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                saturation = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mqttHelper.publish(lamp_id + "=\"sat\": " + saturation);
            }
        });

    }
}
