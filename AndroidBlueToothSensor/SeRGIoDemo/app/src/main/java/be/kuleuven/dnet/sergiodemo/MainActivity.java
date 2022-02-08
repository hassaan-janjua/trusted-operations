package be.kuleuven.dnet.sergiodemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private BluetoothAdapter mBluetoothAdapter;
    private View mLayout;
    private Button mActionButton;

    private int mProtocolState;
    private HRMConnector mHRMConnector;
    private HRMCommunicator mHRMCommunicator;

    public static final int DISCONNECTED            = 0;
    public static final int CONNECTING              = 1;
    public static final int CONNECTED               = 2;
    public static final int CREATINGTING_SHARED_KEY = 3;
    public static final int SHARED_KEY_CREATED      = 4;
    public static final int CREATINGTING_SESSION    = 5;
    public static final int SESSION_CREATED         = 6;
    public static final int REQUESTING_DATA         = 7;
    public static final int RECEIVINGING_DATA       = 8;
    public static final int DISCONNECTING           = 9;


    private static final String []STATUS_TEXT = { "Disconnected", "Connecting ...", "Connected",         "Creating Shared Key ...",  "Shared Key Created",  "Creating Session ...", "Session Created", "Requesting Data ...", "Receiving Data", "Disconnecting ..."};
    private static final String []BUTTON_TEXT = { "Connect"     , "Connect",        "Create Shared Key", "Create Shared Key",        "Create Session",      "Create Session",       "Request Data"   , "Request Data",        "Disconnect",     "Disconnect"};


    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 0;

    private DataPoint []hrmGraphData = new DataPoint[100];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPackages();

        acquireBluetoothAdapter();

        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.main_layout);

        mActionButton = (Button) findViewById(R.id.action_button);

        updateStatusText();

        mActionButton.setOnClickListener(this);

        initCrypto();

        for (int i = 0; i < 100; i++) {
            hrmGraphData[i] = new DataPoint(i, 0);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        checkEnabled();

        checkPermissions();

        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(hrmGraphData);
        graph.addSeries(series);
    }

    private void checkPackages(){
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, proceed with the demo
        } else {
            // Permission is missing and must be requested.
            requestCoarseLocationPermission();
        }
    }

    private void acquireBluetoothAdapter() {
        final BluetoothManager bluetoothManager =
            (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    private void checkEnabled() {
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, R.string.no_ble_no_fun, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Requests the {@link android.Manifest.permission#ACCESS_COARSE_LOCATION} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestCoarseLocationPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.
            Snackbar.make(mLayout, R.string.no_location_no_fun,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_COARSE_LOCATION);
                }
            }).show();

        } else {
            Snackbar.make(mLayout, R.string.coarse_location_unavailable, Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            // Request for coarse location permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Proceed with BLE.
            } else {
                // Permission request was denied.
                Toast.makeText(this, R.string.no_location_no_fun, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_button)
        {
            onActionButtonClick(v);
        }
    }

    private void onActionButtonClick(View v) {
        mActionButton.setEnabled(false);

        switch (mProtocolState) {
            case DISCONNECTED:
                connectToBLE();
                mProtocolState = CONNECTING;
                break;
            case CONNECTING:
                break;
            case CONNECTED:
                createSharedKey();
                mProtocolState = CREATINGTING_SHARED_KEY;
                break;
            case CREATINGTING_SHARED_KEY:
                break;
            case SHARED_KEY_CREATED:
                createSession();
                mProtocolState = CREATINGTING_SESSION;
                break;
            case CREATINGTING_SESSION:
                break;
            case SESSION_CREATED:
                requestData();
                mProtocolState = REQUESTING_DATA;
                break;
            case REQUESTING_DATA:
                break;
            case RECEIVINGING_DATA:
                disconnect();
                mProtocolState = DISCONNECTING;
                break;
            case DISCONNECTING:
                resetComm();
                mProtocolState = DISCONNECTED;
                break;
            case -1:
                resetComm();
                mProtocolState = DISCONNECTED;
                break;
        }

        updateStatusText();

    }

    private void createSharedKey() {
        byte []publicKey = getPublicKey();
        mHRMCommunicator.createSharedKey(publicKey);
    }

    private void createSession() {
        byte[] nonce = getSessionNonce();
        mHRMCommunicator.createSession(nonce);
    }

    private void requestData() {
        mHRMCommunicator.requestData();
    }

    private void disconnect() {
        mHRMCommunicator.disconnect();
    }

    private void resetComm() {
        checkEnabled();

        checkPermissions();

        mHRMConnector.killThread();

        mHRMConnector = null;
    }

    public void onDisconnected() {
        try {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mActionButton.setEnabled(true);
                            mProtocolState = DISCONNECTED;
                            updateStatusText();
                        }
                    });
                }
            }, 100);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onSharedKeyCreated() {
        try {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mActionButton.setEnabled(true);
                            mProtocolState = SHARED_KEY_CREATED;
                            updateStatusText();
                        }
                    });
                }
            }, 100);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onSessionCreated() {
        try {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mActionButton.setEnabled(true);
                            mProtocolState = SESSION_CREATED;
                            updateStatusText();
                        }
                    });
                }
            }, 100);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onDataRequested() {
        try {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mActionButton.setEnabled(true);
                            mProtocolState = RECEIVINGING_DATA;
                            updateStatusText();
                        }
                    });
                }
            }, 100);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void onBLEDeviceConnect() {
        mHRMCommunicator = new HRMCommunicator(MainActivity.this);
        mHRMCommunicator.connect(HRMConnector.HRM_ADDRESS);
    }

    public void onBLEConnected(final int status) {
        try {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mActionButton.setEnabled(true);
                            mProtocolState = status;
                            updateStatusText();
                        }
                    });
                }
            }, 100);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onDataReceived(final byte []data, final int count) {
        try {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateGraph(data);
                        }
                    });
                }
            }, 100);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int[] convert(byte []data) {
        int []converted = new int[data.length/2];
        int i;

        for (i = 0; i < data.length; i+=2) {
            converted[i/2] = ((data[i + 1] & 0xFF) << 8) | ((data[i] & 0xFF));
        }
        return converted;
    }

    private void updateGraph(final byte []data) {
        int i;
        int []hrmData = convert(data);
        for (i = 99; i >= hrmData.length; i--) {
            hrmGraphData[i] = new DataPoint(i, hrmGraphData[i - hrmData.length].getY());
        }
        for (i = 0; i < hrmData.length; i++) {
            hrmGraphData[i] = new DataPoint(i, hrmData[i]);
        }
        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(hrmGraphData);
        graph.removeAllSeries();
        graph.addSeries(series);
        graph.invalidate();
    }

    private void connectToBLE() {

        acquireBluetoothAdapter();

        mHRMConnector = new HRMConnector(this);

        mHRMConnector.start();
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    void updateStatusText() {
        TextView tv = (TextView) findViewById(R.id.status_text);

        if (mProtocolState >= 0 && mProtocolState < STATUS_TEXT.length) {
            tv.setText(STATUS_TEXT[mProtocolState]);
            mActionButton.setText(BUTTON_TEXT[mProtocolState]);
        } else {
            tv.setText("Error!!");
            mActionButton.setText("Reset");
        }
    }

    public int getProtocolState() {
        return mProtocolState;
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void createSharedKey(byte []remotePublicKey);

    public native String initCrypto();

    public native byte[] getPublicKey();

    public native byte[] getSharedKey();

    public native byte[] getSessionNonce();

    public native byte[] getSessionKey();

    public native byte[] decryptData(byte []encryptedData);

    public native String testCrypto();
}
