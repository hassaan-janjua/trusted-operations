package be.kuleuven.dnet.sergiodemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by hqss on 3/11/2018.
 */

public class HRMCommunicator {

    private final static String TAG = HRMCommunicator.class.getSimpleName();


    private MainActivity mMainActivity;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;


    private BluetoothGattCharacteristic mHRMGattCharacteristic;
    private Object sendDataSync;
    private boolean sendDataStatus;

    private Object recvDataSync;
    private boolean recvDataStatus;
    private BlockingQueue<ArrayList<Byte>> receivedMessages =
            new ArrayBlockingQueue<ArrayList<Byte>>(100);

    private boolean isConnected  = false;


    public HRMCommunicator(MainActivity mainActivity) {
        mMainActivity = mainActivity;
        mBluetoothAdapter = mMainActivity.getBluetoothAdapter();
        sendDataSync = new Object();
        recvDataSync = new Object();
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mMainActivity, false, mGattCallback);


               // mHRMGattCharacteristic = ;
        Log.d(TAG, "Trying to create a new connection.");
        isConnected = true;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        isConnected = false;
        Thread worker = new Thread() {
            @Override
            public void run() {

                if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                    Log.w(TAG, "BluetoothAdapter not initialized");
                    return;
                }
                mBluetoothGatt.disconnect();

                mMainActivity.onDisconnected();
            }
        };

        worker.start();


    }

    public void sendDataBlocking(byte []data) {
        sendDataStatus = false;

        mHRMGattCharacteristic.setValue(data);

        while (!sendDataStatus) {
                try {
                    sendDataStatus = mBluetoothGatt.writeCharacteristic(mHRMGattCharacteristic);
                    synchronized (sendDataSync) {
                        sendDataSync.wait(100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }

    private void sendPublicKey(final byte []publicKey) {
        byte[] publicKey_code1 = {0x00, 0x00, 0x00, 0x01};
        byte[] publicKey_code2 = {0x00, 0x00, 0x00, 0x02};

        byte[] data = new byte[20];

        System.arraycopy(publicKey_code1, 0, data, 0, 4);
        System.arraycopy(publicKey, 0, data, 4, 16);

        sendDataBlocking(data);

        System.arraycopy(publicKey_code2, 0, data, 0, 4);
        System.arraycopy(publicKey, 16, data, 4, 16);

        sendDataBlocking(data);
    }

    private byte[] receiveRemotePublicKey() {
        byte[] remotePublicKey = new byte[32];
        ArrayList<Byte> remotePublicKeyPart = null;
        int i;

        try {
            remotePublicKeyPart = receivedMessages.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (remotePublicKeyPart != null)
        {
            for (i = 4; i < 20; i++) {
                remotePublicKey[i-4] = remotePublicKeyPart.get(i).byteValue();
            }
        }

        try {
            remotePublicKeyPart = receivedMessages.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (remotePublicKeyPart != null)
        {
            for (i = 4; i < 20; i++) {
                remotePublicKey[i+12] = remotePublicKeyPart.get(i).byteValue();
            }
        }

        return remotePublicKey;
    }

    public void createSharedKey(final byte []publicKey) {

        Thread worker = new Thread() {
            @Override
            public void run() {

                // Send Public key
                sendPublicKey(publicKey);

                // Receive Remote Public Key
                byte[] remotePublicKey = receiveRemotePublicKey();

                // Call createShared Key
                mMainActivity.createSharedKey(remotePublicKey);

                byte[] shared_key = mMainActivity.getSharedKey();


                mMainActivity.onSharedKeyCreated();
            }
        };

        worker.start();
    }

    public void createSession(final byte []nonce) {

        Thread worker = new Thread() {
            @Override
            public void run() {

                // Send Public key
                sendPublicKey(nonce);

                mMainActivity.onSessionCreated();
            }
        };

        worker.start();
    }

    public void requestData() {
        Thread worker = new Thread() {
            @Override
            public void run() {

                byte[] data = new byte[20];

                for (byte i = 0; i < 20; i++) {
                    data[i] = i++;
                }
                sendDataBlocking(data);

                mMainActivity.onDataRequested();

                receiveData();
            }
        };

        worker.start();
    }

    int fromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }


    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bytesToInt(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for ( int j = 0; j < bytes.length; j++ ) {
            sb.append((int)(bytes[j] & 0xFF) );
            sb.append(" ");
        }
        return sb.toString();
    }

    private void receiveData() {
        byte[] sessionKey = mMainActivity.getSessionKey();
        Log.e(TAG, "sessionKey: " + bytesToInt(sessionKey));

        while (mMainActivity.getProtocolState() == mMainActivity.RECEIVINGING_DATA || mMainActivity.getProtocolState() == mMainActivity.REQUESTING_DATA) {
            ArrayList<Byte> encryptedData = null;
            int counter;
            try {
                encryptedData = receivedMessages.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            byte []enc = new byte[20];
            int i;


            if (encryptedData != null)
            {
                for (i = 0; i < 20; i++) {
                    enc[i] = encryptedData.get(i).byteValue();
                }
            }

            byte []plain = mMainActivity.decryptData(enc);

            counter = enc[3] << 24 | (enc[2] & 0xFF) << 16 | (enc[1] & 0xFF) << 8 | (enc[0] & 0xFF);

            mMainActivity.onDataReceived(plain, counter);

            Log.e(TAG, counter + ": " + bytesToInt(plain));
        }
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");

                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString("0000a000-0000-1000-8000-00805f9b34fb"));

                if (service != null) {
                    mHRMGattCharacteristic = service.getCharacteristic(UUID.fromString("0000a001-0000-1000-8000-00805f9b34fb"));
                    if (mHRMGattCharacteristic != null) {
                        mBluetoothGatt.setCharacteristicNotification(mHRMGattCharacteristic, true);

                        BluetoothGattDescriptor descriptor = mHRMGattCharacteristic.getDescriptor(
                                UUID.fromString(GattAttributesMap.CLIENT_CHARACTERISTIC_CONFIG));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        boolean res = mBluetoothGatt.writeDescriptor(descriptor);

                        // If write descriptor failed, try again
                        while (!res)
                        {
                            res = mBluetoothGatt.writeDescriptor(descriptor);
                        }
                        if (isConnected)
                            mMainActivity.onBLEConnected(MainActivity.CONNECTED);
                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                recvDataSync.notify();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            //broadcastUpdate(ACTION_DATA_AVAILABLE, gatt, characteristic);
            if (mHRMGattCharacteristic.getUuid().equals(characteristic.getUuid())) {
                byte []v = characteristic.getValue();
                ArrayList<Byte> array = new ArrayList<Byte>();
                for (byte b:
                     v) {
                    array.add(new Byte(b));
                }
                try {
                    receivedMessages.put(array);
                } catch (InterruptedException iex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Unexpected interruption");
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendDataStatus = true;
                synchronized (sendDataSync) {
                    sendDataSync.notify();
                }
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendDataStatus = true;
                synchronized (sendDataSync) {
                    sendDataSync.notify();
                }
            }
        }


    };

}
