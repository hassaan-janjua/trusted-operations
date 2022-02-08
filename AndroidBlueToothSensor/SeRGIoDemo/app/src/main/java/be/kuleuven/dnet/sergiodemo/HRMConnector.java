package be.kuleuven.dnet.sergiodemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hqss on 3/11/2018.
 */

public class HRMConnector extends Thread {
    private final static String TAG = HRMConnector.class.getSimpleName();

    private MainActivity mMainActivity;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mScanner;
    private ArrayList<BluetoothDevice> mLeDevices;
    private boolean mStayAlive;

    public static final String HRM_NAME = "SeRGIoD";
    public static final String HRM_ADDRESS = "F0:D4:E3:46:4A:CE";

    Object syncObject;

    public HRMConnector(MainActivity mainActivity) {
        mMainActivity = mainActivity;
        mLeDevices = new ArrayList<BluetoothDevice>();
        syncObject = new Object();
        mStayAlive = true;
    }

    public void killThread() {
        mStayAlive = false;
        synchronized (syncObject) {
            syncObject.notify();
        }
    }

    @Override
    public void run() {

        boolean hrmFound = false;

        mBluetoothAdapter = mMainActivity.getBluetoothAdapter();

        mScanner = mBluetoothAdapter.getBluetoothLeScanner();

        while (!hrmFound && mStayAlive) {
            mLeDevices.clear();
            mScanner.startScan(mLeScanCallback);

            // Wait untill HRM is properly connected.
            synchronized (syncObject)
            {
                try {
                    syncObject.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    mScanner.stopScan(mLeScanCallback);
                }
            }

            for (BluetoothDevice device:
                    mLeDevices) {
                String name = device.getName();
                String address = device.getAddress();

                if (name != null && address != null && name.equals(HRMConnector.HRM_NAME) && address.equals(HRMConnector.HRM_ADDRESS)) {
                    hrmFound = true;
                    break;
                }
            }

        }

        mMainActivity.onBLEDeviceConnect();
    }

    private ScanCallback mLeScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    BluetoothDevice device = result.getDevice();
                    synchronized (syncObject) {
                        mLeDevices.add(device);
                        try {
                            syncObject.notify();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                @Override
                public void onScanFailed(int errorCode) {
                    Toast.makeText(mMainActivity, "Error while starting BLE Scan.", Toast.LENGTH_SHORT).show();
                }

                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult result:
                            results) {
                        BluetoothDevice device = result.getDevice();
                        synchronized (syncObject) { mLeDevices.add(device); }
                    }
                    synchronized (syncObject) {
                        try {
                            syncObject.notify();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            };




}
